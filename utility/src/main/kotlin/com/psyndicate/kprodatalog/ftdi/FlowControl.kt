package com.psyndicate.kprodatalog.ftdi

enum class FlowControl(val flowControl: Int) {
    DISABLE_FLOW_CTRL(0x0),
    RTS_CTS_HS(0x1 shl 8),
    DTR_DSR_HS(0x2 shl 8),
    XON_XOFF_HS(0x4 shl 8)
}