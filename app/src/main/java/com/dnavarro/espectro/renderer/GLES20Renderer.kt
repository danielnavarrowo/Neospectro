package com.dnavarro.espectro.renderer

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.graphics.Bitmap // Added
import android.graphics.Color  // Added
import android.opengl.GLUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import androidx.core.graphics.createBitmap
import kotlin.math.abs
import androidx.core.graphics.set

class GLES20Renderer(private val context: Context) : GLSurfaceView.Renderer {

    private val mSpectrumLogic = SpectrumLogic()
    private var mAudioViz: AudioViz? = null

    // Vertex Buffer: 1024 lines * 2 vertices * 4 floats (x,y,s,t)
    private val mPoints = FloatArray(SpectrumLogic.ARRAY_SIZE)
    private var mVertexBuffer: FloatBuffer

    // Matrices
    private val mMVPMatrix = FloatArray(16)
    private val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)

    // GL Handles
    private var mProgramHandle = 0
    private var mPositionHandle = 0
    private var mTexCoordHandle = 0
    private var mMVPMatrixHandle = 0
    private var mTextureUniformHandle = 0
    private var mTextureId = 0

    // Colors
    var mEdgeColor: Int = Color.rgb(3, 3, 255)
    var mCenterColor: Int = Color.WHITE

    private var mWidth = 0
    private var mHeight = 0

    // State
    private var mVisible = false
    private var mAudioEnabled = false

    init {
        // Initialize points X coordinates and Texture S coordinates once
        // as they don't change (only Y changes)
        // matches behavior in GenericWaveRS and Visualization3RS

        val outlen = SpectrumLogic.NUM_LINES // 1024
        val half = outlen / 2

        for (i in 0 until outlen) {
            val idx = i * 8
            // Vertex 1
            mPoints[idx] = (i - half).toFloat()      // x
            mPoints[idx + 1] = 0f                    // y (updated later)
            mPoints[idx + 2] = 0f                    // s
            mPoints[idx + 3] = 0f                    // t

            // Vertex 2
            mPoints[idx + 4] = (i - half).toFloat()  // x
            mPoints[idx + 5] = 0f                    // y (updated later)
            mPoints[idx + 6] = 1f                    // s
            mPoints[idx + 7] = 0f                    // t
        }

        // Initialize Buffer
        mVertexBuffer = ByteBuffer.allocateDirect(mPoints.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        mVertexBuffer.put(mPoints).position(0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)

        // Get Density for SpectrumLogic logic
        val density = context.resources.displayMetrics.density
        mSpectrumLogic.density = density

        // Load Texture from Color
        val bitmap = generateGradientBitmap(mEdgeColor, mCenterColor)
        mTextureId = loadTexture(bitmap)

        // Compile Shaders
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)

        mProgramHandle = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }

        mPositionHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_Position")
        mTexCoordHandle = GLES20.glGetAttribLocation(mProgramHandle, "a_TexCoordinate")
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_MVPMatrix")
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgramHandle, "u_Texture")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mWidth = width
        mHeight = height
        GLES20.glViewport(0, 0, width, height)

        // Setup Orthographic Projection centered at 0,0
        // Width covers -width/2 to width/2
        // Height covers -height/2 to height/2
        val halfW = width / 2f
        val halfH = height / 2f

        Matrix.orthoM(mProjectionMatrix, 0, -halfW, halfW, -halfH, halfH, -1f, 1f)

        // View Matrix - Scale content to fit screen width
        Matrix.setIdentityM(mViewMatrix, 0)
        val scaleX = width.toFloat() / SpectrumLogic.NUM_LINES
        Matrix.scaleM(mViewMatrix, 0, scaleX, 1f, 1f)

        // Calculate MVP
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        if (!mVisible) return

        // Update Logic
        updateWaveLogic()

        // Update Buffer
        mVertexBuffer.position(0)
        mVertexBuffer.put(mPoints)
        mVertexBuffer.position(0)

        // Draw
        GLES20.glUseProgram(mProgramHandle)

        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 4 * 4, mVertexBuffer)
        GLES20.glEnableVertexAttribArray(mPositionHandle)

        // Texture Coords start at offset 2 floats (8 bytes)
        mVertexBuffer.position(2)
        GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false, 4 * 4, mVertexBuffer)
        GLES20.glEnableVertexAttribArray(mTexCoordHandle)

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId)
        GLES20.glUniform1i(mTextureUniformHandle, 0)

        // Enable blending for transparency
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE) // Additive blending usually looks good for spectrum
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // Draw Triangle Strip
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mPoints.size / 4)

        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mTexCoordHandle)
    }

    private fun updateWaveLogic() {
        var data: IntArray? = null
        var len = 0

        if (mAudioEnabled && mAudioViz != null) {
            data = mAudioViz!!.getFormattedData(1, 1)
            len = data.size
        }


        var isIdle = true
        if (len > 0) {
             isIdle = false
        }

        if (isIdle) {
            mSpectrumLogic.updateIdle(mPoints)
        } else {
            // Always treat width as full lines for logic updates, scaling handles visual fit
            mSpectrumLogic.updateAudio(data!!, mPoints, SpectrumLogic.NUM_LINES, len)
        }
    }

    fun setVisible(visible: Boolean) {
        mVisible = visible
        if (visible) {
            if (mAudioEnabled) startAudio()
        } else {
            stopAudio()
        }
    }

    fun setAudioEnabled(enabled: Boolean) {
        val wasEnabled = mAudioEnabled
        mAudioEnabled = enabled
        if (enabled && !wasEnabled && mVisible) {
            startAudio()
        } else if (!enabled && wasEnabled) {
            stopAudio()
        }
    }

    private fun startAudio() {
        if (mAudioViz == null) {
            mAudioViz = AudioViz(AudioViz.TYPE_FFT, 512)
        }
        mAudioViz?.start()
    }

    private fun stopAudio() {
        mAudioViz?.stop()
        mAudioViz?.release()
        mAudioViz = null
    }

    fun updateTextureColor(edgeColor: Int, centerColor: Int) {
        mEdgeColor = edgeColor
        mCenterColor = centerColor
        if (mTextureId != 0) {
            val textures = IntArray(1)
            textures[0] = mTextureId
            GLES20.glDeleteTextures(1, textures, 0)
        }
        val bitmap = generateGradientBitmap(edgeColor, centerColor)
        mTextureId = loadTexture(bitmap)
    }

    private fun generateGradientBitmap(edgeColor: Int, centerColor: Int): Bitmap {
        val width = 64
        val height = 1
        val bitmap = createBitmap(width, height)

        val edgeR = Color.red(edgeColor)
        val edgeG = Color.green(edgeColor)
        val edgeB = Color.blue(edgeColor)

        val centerR = Color.red(centerColor)
        val centerG = Color.green(centerColor)
        val centerB = Color.blue(centerColor)

        for (i in 0 until width) {
            // Distance from center (0 to 1)
            // Center is 31.5. Edge 0 is 31.5 away. Edge 63 is 31.5 away.
            val distanceToCenter = abs(i - 31.5f) / 31.5f

            // Interpolate between Center and Edge
            // Val = center + (edge - center) * distanceToCenter

            val r = (centerR + (edgeR - centerR) * distanceToCenter).toInt().coerceIn(0, 255)
            val g = (centerG + (edgeG - centerG) * distanceToCenter).toInt().coerceIn(0, 255)
            val b = (centerB + (edgeB - centerB) * distanceToCenter).toInt().coerceIn(0, 255)

            bitmap[i, 0] = Color.rgb(r, g, b)
        }
        return bitmap
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    private fun loadTexture(bitmap: Bitmap): Int {
        val textureHandle = IntArray(1)
        GLES20.glGenTextures(1, textureHandle, 0)

        if (textureHandle[0] != 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

            bitmap.recycle()
        }

        return textureHandle[0]
    }

    companion object {
        // Simple pass-through position and texture coords
        private const val VERTEX_SHADER_CODE = """
            uniform mat4 u_MVPMatrix;
            attribute vec4 a_Position;
            attribute vec2 a_TexCoordinate;
            varying vec2 v_TexCoordinate;
            
            void main() {
                v_TexCoordinate = a_TexCoordinate;
                gl_Position = u_MVPMatrix * a_Position;
            }
        """

        // Simple texture sampling
        private const val FRAGMENT_SHADER_CODE = """
            precision mediump float;
            uniform sampler2D u_Texture;
            varying vec2 v_TexCoordinate;
            
            void main() {
                gl_FragColor = texture2D(u_Texture, v_TexCoordinate);
            }
        """

    }
}
