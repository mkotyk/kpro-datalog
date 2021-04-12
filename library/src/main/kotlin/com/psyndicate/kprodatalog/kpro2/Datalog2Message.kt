package com.psyndicate.kprodatalog.kpro2

import com.psyndicate.kprodatalog.KManagerDatalogFrame

@ExperimentalUnsignedTypes
data class Datalog2Message(
    /*
        Offset: 2 (2h)
        Description: Engine Coolant temperature
        Conversion: extrapolated from curve
        Units: Degrees Celsius
    */
    val ECT: UByte = 0u,

    /*
        Offset: 3 (3h)
        Description: Intake air temperature
        Conversion: extrapolated from curve
        Units: Degrees Celsius
    */
    val IAT: UByte = 0u,

    /*
        Offset: 4 (4h)
        Description: Voltage
        Conversion: result * 0.1
        Units: Volts
    */
    val VLT: UByte = 0u,

    /*
        Offset:5 (5h)
        Description:	Electrical discharge
        Conversion: (result - 196.0) * 0.390625
        Units: Amps
    */
    val ELD: UByte = 0u,

    /*
        Offset: 6 (6h)
        Description: Absolute Atmospheric Pressure
        Conversion: result * 0.01
        Units: kPa
    */
    val AAP: UByte = 0u,

    /*
        Offset: 7 (7h)
        Description: Relative Tank Pressure
        Conversion: (result  - 128.0) * 0.0006529
        Units: kPa
    */
    val RTP: UByte = 0u,

    /*
        Offset: 8 (8h)
        Description: Long term fuel trim
        Conversion: (result - 128.0) / 128
        Units: %
    */
    val LTT: UByte = 0u,

    /*
        Offset: 9 (9h)
        Description: Fuel Status
        Enum:  Open loop, cold = 1, Closed loop = 2, Open loop = 4
    */
    val FST: UByte = 0u
) : Message {
    companion object {
        const val KPRO_OPEN_COLD = 1
        const val KPRO_CLOSED = 2
        const val KPRO_OPEN_DRIVING = 4
    }

    override val size: Int get() = 9
}

@ExperimentalUnsignedTypes
fun KManagerDatalogFrame.apply(message: Datalog2Message): KManagerDatalogFrame = this.copy(
    IAT = message.IAT.toDouble().iatTempConversion,
    ECT = message.ECT.toDouble().ectTempConversion,
    batteryVoltage = (message.VLT.toDouble() * 0.1).round(5),
    eldAmps = -(message.ELD.toDouble() - 196.0) * 0.390625,
    AAP = message.AAP.toDouble() * 0.01,
    RTP = (message.RTP.toDouble() - 128.0) * 0.0006529,
    longTermTrim = (message.LTT.toDouble() - 128.0) * 0.78125,
    fuelStatus = message.FST.toInt()
)

@ExperimentalUnsignedTypes
fun KManagerDatalogFrame.toDatalog2Message(): Datalog2Message = Datalog2Message(
    IAT = IAT.iatTempInverseConversion.toInt().toUByte(),
    ECT = ECT.ectTempInverseConversion.toInt().toUByte(),
    VLT = (batteryVoltage / 0.1).toInt().toUByte(),
    ELD = ((-eldAmps / 0.390625) + 196.0).toInt().toUByte(),
    AAP = (AAP / 0.01).toInt().toUByte(),
    RTP = ((RTP / 0.0006529) + 128.0).toInt().toUByte(),
    LTT = ((longTermTrim / 0.78125) + 128.0).toInt().toUByte(),
    FST = fuelStatus.toUByte()
)
