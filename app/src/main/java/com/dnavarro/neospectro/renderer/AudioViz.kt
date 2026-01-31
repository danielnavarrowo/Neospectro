package com.dnavarro.neospectro.renderer

import android.media.audiofx.Visualizer
import android.util.Log

class AudioViz(private val type: Int, size: Int) {

    private val mRawVizData: ByteArray
    private val mFormattedVizData: IntArray
    private val mFormattedNullData = IntArray(0)

    private var mVisualizer: Visualizer? = null

    private var mLastValidCaptureTimeMs: Long = 0

    companion object {
        const val TYPE_PCM = 0
        const val TYPE_FFT = 1
        private const val MAX_IDLE_TIME_MS = 1000L
        private const val TAG = "AudioViz"
    }

    init {
        var captureSize = size
        val range = Visualizer.getCaptureSizeRange()

        if (captureSize < range[0]) captureSize = range[0]
        if (captureSize > range[1]) captureSize = range[1]

        mRawVizData = ByteArray(captureSize)
        mFormattedVizData = IntArray(captureSize)

        try {
            mVisualizer = Visualizer(0).apply {
                enabled = false
                this.captureSize = mRawVizData.size
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Visualizer: ${e.message}")
        }
    }

    fun start() {
        if (mVisualizer == null) {
            try {
                mVisualizer = Visualizer(0).apply {
                    enabled = false
                    captureSize = mRawVizData.size
                }
            } catch (e: Exception) {
                Log.e(TAG, "AudioViz: Retrying init failed: ${e.message}")
            }
        }

        mVisualizer?.let {
            try {
                if (!it.enabled) {
                    it.enabled = true
                    mLastValidCaptureTimeMs = System.currentTimeMillis()
                }
            } catch (e: Exception) {
                Log.e(TAG, "start() failed: ${e.message}")
            }
        }
    }

    fun stop() {
        mVisualizer?.let {
            try {
                if (it.enabled) {
                    it.enabled = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "stop() failed: ${e.message}")
            }
        }
    }

    fun release() {
        mVisualizer?.release()
        mVisualizer = null
    }

    fun getFormattedData(num: Int, den: Int): IntArray {
        if (captureData()) {
            if (type == TYPE_PCM) {
                for (i in mFormattedVizData.indices) {
                    val tmp = (mRawVizData[i].toInt() and 0xFF) - 128
                    mFormattedVizData[i] = (tmp * num) / den
                }
            } else {
                for (i in mFormattedVizData.indices) {
                    mFormattedVizData[i] = (mRawVizData[i].toInt() * num) / den
                }
            }
            return mFormattedVizData
        } else {
            return mFormattedNullData
        }
    }

    private fun captureData(): Boolean {
        val viz = mVisualizer ?: return false
        var status: Int

        try {
            status = if (type == TYPE_PCM) {
                viz.getWaveForm(mRawVizData)
            } else {
                viz.getFft(mRawVizData)
            }
        } catch (e: Exception) {
             Log.e(TAG, "captureData() exception: ${e.message}")
             return false
        }

        if (status != Visualizer.SUCCESS) {
            return false
        }

        // Detect silence
        var i = 0
        val nullValue: Byte = if (type == TYPE_PCM) (-128).toByte() else 0

        while (i < mRawVizData.size) {
            if (mRawVizData[i] != nullValue) break
            i++
        }

        if (i == mRawVizData.size) {
            if (System.currentTimeMillis() - mLastValidCaptureTimeMs > MAX_IDLE_TIME_MS) {
                return false
            }
        } else {
            mLastValidCaptureTimeMs = System.currentTimeMillis()
        }

        return true
    }
}
