package com.example.musickt.ui.components

import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import com.example.musickt.ui.theme.*
import com.example.musickt.MusicItem
import android.media.MediaMetadataRetriever
import android.graphics.BitmapFactory
import android.graphics.Bitmap

@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier,
    currentMusic: MusicItem? = null,
    colorTransitionDurationMs: Int = 900,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bitmap = remember(currentMusic?.path) {
        currentMusic?.path?.let { p ->
            try {
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(p)
                val art = mmr.embeddedPicture
                mmr.release()
                art?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
            } catch (_: Exception) {
                null
            }
        }
    }
    val topColors = remember(bitmap) { bitmap?.let { dominantColors(it, 3) } }
    val basePalette = if (isDark) {
        listOf(
            DarkGradient1.copy(alpha = 0.65f),
            DarkGradient2.copy(alpha = 0.6f),
            DarkGradient3.copy(alpha = 0.55f),
            DarkGradient2.copy(alpha = 0.5f),
            DarkGradient1.copy(alpha = 0.45f)
        )
    } else {
        listOf(
            LightGradient1.copy(alpha = 0.35f),
            LightGradient2.copy(alpha = 0.3f),
            LightGradient3.copy(alpha = 0.28f),
            LightGradient2.copy(alpha = 0.25f),
            LightGradient1.copy(alpha = 0.22f)
        )
    }
    val palette = if (topColors != null && topColors.isNotEmpty()) {
        val c1 = topColors.getOrNull(0)
        val c2 = topColors.getOrNull(1)
        val c3 = topColors.getOrNull(2)
        val final = when (topColors.size) {
            3 -> listOf(c1!!, c2!!, c3!!)
            2 -> listOf(c1!!, darken(c2!!, 0.15f), lighten(c1, 0.15f))
            else -> listOf(c1!!, darken(c1, 0.15f), lighten(c1, 0.15f))
        }
        if (isDark) {
            listOf(
                final[0].copy(alpha = 0.78f),
                final[1].copy(alpha = 0.6f),
                final[2].copy(alpha = 0.55f),
                final[1].copy(alpha = 0.5f),
                final[0].copy(alpha = 0.55f)
            )
        } else {
            listOf(
                final[0].copy(alpha = 0.45f),
                final[1].copy(alpha = 0.3f),
                final[2].copy(alpha = 0.28f),
                final[1].copy(alpha = 0.25f),
                final[0].copy(alpha = 0.28f)
            )
        }
    } else basePalette

    val a1 = animateColorAsState(targetValue = palette[0], animationSpec = tween(colorTransitionDurationMs, easing = FastOutSlowInEasing), label = "p1")
    val a2 = animateColorAsState(targetValue = palette[1], animationSpec = tween(colorTransitionDurationMs, easing = FastOutSlowInEasing), label = "p2")
    val a3 = animateColorAsState(targetValue = palette[2], animationSpec = tween(colorTransitionDurationMs, easing = FastOutSlowInEasing), label = "p3")
    val a4 = animateColorAsState(targetValue = palette[3], animationSpec = tween(colorTransitionDurationMs, easing = FastOutSlowInEasing), label = "p4")
    val a5 = animateColorAsState(targetValue = palette[4], animationSpec = tween(colorTransitionDurationMs, easing = FastOutSlowInEasing), label = "p5")
    val animatedPalette = listOf(a1.value, a2.value, a3.value, a4.value, a5.value)

    val bgColor = if (isDark) Color(0xFF1C1B1F) else Color(0xFFFFFBFE)

    val transition = rememberInfiniteTransition(label = "aurora")

    val t1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t1"
    )
    val t2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t2"
    )
    val t3 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t3"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val m = kotlin.math.max(w, h)

            val a1 = Math.toRadians(t1.toDouble()).toFloat()
            val a2 = Math.toRadians(t2.toDouble()).toFloat()
            val a3 = Math.toRadians(t3.toDouble()).toFloat()

            val c1 = Offset(w * (0.25f + 0.35f * kotlin.math.sin(a1)), h * (0.35f + 0.35f * kotlin.math.cos(a2)))
            val c2 = Offset(w * (0.7f + 0.3f * kotlin.math.cos(a2)), h * (0.3f + 0.3f * kotlin.math.sin(a3)))
            val c3 = Offset(w * (0.5f + 0.35f * kotlin.math.sin(a3)), h * (0.7f + 0.3f * kotlin.math.cos(a1)))
            val c4 = Offset(w * (0.15f + 0.35f * kotlin.math.cos(a1)), h * (0.75f + 0.3f * kotlin.math.sin(a2)))
            val c5 = Offset(w * (0.85f + 0.3f * kotlin.math.sin(a2)), h * (0.2f + 0.35f * kotlin.math.cos(a3)))

            val centers = listOf(c1, c2, c3, c4, c5)
            val rPulse1 = 1f + 0.22f * kotlin.math.sin(a1)
            val rPulse2 = 1f + 0.1f * kotlin.math.sin(a2)
            val rPulse3 = 1f + 0.12f * kotlin.math.sin(a3)
            val rPulse4 = 1f + 0.09f * kotlin.math.sin(a2)
            val rPulse5 = 1f + 0.08f * kotlin.math.sin(a3)
            val radii = listOf(m * 1.05f * rPulse1, m * 0.85f * rPulse2, m * 0.88f * rPulse3, m * 0.8f * rPulse4, m * 0.75f * rPulse5)

            centers.zip(animatedPalette.zip(radii)).forEach { (center, pair) ->
                val (color, radius) = pair
                val brush = Brush.radialGradient(
                    colors = listOf(color, Color.Transparent),
                    center = center,
                    radius = radius
                )
                drawCircle(brush = brush, radius = radius, center = center)
            }

            val shimmerAngle = a1 + a2 / 2f
            val shimmer = Brush.linearGradient(
                colors = listOf(Color.White.copy(alpha = if (isDark) 0.03f else 0.06f), Color.Transparent),
                start = Offset(0f, 0f),
                end = Offset(
                    kotlin.math.cos(shimmerAngle) * w,
                    kotlin.math.sin(shimmerAngle) * h
                )
            )
            drawRect(brush = shimmer)
        }

        content()
    }
}

private fun dominantColors(bitmap: Bitmap, count: Int = 3): List<Color> {
    val scaled = Bitmap.createScaledBitmap(bitmap, 32, 32, true)
    val step = 32
    val freq = HashMap<Int, Int>()
    for (y in 0 until scaled.height) {
        for (x in 0 until scaled.width) {
            val c = scaled.getPixel(x, y)
            val a = (c shr 24) and 0xFF
            if (a < 128) continue
            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val b = c and 0xFF
            val brightness = (r + g + b) / 3
            if (brightness < 24 || brightness > 232) continue
            val rq = r / step
            val gq = g / step
            val bq = b / step
            val key = (rq shl 6) or (gq shl 3) or bq
            freq[key] = (freq[key] ?: 0) + 1
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

private fun darken(color: Color, amount: Float): Color {
    val a = color.alpha
    val r = (color.red * (1f - amount)).coerceIn(0f, 1f)
    val g = (color.green * (1f - amount)).coerceIn(0f, 1f)
    val b = (color.blue * (1f - amount)).coerceIn(0f, 1f)
    return Color(r, g, b, a)
}

private fun lighten(color: Color, amount: Float): Color {
    val a = color.alpha
    val r = (color.red + (1f - color.red) * amount).coerceIn(0f, 1f)
    val g = (color.green + (1f - color.green) * amount).coerceIn(0f, 1f)
    val b = (color.blue + (1f - color.blue) * amount).coerceIn(0f, 1f)
    return Color(r, g, b, a)
}

@Composable
fun MeshGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    // 选择颜色
    val colors = if (isDark) {
        listOf(
            DarkGradient1.copy(alpha = 0.6f),
            DarkGradient2.copy(alpha = 0.6f),
            DarkGradient3.copy(alpha = 0.6f),
            DarkGradient1.copy(alpha = 0.4f)
        )
    } else {
        listOf(
            LightGradient1.copy(alpha = 0.3f),
            LightGradient2.copy(alpha = 0.3f),
            LightGradient3.copy(alpha = 0.3f),
            LightGradient1.copy(alpha = 0.2f)
        )
    }
    
    // 创建多层渐变动画
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    
    val angle1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle1"
    )
    
    val angle2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle2"
    )
    
    // 创建多个渐变层
    val brush1 = Brush.linearGradient(
        colors = listOf(colors[0], Color.Transparent, colors[1]),
        start = Offset(0f, 0f),
        end = Offset(
            kotlin.math.cos(Math.toRadians(angle1.toDouble())).toFloat() * 2000f,
            kotlin.math.sin(Math.toRadians(angle1.toDouble())).toFloat() * 2000f
        )
    )
    
    val brush2 = Brush.linearGradient(
        colors = listOf(colors[2], Color.Transparent, colors[3]),
        start = Offset(1000f, 0f),
        end = Offset(
            1000f + kotlin.math.cos(Math.toRadians(angle2.toDouble())).toFloat() * 2000f,
            kotlin.math.sin(Math.toRadians(angle2.toDouble())).toFloat() * 2000f
        )
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF1C1B1F) else Color(0xFFFFFBFE))
            .background(brush1)
            .background(brush2)
    ) {
        content()
    }
}
