package com.psyndicate.kprodatalog.ftdi


enum class LineParity(val parity: Int) {
    NONE(0),
    ODD(1),
    EVEN(2),
    MARK(3),
    SPACE(4)
}
