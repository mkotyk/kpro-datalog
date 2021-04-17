package com.psyndicate.kprodatalog.kpro2

enum class MessageType(val id: Byte) {
    Status(0x40),
    Datalog1(0x60),
    Datalog2(0x61),
    Datalog3(0x62)
}