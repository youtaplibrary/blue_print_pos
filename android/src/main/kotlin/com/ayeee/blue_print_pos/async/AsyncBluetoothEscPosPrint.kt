package com.ayeee.blue_print_pos.async

import android.content.Context
class AsyncBluetoothEscPosPrint(context: Context?, onPrintFinished: OnPrintFinished?) :
    AsyncEscPosPrint(
        context!!, onPrintFinished
    ) {

    override fun doInBackground(vararg printersData: AsyncEscPosPrinter?): PrinterStatus {
        if (printersData.isEmpty()) {
            return PrinterStatus(null, FINISH_NO_PRINTER)
        }
        return super.doInBackground(*printersData)
    }
}