package com.psyndicate.kprodatalog.ftdi

import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream
import javax.usb3.IUsbDevice
import javax.usb3.IUsbInterface
import javax.usb3.UsbHostManager
import javax.usb3.enumerated.EEndpointDirection
import javax.usb3.request.BMRequestType

class FTDI(private val usbDevice: IUsbDevice) : Closeable {
    companion object {
        const val FTDI_VENDOR_ID: Int = 0x0403
        val KNOWN_PRODUCT_IDS = listOf(24577, 24592, 24593, 0xF5F8)
        const val DEFAULT_BAUD_RATE = 115200
        const val MODEM_STATUS_HEADER_LENGTH = 2
        val FTDI_USB_CONFIGURATION_WRITE = BMRequestType(
            EEndpointDirection.HOST_TO_DEVICE,
            BMRequestType.EType.VENDOR,
            BMRequestType.ERecipient.DEVICE
        ).byteCode

        val FTDI_USB_CONFIGURATION_READ = BMRequestType(
            EEndpointDirection.DEVICE_TO_HOST,
            BMRequestType.EType.VENDOR,
            BMRequestType.ERecipient.DEVICE
        ).byteCode

        const val SIO_RESET_REQUEST: Byte = 0
        const val SIO_SET_MODEM_CTRL_REQUEST: Byte = 0x01
        const val SIO_SET_FLOW_CTRL_REQUEST: Byte = 0x02
        const val SIO_SET_BAUDRATE_REQUEST: Byte = 0x03
        const val SIO_SET_DATA_REQUEST: Byte = 0x04
        const val SIO_POLL_MODEM_STATUS_REQUEST: Byte = 0x05
        const val SIO_SET_EVENT_CHAR_REQUEST: Byte = 0x06
        const val SIO_SET_ERROR_CHAR_REQUEST: Byte = 0x07
        const val SIO_SET_LATENCY_TIMER_REQUEST: Byte = 0x09
        const val SIO_GET_LATENCY_TIMER_REQUEST: Byte = 0x0A
        const val SIO_SET_DTR_MASK: Byte = 1
        const val SIO_SET_DTR_HIGH: Byte = 1
        const val SIO_SET_DTR_LOW: Byte = 0
        const val SIO_SET_RTS_MASK: Byte = 2
        const val SIO_SET_RTS_HIGH: Byte = 2
        const val SIO_SET_RTS_LOW: Byte = 0
        const val SIO_RESET_SIO: Byte = 0
        const val SIO_RESET_PURGE_RX: Short = 1
        const val SIO_RESET_PURGE_TX: Short = 2

        fun findFTDIDevices(
            vendorId: Int = FTDI_VENDOR_ID,
            productIds: List<Int> = KNOWN_PRODUCT_IDS
        ): Collection<IUsbDevice> {
            return UsbHostManager.getUsbDeviceList(vendorId.toShort(), productIds.map { it.toShort() })
        }
    }

    private val usbInterface: IUsbInterface = usbDevice.activeUsbConfiguration.usbInterfaces.iterator().next()

    val inputStream: UsbBufferedInputStream by lazy {
        val inputPipe = usbInterface.usbEndpoints.first { it.direction == EEndpointDirection.DEVICE_TO_HOST }.usbPipe
        inputPipe.open()
        UsbBufferedInputStream(inputPipe)
    }

    val outputStream: OutputStream by lazy {
        val outputPipe = usbInterface.usbEndpoints.first { it.direction == EEndpointDirection.HOST_TO_DEVICE }.usbPipe
        outputPipe.open()
        object : OutputStream() {
            override fun write(data: ByteArray) {
                outputPipe.syncSubmit(data)
            }

            override fun write(data: Int) {
                val buffer = byteArrayOf(data.toByte())
                write(buffer)
            }
        }
    }
    private val deviceIndex: Short = 0

    init {
        usbInterface.claim { true }
    }

    fun configureSerialPort(
        requestedBaudRate: Int,
        bits: LineDatabit,
        stopbits: LineStopbit,
        parity: LineParity,
        flowControl: FlowControl
    ) {
        setBaudRate(requestedBaudRate)
        setLineProperty(bits, stopbits, parity)
        setFlowControl(flowControl)
    }

    fun setBaudRate(baudRate: Int) {
        usbDevice.syncSubmit(
            usbDevice.createUsbControlIrp(
                FTDI_USB_CONFIGURATION_WRITE,
                SIO_SET_BAUDRATE_REQUEST,
                calculateBaudRate(baudRate),
                deviceIndex
            )
        )
    }

    fun setDTR(state: Boolean) {
        usbDevice.syncSubmit(
            usbDevice.createUsbControlIrp(
                FTDI_USB_CONFIGURATION_WRITE,
                SIO_SET_MODEM_CTRL_REQUEST,
                (if (state) 1 else 0).toShort(),
                deviceIndex
            )
        )
    }

    fun setRTS(state: Boolean) {
        usbDevice.syncSubmit(
            usbDevice.createUsbControlIrp(
                FTDI_USB_CONFIGURATION_WRITE,
                SIO_SET_MODEM_CTRL_REQUEST,
                (if (state) 2 else 0).toShort(),
                deviceIndex
            )
        )
    }

    fun setDTRRTS(dtrState: Boolean, rtsState: Boolean) {
        val dtrValue = if (dtrState) 1 else 0
        val rtsValue = if (rtsState) 2 else 0
        usbDevice.syncSubmit(
            usbDevice.createUsbControlIrp(
                FTDI_USB_CONFIGURATION_WRITE,
                SIO_SET_MODEM_CTRL_REQUEST,
                (dtrValue or rtsValue) as Short,
                deviceIndex
            )
        )
    }

    fun setFlowControl(flowcontrol: FlowControl) {
        usbDevice.syncSubmit(
            usbDevice.createUsbControlIrp(
                FTDI_USB_CONFIGURATION_WRITE,
                SIO_SET_FLOW_CTRL_REQUEST,
                flowcontrol.flowControl.toShort(),
                deviceIndex
            )
        )
    }

    fun setLineProperty(bits: LineDatabit, stopbits: LineStopbit, parity: LineParity) {
        setLineProperty(bits, stopbits, parity, LineBreak.BREAK_OFF)
    }

    fun setLineProperty(
        bits: LineDatabit,
        stopbits: LineStopbit,
        parity: LineParity,
        breaktype: LineBreak
    ) {
        val value =
            (bits.bits or (parity.parity shl 8) or (stopbits.stopbit shl 11) or (breaktype.breakType shl 14)).toShort()
        usbDevice.syncSubmit(
            usbDevice.createUsbControlIrp(
                FTDI_USB_CONFIGURATION_WRITE,
                SIO_SET_DATA_REQUEST,
                value,
                deviceIndex
            )
        )
    }

    private fun calculateBaudRate(requestedBaudRate: Int): Short {
        val divisor = 3000000 / requestedBaudRate
        return (divisor shl 16 shr 16).toShort()
    }

    fun reset() {
        usbDevice.syncSubmit(
            usbDevice.createUsbControlIrp(
                FTDI_USB_CONFIGURATION_WRITE,
                SIO_RESET_REQUEST,
                0.toShort(),
                deviceIndex
            )
        )
    }

    fun purgeRx() {
        usbDevice.syncSubmit(
            usbDevice.createUsbControlIrp(
                FTDI_USB_CONFIGURATION_WRITE,
                SIO_RESET_REQUEST,
                SIO_RESET_PURGE_RX,
                deviceIndex
            )
        )
        inputStream.purge()
    }

    fun purgeTx() {
        usbDevice.syncSubmit(
            usbDevice.createUsbControlIrp(
                FTDI_USB_CONFIGURATION_WRITE,
                SIO_RESET_REQUEST,
                SIO_RESET_PURGE_TX,
                deviceIndex
            )
        )
    }

    fun setLatencyTimer(latencyMs: Int) {
        if (latencyMs < 1 || latencyMs > 255)
            throw Exception("latency out of range. Only valid for 1-255");

        usbDevice.syncSubmit(
            usbDevice.createUsbControlIrp(
                FTDI_USB_CONFIGURATION_WRITE,
                SIO_SET_LATENCY_TIMER_REQUEST,
                latencyMs.toShort(),
                deviceIndex
            )
        )
    }

    override fun close() {
        try {
            usbInterface.release()
        } catch (ex: Exception) {
        }
    }

    override fun toString(): String = "FTDI $usbDevice"
}