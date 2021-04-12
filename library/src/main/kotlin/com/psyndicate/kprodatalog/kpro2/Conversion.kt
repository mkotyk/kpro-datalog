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
            6.6273765056066250e+003,
            -6.8783482947157768e+002,
            2.8868471169684934e+001,
            -6.0393233174253313e-001,
            6.2878907520772234e-003,
            -2.6056691376865053e-005
        )
    )

val Double.ectTempInverseConversion: Double
    get() = this.polyConvert(
        listOf(
            -3.2030343252358108e+005,
            2.1923453363890854e+004,
            -5.9917738904310590e+002,
            8.1761000362398679e+000,
            -5.5709077245462875e-002,
            1.5163690239535166e-004
        )
    )
