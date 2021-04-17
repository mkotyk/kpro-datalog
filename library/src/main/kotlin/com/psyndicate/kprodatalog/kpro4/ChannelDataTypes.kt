package com.psyndicate.kprodatalog.kpro4

enum class ChannelDataTypes(val data: Int) {
    // Channel data types
    CT_UNKNOWN(0x00),
    CT_BIT(0x01), // byte 0=off, 1=on
    CT_NUMBER(0x02), // byte or word
    CT_RPM(0x03), // word revs unit=r/min lsb=1
    CT_SPEED(0x04), // word (lsb=0.01 unit=kph)
    CT_MBAR(0x05), // word mbar pressure
    CT_KPA(0x06), // byte kpa pressure
    CT_TPS(0x07), // byte lsb=0.5% range -20 to 108%
    CT_INJ(0x08), // word lsb=0.001 ms
    CT_IGN(0x09), // also used for cam angle
    CT_RETARD(0x0B), // byte retard 0-64 lsb=0.25
    CT_TEMP(0x10), // byte lsb=1 offset=0 unit=oF
    CT_PCT(0x11), // byte percentage 0 to +100 lsb=128/100
    CT_PCT_SIGNED(0x12), // byte percentage -100 to +100 lsb=128/100
    CT_PCT_CHG(0x13), // word percentage uint=% 0 to 655.35% lsb=0.01%
    CT_MASSFLOW(0x16), // mg/s mass flow
    CT_5V(0x18), // byte 0-5 volts
    CT_19V(0x19), // byte range=6-18.8V lsb=0.05V unit=volts
    CT_LAMBDA(0x1E), //
    CT_BAR(0x20), // word lsb=1 unit=bar
    CT_MM(0x21), // wastegate distance unit=mm
    CT_GFORCE(0x22), // word
    CT_SIGNED(0x23), // byte or word signed
    CT_SIGNED100(0x24) // byte or word signed, fixed 2 dp
}