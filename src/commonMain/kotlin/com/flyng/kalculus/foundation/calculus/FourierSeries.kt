package com.flyng.kalculus.foundation.calculus

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

internal object FourierSeries {
    fun fromSamplesAt(samples: List<Pair<Float, Float>>, n: Int): Pair<Float, Float> {
        val step = if (samples.size < 2) 0.0f else 1.0f / (samples.size - 1)

        val absN = abs(n)

        var pn = 0.0f
        var qn = 0.0f
        var rn = 0.0f
        var sn = 0.0f
        samples.forEachIndexed { idx, (xt, yt) ->
            val co = cos(absN * 2 * PI * step * idx).toFloat() * step
            val si = sin(absN * 2 * PI * step * idx).toFloat() * step
            pn += xt * co
            qn += yt * co
            rn += xt * si
            sn += yt * si
        }

        return if (n < 0) {
            (pn - sn) to (qn + rn)
        } else {
            (pn + sn) to (qn - rn)
        }
    }
}