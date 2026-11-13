package com.dnavarro.neospectro.renderer

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.Choreographer
import android.view.TextureView
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface
import javax.microedition.khronos.opengles.GL10

/**
 * A TextureView-backed OpenGL ES 2.0 surface compatible with [GLSurfaceView.Renderer],
 * paced by [Choreographer] so rendering matches display VSync instead of spinning uncapped
 * on non-blocking [SurfaceTexture] buffer swaps.
 */
class GLTextureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextureView(context, attrs, defStyleAttr), TextureView.SurfaceTextureListener {

    var onFrameUpdated: (() -> Unit)? = null

    private var renderer: GLSurfaceView.Renderer? = null
    private var glThread: GLThread? = null
    private var pendingResume = false

    private val mainHandler = Handler(Looper.getMainLooper())
    private var choreographerRunning = false

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!choreographerRunning) return
            glThread?.requestRender()
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    init {
        isOpaque = true
        surfaceTextureListener = this
    }

    fun setRenderer(renderer: GLSurfaceView.Renderer) {
        check(this.renderer == null) { "Renderer already set" }
        this.renderer = renderer
        glThread = GLThread(renderer).apply {
            if (pendingResume) {
                requestPaused = false
            }
            start()
        }
        if (pendingResume && isAttachedToWindow) {
            startChoreographer()
        }
    }

    fun queueEvent(event: Runnable) {
        glThread?.queueEvent(event)
    }

    fun onResume() {
        pendingResume = true
        glThread?.onResume()
        startChoreographer()
    }

    fun onPause() {
        pendingResume = false
        stopChoreographer()
        glThread?.onPause()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (pendingResume) {
            startChoreographer()
        }
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        glThread?.onSurfaceAvailable(surface, width, height)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        glThread?.onWindowResize(width, height)
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        glThread?.onSurfaceDestroyed()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        onFrameUpdated?.invoke()
    }

    override fun onDetachedFromWindow() {
        stopChoreographer()
        glThread?.requestExitAndWait()
        glThread = null
        super.onDetachedFromWindow()
    }

    private fun startChoreographer() {
        val startAction = Runnable {
            if (!choreographerRunning && pendingResume) {
                choreographerRunning = true
                Choreographer.getInstance().postFrameCallback(frameCallback)
            }
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            startAction.run()
        } else {
            mainHandler.post(startAction)
        }
    }

    private fun stopChoreographer() {
        val stopAction = Runnable {
            if (choreographerRunning) {
                choreographerRunning = false
                Choreographer.getInstance().removeFrameCallback(frameCallback)
            }
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            stopAction.run()
        } else {
            mainHandler.post(stopAction)
        }
    }

    private class GLThread(
        private val renderer: GLSurfaceView.Renderer
    ) : Thread("GLTextureViewThread") {

        private val lock = ReentrantLock()
        private val condition = lock.newCondition()
        private val eventQueue = ConcurrentLinkedQueue<Runnable>()

        private var shouldExit = false
        private var exited = false
        var requestPaused = true
        private var paused = false
        private var renderRequested = false
        private var surfaceTexture: SurfaceTexture? = null
        private var width = 0
        private var height = 0
        private var sizeChanged = true
        private var hasEglContext = false
        private var hasEglSurface = false

        private var egl: EGL10? = null
        private var eglDisplay: EGLDisplay = EGL10.EGL_NO_DISPLAY
        private var eglConfig: EGLConfig? = null
        private var eglContext: EGLContext = EGL10.EGL_NO_CONTEXT
        private var eglSurface: EGLSurface = EGL10.EGL_NO_SURFACE
        private var gl10: GL10? = null

        fun queueEvent(event: Runnable) {
            eventQueue.add(event)
            lock.withLock {
                condition.signalAll()
            }
        }

        fun requestRender() {
            lock.withLock {
                renderRequested = true
                condition.signalAll()
            }
        }

        fun onResume() {
            lock.withLock {
                requestPaused = false
                renderRequested = true
                condition.signalAll()
            }
        }

        fun onPause() {
            lock.withLock {
                requestPaused = true
                renderRequested = false
                condition.signalAll()
                while (!exited && !paused) {
                    try {
                        condition.await()
                    } catch (_: InterruptedException) {
                        currentThread().interrupt()
                        return
                    }
                }
            }
        }

        fun onSurfaceAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            lock.withLock {
                this.surfaceTexture = surface
                this.width = width
                this.height = height
                this.sizeChanged = true
                this.renderRequested = true
                condition.signalAll()
            }
        }

        fun onWindowResize(width: Int, height: Int) {
            lock.withLock {
                this.width = width
                this.height = height
                this.sizeChanged = true
                this.renderRequested = true
                condition.signalAll()
            }
        }

        fun onSurfaceDestroyed() {
            lock.withLock {
                this.surfaceTexture = null
                condition.signalAll()
                while (!exited && hasEglSurface) {
                    try {
                        condition.await()
                    } catch (_: InterruptedException) {
                        currentThread().interrupt()
                        return
                    }
                }
            }
        }

        fun requestExitAndWait() {
            lock.withLock {
                shouldExit = true
                condition.signalAll()
                while (!exited) {
                    try {
                        condition.await()
                    } catch (_: InterruptedException) {
                        currentThread().interrupt()
                        return
                    }
                }
            }
        }

        override fun run() {
            try {
                guardedRun()
            } catch (_: InterruptedException) {
                currentThread().interrupt()
            } finally {
                lock.withLock {
                    exited = true
                    condition.signalAll()
                }
            }
        }

        private fun guardedRun() {
            try {
                while (true) {
                    var createContext = false
                    var createSurface = false
                    var doResize = false
                    var doDraw = false
                    var currentWidth = 0
                    var currentHeight = 0
                    var activeSurface: SurfaceTexture? = null

                    lock.withLock {
                        while (true) {
                            if (shouldExit) {
                                return
                            }

                            if (surfaceTexture == null && hasEglSurface) {
                                destroyEglSurfaceLocked()
                                condition.signalAll()
                            }

                            if (paused != requestPaused) {
                                paused = requestPaused
                                condition.signalAll()
                            }

                            val readyToDraw = !paused && surfaceTexture != null && width > 0 && height > 0
                            if (readyToDraw && (!hasEglSurface || renderRequested)) {
                                if (!hasEglContext) {
                                    createContext = true
                                }
                                if (!hasEglSurface) {
                                    createSurface = true
                                    doResize = true
                                } else if (sizeChanged) {
                                    doResize = true
                                }
                                sizeChanged = false
                                doDraw = renderRequested
                                renderRequested = false
                                currentWidth = width
                                currentHeight = height
                                activeSurface = surfaceTexture
                                break
                            }

                            // Drain queued events only when an EGL surface is current
                            if (hasEglSurface && !eventQueue.isEmpty()) {
                                break
                            }

                            condition.await()
                        }
                    }

                    if (createContext) {
                        if (!initEglContext()) {
                            lock.withLock {
                                shouldExit = true
                            }
                            return
                        }
                    }

                    if (createSurface && activeSurface != null) {
                        if (!createEglSurface(activeSurface)) {
                            lock.withLock {
                                shouldExit = true
                            }
                            return
                        }
                        if (createContext) {
                            renderer.onSurfaceCreated(gl10, eglConfig)
                        }
                    }

                    // Drain queued GL events before drawing
                    while (true) {
                        val event = eventQueue.poll() ?: break
                        event.run()
                    }

                    if (!hasEglSurface || paused || !doDraw) {
                        continue
                    }

                    if (doResize) {
                        renderer.onSurfaceChanged(gl10, currentWidth, currentHeight)
                    }

                    renderer.onDrawFrame(gl10)

                    val eglInstance = egl
                    if (eglInstance != null && eglSurface !== EGL10.EGL_NO_SURFACE) {
                        if (!eglInstance.eglSwapBuffers(eglDisplay, eglSurface)) {
                            val err = eglInstance.eglGetError()
                            if (err == EGL11_CONTEXT_LOST) {
                                lock.withLock {
                                    destroyEglSurfaceLocked()
                                    destroyEglContextLocked()
                                }
                            } else {
                                Log.w(TAG, "eglSwapBuffers failed: $err")
                            }
                        }
                    }
                }
            } finally {
                lock.withLock {
                    destroyEglSurfaceLocked()
                    destroyEglContextLocked()
                }
            }
        }

        private fun initEglContext(): Boolean {
            val eglInstance = EGLContext.getEGL() as EGL10
            egl = eglInstance

            val display = eglInstance.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
            if (display === EGL10.EGL_NO_DISPLAY) {
                Log.e(TAG, "eglGetDisplay failed")
                return false
            }

            val version = IntArray(2)
            if (!eglInstance.eglInitialize(display, version)) {
                Log.e(TAG, "eglInitialize failed")
                return false
            }
            eglDisplay = display

            val config = chooseEglConfig(eglInstance, display)
            if (config == null) {
                Log.e(TAG, "Unable to find a suitable EGLConfig")
                return false
            }
            eglConfig = config

            val attribList = intArrayOf(EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE)
            val context = eglInstance.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, attribList)
            if (context == null || context === EGL10.EGL_NO_CONTEXT) {
                Log.e(TAG, "eglCreateContext failed: ${eglInstance.eglGetError()}")
                return false
            }
            eglContext = context
            gl10 = context.gl as? GL10
            hasEglContext = true
            return true
        }

        private fun createEglSurface(surfaceTexture: SurfaceTexture): Boolean {
            val eglInstance = egl ?: return false
            val config = eglConfig ?: return false

            destroyEglSurfaceLocked()

            val surface = eglInstance.eglCreateWindowSurface(eglDisplay, config, surfaceTexture, null)
            if (surface == null || surface === EGL10.EGL_NO_SURFACE) {
                Log.e(TAG, "eglCreateWindowSurface failed: ${eglInstance.eglGetError()}")
                return false
            }

            if (!eglInstance.eglMakeCurrent(eglDisplay, surface, surface, eglContext)) {
                Log.e(TAG, "eglMakeCurrent failed: ${eglInstance.eglGetError()}")
                eglInstance.eglDestroySurface(eglDisplay, surface)
                return false
            }

            eglSurface = surface
            hasEglSurface = true
            return true
        }

        private fun destroyEglSurfaceLocked() {
            val eglInstance = egl ?: return
            if (eglSurface !== EGL10.EGL_NO_SURFACE) {
                eglInstance.eglMakeCurrent(
                    eglDisplay,
                    EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_CONTEXT
                )
                eglInstance.eglDestroySurface(eglDisplay, eglSurface)
                eglSurface = EGL10.EGL_NO_SURFACE
            }
            hasEglSurface = false
        }

        private fun destroyEglContextLocked() {
            val eglInstance = egl ?: return
            if (eglContext !== EGL10.EGL_NO_CONTEXT) {
                eglInstance.eglDestroyContext(eglDisplay, eglContext)
                eglContext = EGL10.EGL_NO_CONTEXT
            }
            if (eglDisplay !== EGL10.EGL_NO_DISPLAY) {
                eglInstance.eglTerminate(eglDisplay)
                eglDisplay = EGL10.EGL_NO_DISPLAY
            }
            gl10 = null
            hasEglContext = false
        }

        private fun chooseEglConfig(egl: EGL10, display: EGLDisplay): EGLConfig? {
            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfigs = IntArray(1)
            val configSpec = intArrayOf(
                EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_NONE
            )
            if (egl.eglChooseConfig(display, configSpec, configs, 1, numConfigs) && numConfigs[0] > 0) {
                return configs[0]
            }
            val fallbackSpec = intArrayOf(
                EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_NONE
            )
            if (egl.eglChooseConfig(display, fallbackSpec, configs, 1, numConfigs) && numConfigs[0] > 0) {
                return configs[0]
            }
            return null
        }

        companion object {
            private const val TAG = "GLTextureView"
            private const val EGL_CONTEXT_CLIENT_VERSION = 0x3098
            private const val EGL_OPENGL_ES2_BIT = 4
            private const val EGL11_CONTEXT_LOST = 0x300E
        }
    }
}
