package com.psyndicate.kprodatalog.ftdi

import java.io.InputStream
import javax.usb3.IUsbPipe
import javax.usb3.event.IUsbPipeListener
import javax.usb3.event.UsbPipeDataEvent
import javax.usb3.event.UsbPipeErrorEvent

class UsbBufferedInputStream(private val usbPipe: IUsbPipe) : InputStream() {
    private val buffers = ArrayDeque<ByteArray>()
    private var position: Int = 0
    private val lock = Object()

//    init {
//        usbPipe.addUsbPipeListener(
//            object : IUsbPipeListener {
//                override fun errorEventOccurred(event: UsbPipeErrorEvent) {
//                    println(event)
//                }
//
//                override fun dataEventOccurred(event: UsbPipeDataEvent) {
//                    // First two bytes are status
//                    if (event.actualLength > 2) {
//                        val buffer = ByteArray(event.actualLength - 2)
//                        event.data.copyInto(buffer, 0, 2, event.actualLength)
//                        synchronized(lock) {
//                            buffers.addLast(buffer)
//                            lock.notifyAll()
//                        }
//                    }
//                }
//            }
//        )
//    }

    fun purge() {
        synchronized(lock) {
            buffers.clear()
            position = 0
        }
    }

    override fun available(): Int {
        synchronized(lock) {
            return buffers.sumBy { it.size } - position
        }
    }

    override fun read(): Int {
        val start = System.currentTimeMillis()
        while (true) {
            synchronized(lock) {
                while (buffers.isNotEmpty()) {
                    val activeBuffer = buffers.first()
                    if (position >= activeBuffer.size) {
                        buffers.removeFirst()
                        position = 0
                        continue
                    }
                    return activeBuffer[position].toInt().also {
                        position++
                    }
                }
                val buffer = ByteArray(64)
                val actualRead = usbPipe.syncSubmit(buffer)
                if (actualRead > 2) {
                    buffers.addLast(buffer.copyOfRange(2, actualRead))
                }
                if (System.currentTimeMillis() - start > 500) return -1
            }
        }
    }

    override fun close() {
        super.close()
        usbPipe.close()
    }
}