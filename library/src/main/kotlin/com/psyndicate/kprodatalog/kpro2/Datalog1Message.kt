package com.psyndicate.kprodatalog.kpro2

import com.psyndicate.kprodatalog.KManagerDatalogFrame
import com.psyndicate.kprodatalog.kpro2.Conversions.CAMDEG
import com.psyndicate.kprodatalog.kpro2.Conversions.HALF_SHORT
import com.psyndicate.kprodatalog.kpro2.Conversions.O2VOLTS
import com.psyndicate.kprodatalog.kpro2.Conversions.PERCENT
import com.psyndicate.kprodatalog.kpro2.Conversions.VOLTS
import kotlin.math.max

@ExperimentalUnsignedTypes
data class Datalog1Message(
    /*
        Offset: 2 (2h)
	    Description: Crank Revolutions Per Minute
	    Conversion: result * 0.25
	    Units:RPM
	*/
    val RPM: UShort = 0u,

    /*
        Offset: 4 (4h)
        Description: Vehicle Speed Sensor
        Conversion: None
        Units: Km/h
    */
    val VSS: UByte = 0u,

    /*
        Offset: 5 (5h)
        Description: Throttle position sensor
        Conversion: ((result * 0.00390625 * 5.0) - 0.45) * 25 + 0.5
        Units: Percent
    */
    val TPS: UByte = 0u,

    /*
        Offset: 6 (6h)
        Description: MAP value
        Conversion: result * 0.01
        Units: kPa
        Notes:  If MAP2 is 0, use MAP1
    */
    val MAP1: UByte = 0u,

    /*
        Offset: 7 (7h)
        Description: Calculated Load Value
        Conversion: result * 0.00390625  * 100
        Units: % of possible load
    */
    val CLV: UByte = 0u,

    /*
        Offset: 8 (8h)
        Description: Cam angle
        Conversion: result * 0.5 - 20.0
        Units: Degrees
    */
    val CAM: UByte = 0u,

    /*
        Offset: 9 (9h)
        Description: Target cam angle
        Conversion: max(result * 0.00390625 * 60.0 , 0.0)
        Units: Degrees
    */
    val TCM: UByte = 0u,

    /*
        Offset: 10 (Ah)
        Notes: Not used for datalog
    */
    override var checksum: Byte = 0,

    /*
        Offset: 11 (Bh)
        Description: Injector pulse time
        Conversion: result * 0.004
        Units: ms
    */
    val INJ: UShort = 0u,

    /*
        Offset: 13 (Dh)
        Description: Ignition Advance
        Conversion: result * 0.5 - 64.0
        Units: Degrees relative to TDC
    */
    val IGN: UByte = 0u,

    /*
        Offset: 14 (Eh)
        Description: O2 Sensor Voltage
        Conversion:
            volts (80h) = result * 1.25 * 0.00390625
            current (88h) = 0.05 * volts - 0.037
     */
    val O2V: UByte = 0u,

    /*
        Offset: 15 (Fh)
        Description: Secondary O2 Sensor Volts
        Conversion: result * 0.00390625 * 5.0
        Units: Volts
    */
    val SO2: UByte = 0u,

    /*
        Offset: 16 (10h)
        Description: Primary O2 Sensor
        Conversion: if (result == 0.0) 0.0 else 32768.0 / result
        Units: :1 Stoich
    */
    val PO2: UShort = 0u,

    /*
        Offset: 18 (12h)
        Description: Targetted air fuel ratio
        Conversion: if (result == 0.0) 0.0 else 32768.0 /result
        Units: :1 Stoich
    */
    val TGT: UShort = 0u,

    /*
        Offset: 20 (14h)
        Description: Short term fuel trim
        Conversion: (result - 128.0) / 128
        Units: %
    */
    val STT: UByte = 0u,

    /*
        Offset: 21 (15h)
        Description: Knock retard ignition
        Conversion: result * 0.25
        Units: Degrees
     */
    val KNR: UByte = 0u,

    /*
        Offset: 22 (16h)
        Description: Knock level
        Conversion: result * 0.00390625 * 5.0
        Units: Volts
      */
    val KNL: UByte = 0u,

    /*
        Offset: 23 (17h)
        Description: Knock threshold
        Conversion: result * 0.00390625 * 5.0
        Units: Volts
    */
    val KNT: UByte = 0u,

    /*
        Offset: 24 (18h)
        Description: Knock count
        Conversion: None
        Units: Number of detected occurances
    */
    val KNC: UShort = 0u,

    /*
        Offset: 26 (1Ah)
        Description: Unknown - possibly table cell index
        Conversion: None
     */
    val unknown1: UByte = 0u,

    /*
        Offset: 27 (1Bh)
        Description: Unknown - possibly table cell index
        Conversion: None
     */
    val unknown2: UByte = 0u,

    /*
        Offset: 28 (1Ch)
        Description: Unknown - possibly table cell index
        Conversion: None
     */
    val unknown3: UByte = 0u,

    /*
        Offset: 29 (1Dh)
        Description: Unknown - possibly table cell index
        Conversion: None
    */
    val unknown4: UByte = 0u,

    /*
        Offset: 30 (1Eh)
        Description: Unknown - possibly table cell index
        Conversion: None
    */
    val unknown5: UByte = 0u,

    /*
        Offset: 31 (1Fh)
        Description: Switch status
        Conversion: Each bit mapped into its own byte
        Units:
          0x01 = RVSLCK
          0x02 = BKSW
          0x04 = ACSW
          0x08 = ACCL
          0x10 = SCS
          0x20 = EPS
          0x40 = FLR
          0x80 = FANC
    */
    val SWT: UByte = 0u,

    /*
        Offest: 32 (20h)
        Description: VTEC Status and N2O bits for 1 and 2
        Conversion: Each bit mapped into its own byte
        Units:
          0x01 = VTP
          0x02 = VTS
          0x10 = N2O Arm 1
          0x20 = N2O On 1
          0x40 = N2O Arm 2
          0x80 = N2O On 2
   */
    val VTC: UByte = 0u,

    /*
        Offset: 33 (21h)
        Description: Manifold air pressure
        Conversion: result * 0.001
        Units: Kpa
        See logic at MAP1
    */
    val MAP2: UShort = 0u,

    /*
        Offset: 35 (23h)
        Description: Gear transmission is in (calculated)
        Conversion: None
    */
    val GEAR: UByte = 0u,

    /*
        Offset: 36 (24h)
        Description: Boost Controller Duty
        Conversion: result * 0.00244140625 + 0.5
    */
    val BCD: UShort = 0u,

    /*
        Offset: 38 (26h)
        Description: Unknown - Rises with RPM, Oil pressure?
        Conversion: None
    */
    val unknown7: UByte = 0u,

    /*
        Offset: 39 (27h)
        Description: Unknown
        Conversion: result * 0.25 - 27.0
    */
    val unknown8: UShort = 0u,

    /*
        Offset: 41 (29h)
        Description: TPS2
        Conversion: (result - 5760) * 100.0 * 0.000019
    */
    val TPS2: UShort = 0u,

    /*
        Offset: 43 (2Bh)
        Description: Electrical Discharge Sensor
        Conversion: result * 0.000015258789 * 5.0
        Units: Volts
    */
    val ELDV: UShort = 0u,

    /*
        Offset: 45 (2Dh)
        Description: Flags for N2O #3 and Onboard data logging active
        Conversion: Each bit is mapped to its own byte
        Units:
          0x01 = N2O Arm 3
          0x02 = N2O On 3
          0x04 = Onboard data logging active
     */
    val otherFlags: UByte = 0u
) : Message {
    companion object {
        // SWT bits
        const val SW_RVSLCK = 0x01
        const val SW_BKSW = 0x02
        const val SW_ACSW = 0x04
        const val SW_ACCL = 0x08
        const val SW_SCS = 0x10
        const val SW_EPS = 0x20
        const val SW_FLR = 0x40
        const val SW_FANC = 0x80

        // VTC bits
        const val SW_VTP = 0x01
        const val SW_VTS = 0x02
        const val N2O_1_ARM = 0x10
        const val N2O_1_ON = 0x20
        const val N2O_2_ARM = 0x40
        const val N2O_2_ON = 0x80

        // Other Flag bits
        const val N2O_3_ARM = 0x01
        const val N2O_3_ON = 0x02
        const val DATALOGGING_ACTIVE = 0x04

        const val MSG_SIZE = 45
    }

    override val size: Int get() = MSG_SIZE
    override val id: Byte get() = MessageType.Datalog1.id
}

@ExperimentalUnsignedTypes
fun KManagerDatalogFrame.apply(message: Datalog1Message): KManagerDatalogFrame = this.copy(
    RPM = (message.RPM.toDouble() * 0.25).toInt(),
    speed = message.VSS.toDouble(),
    tps = (((message.TPS.toDouble() * VOLTS) - 0.45) * 25 + 0.5).toInt().toDouble(),
    map = if (message.MAP2.toInt() == 0) {
        message.MAP1.toDouble() * 0.01
    } else {
        (message.MAP2.toDouble() * 0.001).round(4)
    },
    clv = message.CLV.toDouble() * PERCENT,
    camAngle = message.CAM.toDouble() * 0.5 - 20.0,
    targetCamAngle = max(message.TCM.toDouble() * CAMDEG, 0.0),
    injectorDuration = (message.INJ.toDouble() * 0.004).round(4),
    ignition = (message.IGN.toDouble() * 0.5 - 64.0).round(4),
    o2Voltage = message.O2V.toDouble() * O2VOLTS,
    o2mA = (message.O2V.toDouble() * O2VOLTS) * 0.05 - 0.037,
    so2 = message.SO2.toDouble() * VOLTS,
    lambda = if (message.PO2.toInt() == 0) 0.0 else HALF_SHORT / message.PO2.toDouble(),
    targetLambda = if (message.TGT.toInt() == 0) 0.0 else HALF_SHORT / message.TGT.toDouble(),
    shortTermTrim = (message.STT.toDouble() - 128.0) * 0.78125,
    knockRetard = message.KNR.toDouble() * 0.25,
    knockLevelVolts = message.KNL.toDouble() * VOLTS,
    knockThresholdVolts = message.KNT.toDouble() * VOLTS,
    knockCount = message.KNC.toInt(),
    unknown1 = message.unknown1.toInt(),
    unknown2 = message.unknown2.toInt(),
    unknown3 = message.unknown3.toInt(),
    unknown4 = message.unknown4.toInt(),
    unknown5 = message.unknown5.toInt(),
    rvslck = if (message.SWT.toInt() and Datalog1Message.SW_RVSLCK != 0) 1 else 0,
    bksw = if (message.SWT.toInt() and Datalog1Message.SW_BKSW != 0) 1 else 0,
    acsw = if (message.SWT.toInt() and Datalog1Message.SW_ACSW != 0) 1 else 0,
    accl = if (message.SWT.toInt() and Datalog1Message.SW_ACCL != 0) 1 else 0,
    scs = if (message.SWT.toInt() and Datalog1Message.SW_SCS != 0) 1 else 0,
    eps = if (message.SWT.toInt() and Datalog1Message.SW_EPS != 0) 1 else 0,
    flr = if (message.SWT.toInt() and Datalog1Message.SW_FLR != 0) 1 else 0,
    fanc = if (message.SWT.toInt() and Datalog1Message.SW_FANC != 0) 1 else 0,
    vtc = if (message.VTC.toInt() and Datalog1Message.SW_VTS != 0) 1 else 0,
    vtp = if (message.VTC.toInt() and Datalog1Message.SW_VTP != 0) 1 else 0,
    n2o_arm1 = if (message.VTC.toInt() and Datalog1Message.N2O_1_ARM != 0) 1 else 0,
    n2o_on1 = if (message.VTC.toInt() and Datalog1Message.N2O_1_ON != 0) 1 else 0,
    n2o_arm2 = if (message.VTC.toInt() and Datalog1Message.N2O_2_ARM != 0) 1 else 0,
    n2o_on2 = if (message.VTC.toInt() and Datalog1Message.N2O_2_ON != 0) 1 else 0,
    gear = message.GEAR.toInt(),
    boostControllerDuty = message.BCD.toDouble() * 0.00244140625 + 0.5,
    unknown7 = message.unknown7.toInt(),
    unknown8 = message.unknown8.toDouble() * 0.25 - 27.0,
    tps2 = (message.TPS2.toDouble() - 5760.0) * 100.0 * 0.000019,
    eldVoltage = message.ELDV.toDouble() * 0.000015258789 * 5.0,
    n2o_arm3 = if (message.otherFlags.toInt() and Datalog1Message.N2O_3_ARM != 0) 1 else 0,
    n2o_on3 = if (message.otherFlags.toInt() and Datalog1Message.N2O_3_ON != 0) 1 else 0,
    datalogging = if (message.otherFlags.toInt() and Datalog1Message.DATALOGGING_ACTIVE != 0) 1 else 0,
)

@ExperimentalUnsignedTypes
fun KManagerDatalogFrame.toDatalog1Message(): Datalog1Message = Datalog1Message(
    RPM = (this.RPM / 0.25).toInt().toUShort(),
    VSS = speed.toInt().toUByte(),
    TPS = ((((tps - 0.5) / 25) + 0.45) / VOLTS).toInt().toUByte(),
    MAP1 = 0u, // Only set MAP2 as it's more precise
    MAP2 = (map / 0.001).toInt().toUShort(),
    CLV = (clv / PERCENT).toInt().toUByte(),
    CAM = ((camAngle + 20.0) / 0.5).toInt().toUByte(),
    TCM = (targetCamAngle / CAMDEG).toInt().toUByte(),
    INJ = (injectorDuration / 0.004).toInt().toUShort(),
    IGN = ((ignition + 64.0) / 0.5).toInt().toUByte(),
    O2V = (o2Voltage / O2VOLTS).toInt().toUByte(),
    SO2 = (so2 / VOLTS).toInt().toUByte(),
    PO2 = ((1.0 / lambda) * HALF_SHORT).toInt().toUShort(),
    TGT = ((1.0 / targetLambda) * HALF_SHORT).toInt().toUShort(),
    STT = (shortTermTrim / 0.78125 + 128.0).toInt().toUByte(),
    KNR = (knockRetard / 0.25).toInt().toUByte(),
    KNL = (knockLevelVolts / VOLTS).toInt().toUByte(),
    KNT = (knockThresholdVolts / VOLTS).toInt().toUByte(),
    KNC = knockCount.toUShort(),
    unknown1 = unknown1.toUByte(),
    unknown2 = unknown2.toUByte(),
    unknown3 = unknown3.toUByte(),
    unknown4 = unknown4.toUByte(),
    unknown5 = unknown5.toUByte(),
    SWT = let {
        (if (rvslck > 0) Datalog1Message.SW_RVSLCK else 0) +
                (if (bksw > 0) Datalog1Message.SW_BKSW else 0) +
                (if (acsw > 0) Datalog1Message.SW_ACSW else 0) +
                (if (accl > 0) Datalog1Message.SW_ACCL else 0) +
                (if (scs > 0) Datalog1Message.SW_SCS else 0) +
                (if (eps > 0) Datalog1Message.SW_EPS else 0) +
                (if (flr > 0) Datalog1Message.SW_FLR else 0) +
                (if (fanc > 0) Datalog1Message.SW_FANC else 0)
    }.toUByte(),
    VTC = let {
        (if (vtc > 0) Datalog1Message.SW_VTS else 0) +
                (if (vtp > 0) Datalog1Message.SW_VTP else 0) +
                (if (n2o_arm1 > 0) Datalog1Message.N2O_1_ARM else 0) +
                (if (n2o_on1 > 0) Datalog1Message.N2O_1_ON else 0) +
                (if (n2o_arm2 > 0) Datalog1Message.N2O_2_ARM else 0) +
                (if (n2o_on2 > 0) Datalog1Message.N2O_2_ON else 0)
    }.toUByte(),
    GEAR = gear.toUByte(),
    BCD = ((boostControllerDuty - 0.5) / 0.00244140625).toInt().toUShort(),
    unknown7 = unknown7.toUByte(),
    unknown8 = ((unknown8 + 27.0) / 0.25).toInt().toUShort(),
    TPS2 = (tps2 / (100.0 * 0.000019) + 5760.0).toInt().toUShort(),
    ELDV = (eldVoltage / (0.000015258789 * 5.0)).toInt().toUShort(),
    otherFlags = let {
        (if (n2o_arm3 > 0) Datalog1Message.N2O_3_ARM else 0) +
                (if (n2o_on3 > 0) Datalog1Message.N2O_3_ON else 0) +
                (if (datalogging > 0) Datalog1Message.DATALOGGING_ACTIVE else 0)
    }.toUByte(),
)