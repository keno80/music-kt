package com.example.musickt.ui.theme

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import android.graphics.Color as AndroidColor

fun dominantColors(bitmap: Bitmap, count: Int = 1): List<Color> {
    val scaled = Bitmap.createScaledBitmap(bitmap, 32, 32, true)
    val step = 32
    val freq = HashMap<Int, Float>()
    val cx = scaled.width / 2f
    val cy = scaled.height / 2f
    val maxDist = kotlin.math.sqrt(cx * cx + cy * cy)
    val hsv = FloatArray(3)
    for (y in 0 until scaled.height) {
        for (x in 0 until scaled.width) {
            val c = scaled.getPixel(x, y)
            val a = (c shr 24) and 0xFF
            if (a < 128) continue
            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val b = c and 0xFF
            AndroidColor.RGBToHSV(r, g, b, hsv)
            val s = hsv[1]
            val v = hsv[2]
            if (s < 0.20f) continue
            if (v < 0.12f || v > 0.95f) continue
            val dx = x - cx
            val dy = y - cy
            val norm = kotlin.math.sqrt(dx * dx + dy * dy) / maxDist
            val weight = (1f - norm) * (1f - norm)
            val rq = r / step
            val gq = g / step
            val bq = b / step
            val key = (rq shl 6) or (gq shl 3) or bq
            freq[key] = (freq[key] ?: 0f) + weight
        }
    }
    val sorted = freq.entries.sortedByDescending { it.value }.take(count)
    return sorted.map {
        val rq = (it.key shr 6) and 0x7
        val gq = (it.key shr 3) and 0x7
        val bq = it.key and 0x7
        val r = rq * step + step / 2
        val g = gq * step + step / 2
        val b = bq * step + step / 2
        Color(r / 255f, g / 255f, b / 255f)
    }
}

fun lighten(color: Color, amount: Float): Color {
    val a = color.alpha
    val r = (color.red + (1f - color.red) * amount).coerceIn(0f, 1f)
    val g = (color.green + (1f - color.green) * amount).coerceIn(0f, 1f)
    val b = (color.blue + (1f - color.blue) * amount).coerceIn(0f, 1f)
    return Color(r, g, b, a)
}

fun darken(color: Color, amount: Float): Color {
    val a = color.alpha
    val r = (color.red * (1f - amount)).coerceIn(0f, 1f)
    val g = (color.green * (1f - amount)).coerceIn(0f, 1f)
    val b = (color.blue * (1f - amount)).coerceIn(0f, 1f)
    return Color(r, g, b, a)
}

fun onColorFor(color: Color): Color {
    val l = color.luminance()
    return if (l > 0.5f) Color.Black else Color.White
}