package com.ayeee.blue_print_pos.async

import android.content.Context
import com.ayeee.blue_print_pos.CoroutinesAsyncTask
import com.ayeee.blue_print_pos.EscPosCharsetEncoding
import com.ayeee.blue_print_pos.EscPosPrinter
import com.ayeee.blue_print_pos.exceptions.EscPosBarcodeException
import com.ayeee.blue_print_pos.exceptions.EscPosConnectionException
import com.ayeee.blue_print_pos.exceptions.EscPosEncodingException
import com.ayeee.blue_print_pos.exceptions.EscPosParserException
import java.lang.ref.WeakReference


abstract class AsyncEscPosPrint @JvmOverloads constructor(
    context: Context,
    private var onPrintFinished: OnPrintFinished? = null
) :
    CoroutinesAsyncTask<AsyncEscPosPrinter, Int, AsyncEscPosPrint.PrinterStatus>("MyAsyncTask") {
    private var weakContext: WeakReference<Context>


    init {
        weakContext = WeakReference(context)
    }

    override fun doInBackground(vararg printersData: AsyncEscPosPrinter?): PrinterStatus {
        if (printersData.isEmpty()) {
            return PrinterStatus(null, FINISH_NO_PRINTER)
        }
        publishProgress(PROGRESS_CONNECTING)
        val printerData = printersData[0] ?: return PrinterStatus(null, FINISH_PARSER_ERROR)
        try {
            val deviceConnection = printerData.printerConnection
            val printer = EscPosPrinter(
                deviceConnection,
                printerData.printerDpi,
                printerData.printerWidthMM,
                printerData.printerNbrCharactersPerLine,
                EscPosCharsetEncoding("windows-1252", 16)
            )
            // printer.useEscAsteriskCommand(true);

            publishProgress(PROGRESS_PRINTING)
            val textsToPrint: Array<String?> = printerData.textsToPrint
            for (textToPrint in textsToPrint) {
                printer.printFormattedTextAndCut(textToPrint)
                Thread.sleep(500)
            }
            publishProgress(PROGRESS_PRINTED)
        } catch (e: EscPosConnectionException) {
            e.printStackTrace()
            return PrinterStatus(printerData, FINISH_PRINTER_DISCONNECTED)
        } catch (e: EscPosParserException) {
            e.printStackTrace()
            return PrinterStatus(printerData, FINISH_PARSER_ERROR)
        } catch (e: EscPosEncodingException) {
            e.printStackTrace()
            return PrinterStatus(printerData, FINISH_ENCODING_ERROR)
        } catch (e: EscPosBarcodeException) {
            e.printStackTrace()
            return PrinterStatus(printerData, FINISH_BARCODE_ERROR)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return PrinterStatus(printerData, FINISH_SUCCESS)
    }

    override fun onPreExecute() {

    }

    override fun onProgressUpdate(vararg values: Int?) {

    }

    override fun onPostExecute(result: PrinterStatus) {
//        val context = weakContext.get() ?: return

//        when (result.printerStatus) {
//            FINISH_SUCCESS -> AlertDialog.Builder(context)
//                .setTitle("Success")
//                .setMessage("Congratulation ! The texts are printed !")
//                .show()
//
//            FINISH_NO_PRINTER -> AlertDialog.Builder(context)
//                .setTitle("No printer")
//                .setMessage("The application can't find any printer connected.")
//                .show()
//
//            FINISH_PRINTER_DISCONNECTED -> AlertDialog.Builder(context)
//                .setTitle("Broken connection")
//                .setMessage("Unable to connect the printer.")
//                .show()
//
//            FINISH_PARSER_ERROR -> AlertDialog.Builder(context)
//                .setTitle("Invalid formatted text")
//                .setMessage("It seems to be an invalid syntax problem.")
//                .show()
//
//            FINISH_ENCODING_ERROR -> AlertDialog.Builder(context)
//                .setTitle("Bad selected encoding")
//                .setMessage("The selected encoding character returning an error.")
//                .show()
//
//            FINISH_BARCODE_ERROR -> AlertDialog.Builder(context)
//                .setTitle("Invalid barcode")
//                .setMessage("Data send to be converted to barcode or QR code seems to be invalid.")
//                .show()
//        }
        if (onPrintFinished != null) {
            if (result.printerStatus == FINISH_SUCCESS) {
                onPrintFinished!!.onSuccess(result.asyncEscPosPrinter)
            } else {
                onPrintFinished!!.onError(result.asyncEscPosPrinter, result.printerStatus)
            }
        }
    }

    class PrinterStatus(val asyncEscPosPrinter: AsyncEscPosPrinter?, val printerStatus: Int)
    abstract class OnPrintFinished {
        abstract fun onError(asyncEscPosPrinter: AsyncEscPosPrinter?, codeException: Int)
        abstract fun onSuccess(asyncEscPosPrinter: AsyncEscPosPrinter?)
    }

    companion object {
        const val FINISH_SUCCESS = 1
        const val FINISH_NO_PRINTER = 2
        const val FINISH_PRINTER_DISCONNECTED = 3
        const val FINISH_PARSER_ERROR = 4
        const val FINISH_ENCODING_ERROR = 5
        const val FINISH_BARCODE_ERROR = 6
        const val PROGRESS_CONNECTING = 1
        const val PROGRESS_PRINTING = 3
        const val PROGRESS_PRINTED = 4
    }
}