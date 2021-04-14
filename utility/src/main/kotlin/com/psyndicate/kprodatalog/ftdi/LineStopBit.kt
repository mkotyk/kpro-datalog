package com.psyndicate.kprodatalog.ftdi


enum class LineStopbit(val stopbit: Int) {
    STOP_BIT_1(0),
    STOP_BIT_15(1),
    STOP_BIT_2(2);
}
