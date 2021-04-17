package com.psyndicate.kprodatalog.kpro2

import kotlin.math.pow
import kotlin.math.roundToInt

object Conversions {
    const val NORMAL = 0.00390625       // 1/256th
    const val VOLTS = NORMAL * 5.0      // 1/256 of 5.0 Volts
    const val PERCENT = NORMAL * 100    // 1/256 of 100 percent
    const val O2VOLTS = NORMAL * 1.25   // 1/256 of 1.25 Volts
    const val CAMDEG = NORMAL * 60.0    // 1/256 of 60.0 degrees
    const val HALF_SHORT = 32768.0      // Half of a 16bit "short int"
}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return (this * multiplier).roundToInt() / multiplier
}

fun Double.polyConvert(terms: List<Double>): Double = terms.mapIndexed { index, term -> term * this.pow(index) }.sum()

val Double.iatTempConversion: Double
    get() = this.polyConvert(
        listOf(
            8.7484375000000000e+001,
            -4.2187500000000000e-001
        )
    )

val Double.iatTempInverseConversion: Double
    get() = this.polyConvert(
        listOf(
            2.0737037037037038e+002,
            -2.3703703703703702e+000,
        )
    )

val Double.ectTempConversion: Double
    get() = this.polyConvert(
        listOf(
            1.2012932677355192e+002,
            -1.1912497349228761e+000,
            5.1833341961054673e-003,
            -1.0640590426890055e-005
        )
    )

val Double.ectTempInverseConversion: Double
    get() = this.polyConvert(
        listOf(
            2.0996060570938579e+002,
            -2.4532086905728314e+000,
            -1.4632564149589432e-002,
            3.6449222676847640e-004,
            -1.6141195870796833e-006
        )
    )
