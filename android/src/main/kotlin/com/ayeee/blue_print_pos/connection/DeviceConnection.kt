package com.ayeee.blue_print_pos.connection

class DeviceConnection {
    var bytes: ByteArray

    init {
        bytes = ByteArray(0)
    }

    /**
     * Add data to send.
     */
    fun write(bytes: ByteArray) {
        val data = ByteArray(bytes.size + this.bytes.size)
        System.arraycopy(this.bytes, 0, data, 0, this.bytes.size)
        System.arraycopy(bytes, 0, data, this.bytes.size, bytes.size)
        this.bytes = data
    }
}