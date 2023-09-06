package com.ayeee.blue_print_pos.async

import  com.ayeee.blue_print_pos.EscPosPrinterSize
import com.ayeee.blue_print_pos.connection.DeviceConnection


class AsyncEscPosPrinter(
    val printerConnection: DeviceConnection,
    printerDpi: Int,
    printerWidthMM: Float,
    printerNbrCharactersPerLine: Int
) :
    EscPosPrinterSize(printerDpi, printerWidthMM, printerNbrCharactersPerLine) {
    var textsToPrint = arrayOfNulls<String>(0)
        private set

    fun setTextsToPrint(textsToPrint: Array<String?>): AsyncEscPosPrinter {
        this.textsToPrint = textsToPrint
        return this
    }

    fun addTextToPrint(textToPrint: String?): AsyncEscPosPrinter {
        val tmp = arrayOfNulls<String>(textsToPrint.size + 1)
        System.arraycopy(textsToPrint, 0, tmp, 0, textsToPrint.size)
        tmp[textsToPrint.size] = textToPrint
        textsToPrint = tmp
        return this
    }
}