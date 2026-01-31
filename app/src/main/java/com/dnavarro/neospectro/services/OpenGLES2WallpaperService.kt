package com.dnavarro.neospectro.services

import android.content.Context
import android.opengl.GLSurfaceView
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder

abstract class OpenGLES2WallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return GLEngine()
    }

    open inner class GLEngine : Engine() {

        private var glSurfaceView: WallpaperGLSurfaceView? = null
        private var renderer: GLSurfaceView.Renderer? = null

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            glSurfaceView = WallpaperGLSurfaceView(this@OpenGLES2WallpaperService)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                glSurfaceView?.onResume()
            } else {
                glSurfaceView?.onPause()
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            glSurfaceView?.onWallpaperDestroy()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            glSurfaceView?.surfaceChanged(holder, format, width, height)
        }

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            glSurfaceView?.surfaceCreated(holder)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            glSurfaceView?.surfaceDestroyed(holder)
        }

        fun setRenderer(renderer: GLSurfaceView.Renderer) {
            this.renderer = renderer
            glSurfaceView?.setRenderer(renderer)
            glSurfaceView?.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }

        fun queueEvent(r: Runnable) {
            glSurfaceView?.queueEvent(r)
        }

        inner class WallpaperGLSurfaceView(context: Context) : GLSurfaceView(context) {
            init {
                setEGLContextClientVersion(2)
            }

            override fun getHolder(): SurfaceHolder {
                return this@GLEngine.surfaceHolder
            }

            fun onWallpaperDestroy() {
                super.onDetachedFromWindow()
            }
        }
    }
}
