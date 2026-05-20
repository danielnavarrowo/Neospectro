package com.dnavarro.neospectro.renderer

import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.pow
import kotlin.math.hypot

class SpectrumLogic {

    var density: Float = 1f // Added density scaling factor

    private val mAnalyzer = IntArray(512)

    // Waveform state
    private var wave1pos = 0
    private var wave1amp = 0
    private var wave2pos = 0
    private var wave2amp = 0
    private var wave3pos = 0
    private var wave3amp = 0
    private var wave4pos = 0
    private var wave4amp = 0

    companion object {
        const val NUM_LINES = 1024
        const val FLOATS_PER_VERTEX = 4
        const val VERTICES_PER_LINE = 2
        const val ARRAY_SIZE = NUM_LINES * VERTICES_PER_LINE * FLOATS_PER_VERTEX
    }

    fun updateIdle(points: FloatArray) {

        val amp1 = sin(0.007f * wave1amp) * 120
        val amp2 = sin(0.023f * wave2amp) * 80
        val amp3 = sin(0.011f * wave3amp) * 40
        val amp4 = sin(0.031f * wave4amp) * 20

        // Use density for minimum thickness
        val minThickness = 2f * density

        // Loop 1024 times
        for (i in 0 until NUM_LINES) {
            var `val` = abs(sin(0.013f * (wave1pos + i)) * amp1 + sin(0.029f * (wave2pos + i)) * amp2)
            val off = sin(0.005f * (wave3pos + i)) * amp3 + sin(0.017f * (wave4pos + i)) * amp4

            if (`val` < minThickness && `val` > -minThickness) `val` = minThickness

            // points structure: [x, y, s, t, x, y, s, t]
            // We update y at index 1 and 5
            points[i * 8 + 1] = `val` + off
            points[i * 8 + 5] = -`val` + off
        }

        wave1pos++
        wave1amp++
        wave2pos--
        wave2amp++
        wave3pos++
        wave3amp++
        wave4pos++
        wave4amp++

    }

    fun reset() {
        wave1pos = 0
        wave1amp = 0
        wave2pos = 0
        wave2amp = 0
        wave3pos = 0
        wave3amp = 0
        wave4pos = 0
        wave4amp = 0
    }

    fun updateAudio(vizData: IntArray, points: FloatArray, width: Int, lenInput: Int) {
        var len = lenInput
        // The really high frequencies aren't that interesting for music,
        // so just chop those off and use only the lower half of the spectrum
        len /= 2

        if (len == 0) return

        len /= 2

        if (len > mAnalyzer.size) len = mAnalyzer.size

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

            // Attack & Decay smoothing
            val attack = 0.4f
            val decay = 0.1f

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
