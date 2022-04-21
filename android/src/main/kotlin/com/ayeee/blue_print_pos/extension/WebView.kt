package com.ayeee.blue_print_pos.extension

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.webkit.WebView
import com.ayeee.blue_print_pos.Logger
import kotlin.math.absoluteValue
import kotlin.math.min

fun WebView.toBitmap(offsetWidth: Double, offsetHeight: Double, deviceWidth: Int): Bitmap? {
    if (offsetHeight > 0 && offsetWidth > 0) {
        var width = (offsetWidth * this.resources.displayMetrics.density).absoluteValue.toInt()
        var height = (offsetHeight * this.resources.displayMetrics.density).absoluteValue.toInt()
        if (width > deviceWidth) {
            height = ((deviceWidth.toDouble() / width.toDouble()) * height).toInt()
            width = deviceWidth
        }
        Logger.log("(WebView.toBitmap) width: $width, height: $height")
        this.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        this.draw(canvas)
        return bitmap
    }
    return null
}