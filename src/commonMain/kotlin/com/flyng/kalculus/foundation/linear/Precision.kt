package com.flyng.kalculus.foundation.linear

import kotlin.math.pow
import kotlin.math.roundToInt

internal object Precision {
    /**
     * The default global floating-point tolerance for the application when considering floating-point equality.
     */
    const val DEFAULT = 4
}

/**
 * Rounds this [Float] to [digits] number of floating-point precision. The rounding attempt taken here is not always
 * perfect, but it is decent enough to, and should only to, check floating-point equality. All the classes dealing with
 * floating-point arithmetics, especially those using trig or exponential functions, should use this function to
 * round parameters before doing equality check.
 * @param digits the number of floating-point digits to keep when perform rounding.
 * @see Precision.DEFAULT
 */
internal fun Float.round(digits: Int = Precision.DEFAULT): Float {
    val shift = 10.0f.pow(digits)
    return (this * shift).roundToInt() / shift
}
