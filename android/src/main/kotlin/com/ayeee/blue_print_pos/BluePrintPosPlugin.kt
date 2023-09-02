package com.ayeee.blue_print_pos

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.WindowInsets
import android.webkit.WebView
import android.webkit.WebViewClient
import com.ayeee.blue_print_pos.async.AsyncBluetoothEscPosPrint
import com.ayeee.blue_print_pos.async.AsyncEscPosPrint
import com.ayeee.blue_print_pos.async.AsyncEscPosPrinter
import com.ayeee.blue_print_pos.connection.DeviceConnection
import com.ayeee.blue_print_pos.extension.toBitmap
import com.ayeee.blue_print_pos.extension.toByteArray
import com.ayeee.blue_print_pos.textparser.PrinterTextParserImg
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** BluePrintPosPlugin */
class BluePrintPosPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var activity: Activity
    private lateinit var context: Context
    private var selectedDevice = DeviceConnection()
    private var printer: AsyncEscPosPrinter? = null

    private fun parseTextToBytes(printerData: String, result: Result) {
        printer = AsyncEscPosPrinter(selectedDevice, 203, 48f, 32)
        AsyncBluetoothEscPosPrint(
            context,
            object : AsyncEscPosPrint.OnPrintFinished() {
                override fun onError(asyncEscPosPrinter: AsyncEscPosPrinter?, codeException: Int) {
                    Log.e("Async.OnPrintFinished", "AsyncEscPosPrint.OnPrintFinished : An error occurred !")
                    printer!!.setTextsToPrint(arrayOfNulls(0))
                }

                override fun onSuccess(asyncEscPosPrinter: AsyncEscPosPrinter?) {
                    Log.i("Async.OnPrintFinished", "AsyncEscPosPrint.OnPrintFinished : Print is finished !")
                    result.success(printer!!.printerConnection.bytes)
                    printer!!.setTextsToPrint(arrayOfNulls(0))
                    printer!!.printerConnection.bytes = ByteArray(0)
                }
            }
        )
            .execute(this.getAsyncEscPosPrinter(selectedDevice,printerData))

    }

    @SuppressLint("SimpleDateFormat")
    fun getAsyncEscPosPrinter(printerConnection: DeviceConnection?, printerData: String): AsyncEscPosPrinter? {
        if ( printerConnection == null || printer == null) {
            return null
        }
        return printer!!.addTextToPrint(printerData)
    }

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        val viewID = "webview-view-type"
        flutterPluginBinding.platformViewRegistry.registerViewFactory(viewID, FLNativeViewFactory())

        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "blue_print_pos")
        channel.setMethodCallHandler(this)
        context = flutterPluginBinding.applicationContext
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        val arguments = call.arguments as Map<*, *>
        val content = arguments["content"] as String
        val duration = arguments["duration"] as Double?
        val textScaleFactor: Double? = arguments["textScaleFactor"] as? Double?
        val textZoom: Int? = textScaleFactor?.let { (it * 100).toInt() }

        when (call.method) {
            "contentToImage" -> {
                contentToImage(content, textZoom, result, duration)
            }
            "convertImageToHexadecimal" -> {
                val imageDataBase64 = (call.arguments as? Map<*, *>)?.get("content") as? String
                if (imageDataBase64.isNullOrEmpty()) {
                    result.error(
                        "400",
                        "Please supply this bytesToHexadecimalString method with a string to convert",
                        null,
                    )
                } else {
                    printer = AsyncEscPosPrinter(selectedDevice, 203, 48f, 32)
                    val bytes = Base64.decode(imageDataBase64,Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.size)
                    val hexadecimal = PrinterTextParserImg.bitmapToHexadecimalString(printer,bitmap)
                    result.success(hexadecimal)
                }
            }
            "parseTextToBytes" -> {
                Log.i("Content", content)
                parseTextToBytes(content,result)

            } else -> {
                result.notImplemented()
            }
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("SetJavaScriptEnabled")
    private fun contentToImage(
        content: String,
        textZoom: Int?,
        result: Result,
        duration: Double?
    ) {
        val webView = WebView(this.context)
        val dWidth: Int
        val dHeight: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets =
                windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            dWidth = windowMetrics.bounds.width() - insets.left - insets.right
            dHeight = windowMetrics.bounds.height() - insets.bottom - insets.top
        } else {
            dWidth = activity.window.windowManager.defaultDisplay.width
            dHeight = activity.window.windowManager.defaultDisplay.height
        }
        Logger.log("\ndwidth : $dWidth")
        Logger.log("\ndheight : $dHeight")
        webView.layout(0, 0, dWidth, dHeight)
        webView.loadDataWithBaseURL(null, content, "text/HTML", "UTF-8", null)
        webView.setInitialScale(1)
        webView.settings.javaScriptEnabled = true
        webView.settings.useWideViewPort = true
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.textZoom = textZoom ?: webView.settings.textZoom
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Logger.log("\n=======> enabled scrolled <=========")
            WebView.enableSlowWholeDocumentDraw()
        }

        Logger.log("\n ///////////////// webview setted /////////////////")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)

                fun destroyWebView() {
                    webView.destroy()
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    Logger.log("\n ================ webview completed ==============")
                    Logger.log("\n scroll delayed ${webView.scrollBarFadeDuration}")

                    webView.evaluateJavascript("document.body.offsetWidth") { offsetWidth ->
                        webView.evaluateJavascript("document.body.offsetHeight") { offsetHeight ->
                            Logger.log("\noffsetWidth : $offsetWidth")
                            Logger.log("\noffsetHeight : $offsetHeight")
                            if (offsetWidth == null || offsetWidth.isEmpty() || offsetHeight == null || offsetHeight.isEmpty()) {
                                result.error(
                                    "FAILED_CONTENT_TO_IMAGE",
                                    "Failed to convert content to image",
                                    null
                                )
                                destroyWebView()
                                return@evaluateJavascript
                            }

                            val data: Bitmap? = webView.toBitmap(
                                offsetWidth.toDouble(),
                                offsetHeight.toDouble(),
                                dWidth,
                            )
                            if (data == null) {
                                result.error(
                                    "FAILED_CONTENT_TO_IMAGE",
                                    "Failed to convert content to image",
                                    null
                                )
                            } else {
                                val bytes: ByteArray = data.toByteArray()
                                result.success(bytes)
                                Logger.log("\n Got snapshot")
                            }

                            destroyWebView()
                        }
                    }

                }, (duration ?: 0.0).toLong())

            }
        }
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        Logger.log("onAttachedToActivity")
        activity = binding.activity
        context = activity.applicationContext
        WebView(activity.applicationContext).apply {
            minimumHeight = 1
            minimumWidth = 1
        }
    }

    override fun onDetachedFromActivityForConfigChanges() {
        // This call will be followed by onReattachedToActivityForConfigChanges().
        Logger.log("onDetachedFromActivityForConfigChanges")
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        Logger.log("onAttachedToActivity")
        onAttachedToActivity(binding)
    }

    override fun onDetachedFromActivity() {
        Logger.log("onDetachedFromActivity")
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
