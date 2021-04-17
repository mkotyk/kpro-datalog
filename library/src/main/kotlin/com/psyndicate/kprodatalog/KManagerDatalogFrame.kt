package com.psyndicate.kprodatalog

import com.psyndicate.kprodatalog.serialization.SerializedSize

@Suppress("ArrayInDataClass")
data class KManagerDatalogFrame(
    val frameNumber: Int = 0,               // Offset 0 (0h)
    val timeOffset: Int = 0,                // Offset 4 (4h)
    val RPM: Int = 0,                       // Offset 8  (8h)
    val padding0: Int = 0,                  // Offset 12 (Ch) - Always 0 to align at 8 bytes
    val speed: Double = 0.0,                // Offset 16 (10h)
    val map: Double = 0.0,                  // Offset 24 (18h)
    val clv: Double = 0.0,                  // Offset 32 (20h)
    val tps: Double = 0.0,                  // Offset 40 (28h)
    val camAngle: Double = 0.0,             // Offset 48 (30h)
    val targetCamAngle: Double = 0.0,       // Offset 56 (38h)
    val injectorDuration: Double = 0.0,     // Offset 64 (40h)
    val ignition: Double = 0.0,             // Offset 72 (48h)
    val IAT: Double = 0.0,                  // Offset 80 (50h)
    val ECT: Double = 0.0,                  // Offset 88 (58h)
    val unknown1: Int = 0,                  // Offset 96 (60h)
    val unknown2: Int = 0,                  // Offset 100 (64h)
    val unknown3: Int = 0,                  // Offset 104 (68h)
    val unknown4: Int = 0,                  // Offset 108 (6Ch)
    val unknown5: Int = 0,                  // Offset 112 (70h)
    val rvslck: Byte = 0,                   // Offset 116 (74h)
    val bksw: Byte = 0,                     // Offset 117 (75h)
    val acsw: Byte = 0,                     // Offset 118 (76h)
    val accl: Byte = 0,                     // Offset 119 (77h)
    val scs: Byte = 0,                      // Offset 120 (78h)
    val eps: Byte = 0,                      // Offset 121 (79h)
    val flr: Byte = 0,                      // Offset 122 (7Ah)
    val vtp: Byte = 0,                      // Offset 123 (7Bh)
    val vtc: Byte = 0,                      // Offset 124 (7Ch)
    val fanc: Byte = 0,                     // Offset 125 (7Dh)
    val mil: Byte = 0,                      // Offset 126 (7Eh)
    val blankFlag: Byte = 0,                // Offset 127 (7Fh)
    val o2Voltage: Double = 0.0,            // Offset 128 (80h)
    val o2mA: Double = 0.0,                 // Offset 136 (88h)
    val so2: Double = 0.0,                  // Offset 144 (90h)
    val lambda: Double = 0.0,               // Offset 152 (98h)
    val targetLambda: Double = 0.0,         // Offset 160 (A0h)
    val shortTermTrim: Double = 0.0,        // Offset 168 (A8h)
    val longTermTrim: Double = 0.0,         // Offset 176 (B0h)
    val fuelStatus: Int = 0,                // Offset 184 (B8h)
    val padding1: Int = 0,                  // Offset 188 (BCh) - Always 0 to align at 8 bytes
    val knockRetard: Double = 0.0,          // Offset 192 (C0h)
    val knockLevelVolts: Double = 0.0,      // Offset 200 (C8h)
    val knockThresholdVolts: Double = 0.0,  // Offset 208 (D0h)
    val knockCount: Int = 0,                // Offset 216 (D8h)
    val padding2: Int = 0,                  // Offset 220 (DCh)  - Always 0 to align at 8 bytes
    val AAP: Double = 0.0,                  // Offset 224 (E0h)
    val RTP: Double = 0.0,                  // Offset 232 (E8h)
    val batteryVoltage: Double = 0.0,       // Offset 240 (F0h)
    val eldAmps: Double = 0.0,              // Offset 248 (F8h)
    val n2o_arm1: Byte = 0,                 // Offset 256 (100h) in+20 & 0x10
    val n2o_on1: Byte = 0,                  // Offset 257 (101h) in+20 & 0x20
    val n2o_arm2: Byte = 0,                 // Offset 258 (102h) in+20 & 0x40
    val n2o_on2: Byte = 0,                  // Offset 259 (103h) in+20 & 0x80
    val n2o_arm3: Byte = 0,                 // Offset 260 (104h) in+2D & 0x1
    val n2o_on3: Byte = 0,                  // Offset 261 (105h) in+2D & 0x2
    val diagnostics1: Byte = 0,             // Offset 262 (106h)
    val diagnostics2: Byte = 0,             // Offset 263 (107h)
    @SerializedSize(20)
    val diagnosticCodes: ByteArray = ByteArray(20), // Offset: 264 (108h) - Copied from Datalog2, 20 bytes
    val gear: Int = 0,                      // Offset 284 (11Ch) -> in+23h
    val boostControllerDuty: Double = 0.0,  // Offset 288 (120h) -> in+24h
    val unknown7: Int = 0,                  // Offset 296 (128h) -> in+26h
    val padding3: Int = 0,                  // Offset 300 (12Ch)  - Always 0 to align at 8 bytes
    val unknown8: Double = 0.0,             // Offset 304 (130h) -> in+27h
    val tps2: Double = 0.0,                 // Offset 312 (138h) -> in+29h
    val eldVoltage: Double = 0.0,           // Offset 320 (140h) -> in+2Bh
    val datalogging: Byte = 0,              // Offset 328 (148h) -> in+2Dh
) {
    companion object {
         const val SIZE = 336
    }
}