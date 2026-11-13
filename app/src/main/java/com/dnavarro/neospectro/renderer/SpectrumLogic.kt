package com.dnavarro.neospectro.renderer

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin

class SpectrumLogic {

    var density: Float = 1f // Added density scaling factor

    private val mAnalyzer = IntArray(512)

    // Waveform state (normalized to 60 steps per second)
    private var wave1pos = 0.0
    private var wave1amp = 0.0
    private var wave2pos = 0.0
    private var wave2amp = 0.0
    private var wave3pos = 0.0
    private var wave3amp = 0.0
    private var wave4pos = 0.0
    private var wave4amp = 0.0

    private var lastIdleTimeNs = 0L
    private var lastAudioTimeNs = 0L

    companion object {
        const val NUM_LINES = 1024
        const val FLOATS_PER_VERTEX = 4
        const val VERTICES_PER_LINE = 2
        const val ARRAY_SIZE = NUM_LINES * VERTICES_PER_LINE * FLOATS_PER_VERTEX
        private const val TARGET_FPS = 60.0
        private const val MAX_FRAME_DELTA_SECONDS = 0.1
        private const val TWO_PI = 2.0 * PI
    }

    fun updateIdle(points: FloatArray) {
        val nowNs = System.nanoTime()
        val step = if (lastIdleTimeNs == 0L) {
            0.0
        } else {
            val dtSeconds = ((nowNs - lastIdleTimeNs) / 1_000_000_000.0)
                .coerceIn(0.0, MAX_FRAME_DELTA_SECONDS)
            dtSeconds * TARGET_FPS
        }
        lastIdleTimeNs = nowNs
        lastAudioTimeNs = 0L

        wave1pos += step
        wave1amp += step
        wave2pos -= step
        wave2amp += step
        wave3pos += step
        wave3amp += step
        wave4pos += step
        wave4amp += step

        val amp1 = sin((0.007 * wave1amp) % TWO_PI).toFloat() * 120f
        val amp2 = sin((0.023 * wave2amp) % TWO_PI).toFloat() * 80f
        val amp3 = sin((0.011 * wave3amp) % TWO_PI).toFloat() * 40f
        val amp4 = sin((0.031 * wave4amp) % TWO_PI).toFloat() * 20f

        val phase1 = ((0.013 * wave1pos) % TWO_PI).toFloat()
        val phase2 = ((0.029 * wave2pos) % TWO_PI).toFloat()
        val phase3 = ((0.005 * wave3pos) % TWO_PI).toFloat()
        val phase4 = ((0.017 * wave4pos) % TWO_PI).toFloat()

        // Use density for minimum thickness
        val minThickness = 2f * density

        // Loop 1024 times
        for (i in 0 until NUM_LINES) {
            var `val` = abs(sin(phase1 + 0.013f * i) * amp1 + sin(phase2 + 0.029f * i) * amp2)
            val off = sin(phase3 + 0.005f * i) * amp3 + sin(phase4 + 0.017f * i) * amp4

            if (`val` < minThickness && `val` > -minThickness) `val` = minThickness

            // points structure: [x, y, s, t, x, y, s, t]
            // We update y at index 1 and 5
            points[i * 8 + 1] = `val` + off
            points[i * 8 + 5] = -`val` + off
        }
    }

    fun reset() {
        wave1pos = 0.0
        wave1amp = 0.0
        wave2pos = 0.0
        wave2amp = 0.0
        wave3pos = 0.0
        wave3amp = 0.0
        wave4pos = 0.0
        wave4amp = 0.0
        lastIdleTimeNs = 0L
        lastAudioTimeNs = 0L
    }

    fun updateAudio(vizData: IntArray, points: FloatArray, width: Int, lenInput: Int) {
        val nowNs = System.nanoTime()
        val frameScale = if (lastAudioTimeNs == 0L) {
            1.0f
        } else {
            val dtSeconds = ((nowNs - lastAudioTimeNs) / 1_000_000_000.0)
                .coerceIn(0.0, MAX_FRAME_DELTA_SECONDS)
            (dtSeconds * TARGET_FPS).toFloat().coerceIn(0.1f, 3.0f)
        }
        lastAudioTimeNs = nowNs
        lastIdleTimeNs = 0L

        var len = lenInput
        // The really high frequencies aren't that interesting for music,
        // so just chop those off and use only the lower half of the spectrum
        len /= 2

        if (len == 0) return

        len /= 2

        if (len > mAnalyzer.size) len = mAnalyzer.size

        // Attack & Decay smoothing normalized to 60fps time constant
        val attack = 1f - (1f - 0.4f).pow(frameScale)
        val decay = 1f - (1f - 0.1f).pow(frameScale)

        // Update mAnalyzer
        for (i in 1 until len - 1) {
            val val1 = vizData[i * 2]
            val val2 = vizData[i * 2 + 1]

            // Use amplitude (hypotenuse) instead of squared magnitude to prevent massive bass spikes
            val amplitude = hypot(val1.toDouble(), val2.toDouble()).toFloat()

            // Add progressive weight to boost high frequencies, balancing the bass-heavy nature of music
            val weight = 1.0f + (i.toFloat() / len) * 4.0f

            val targetVal = (amplitude * weight * 35f).toInt()
            val oldval = mAnalyzer[i]

            var newval = if (targetVal > oldval) {
                (oldval * (1 - attack) + targetVal * attack).toInt()
            } else {
                (oldval * (1 - decay) + targetVal * decay).toInt()
            }

            if (newval < 0) newval = 0

            mAnalyzer[i] = newval
        }

        // Distribute data over points with Interpolation
        val outlen = NUM_LINES // 1024
        val spreadWidth = if (width > outlen) outlen else width
        val skip = (outlen - spreadWidth) / 2

        val numBars = 64
        val barWidth = spreadWidth / numBars

        val minIdx = 1.0
        val maxIdx = (len - 2).coerceAtLeast(1).toDouble()
        val ratio = maxIdx / minIdx

        for (i in 0 until spreadWidth) {
            val barIndex = i / barWidth

            val pow = barIndex.toDouble() / (numBars - 1).coerceAtLeast(1).toDouble()
            val exactIdx = minIdx * ratio.pow(pow)

            val idx = exactIdx.toInt().coerceIn(1, len - 2)
            val frac = (exactIdx - idx).toFloat()

            val v1 = mAnalyzer[idx]
            val nextIdx = (idx + 1).coerceAtMost(len - 2)
            val v2 = mAnalyzer[nextIdx]

            val interpolatedVal = v1 + (v2 - v1) * frac

            var `val` = interpolatedVal / 8f
            val minThickness = 1f * density

            val posInBar = i % barWidth
            if (posInBar == 0 || posInBar == barWidth - 1) {
                `val` = minThickness
            } else {
                if (`val` < minThickness && `val` > -minThickness) `val` = minThickness
            }

            val pointIdx = (i + skip) * 8
            if (pointIdx + 5 < points.size) {
                points[pointIdx + 1] = `val`
                points[pointIdx + 5] = -`val`
            }
        }
    }
}
