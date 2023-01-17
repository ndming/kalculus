package com.flyng.kalculus.io

import android.graphics.Path
import android.graphics.PathMeasure
import android.util.Log
import androidx.core.graphics.PathParser
import com.flyng.kalculus.BuildConfig
import com.flyng.kalculus.core.manager.CameraManager

object SvgSampler {
    fun fromSvgData(svg: SvgData): List<Pair<Float, Float>> {
        val path = PathParser.createPathFromPathData(svg.path)
        val matrix = android.graphics.Matrix()
        matrix.setTranslate(-svg.width / 2, -svg.height / 2)
        matrix.postRotate(180.0f)
        val scale = CameraManager.STANDARD_VIEW_PORT.toFloat() / (maxOf(svg.width, svg.height) / 2)
        matrix.postScale(-scale, scale)
        path.transform(matrix)

        return sampling(path)
    }

    private fun sampling(path: Path): List<Pair<Float, Float>> {
        val pm = PathMeasure(path, true)
        val length = pm.length

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "length = $length")
        }

        val incr = 1.0f / (length * SAMPLING_FACTOR)

        val samples = mutableListOf<Pair<Float, Float>>()
        var fraction = 0.0f

        val pos = FloatArray(2)
        val tan = FloatArray(2)

        while (fraction <= 1.0f) {
            pm.getPosTan(length * fraction, pos, tan)
            samples.add(pos[0] to pos[1])
            fraction += incr
        }

        pm.getPosTan(0.0f, pos, tan)
        samples.add(pos[0] to pos[1])

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "sampling size = ${samples.size}")
        }

        return samples
    }

    const val SAMPLING_FACTOR = 100

    private const val TAG = "SvgSampler"
}