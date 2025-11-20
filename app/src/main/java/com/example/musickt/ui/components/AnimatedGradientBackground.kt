package com.example.musickt.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import com.example.musickt.ui.theme.*

@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val palette = if (isDark) {
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

    val bgColor = if (isDark) Color(0xFF1C1B1F) else Color(0xFFFFFBFE)

    val transition = rememberInfiniteTransition(label = "aurora")

    val t1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(32000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t1"
    )
    val t2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(26000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "t2"
    )
    val t3 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(21000, easing = LinearEasing),
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

            val a1 = Math.toRadians(t1.toDouble()).toFloat()
            val a2 = Math.toRadians(t2.toDouble()).toFloat()
            val a3 = Math.toRadians(t3.toDouble()).toFloat()

            val c1 = Offset(w * (0.25f + 0.2f * kotlin.math.sin(a1)), h * (0.35f + 0.25f * kotlin.math.cos(a2)))
            val c2 = Offset(w * (0.7f + 0.15f * kotlin.math.cos(a2)), h * (0.3f + 0.2f * kotlin.math.sin(a3)))
            val c3 = Offset(w * (0.5f + 0.25f * kotlin.math.sin(a3)), h * (0.7f + 0.15f * kotlin.math.cos(a1)))
            val c4 = Offset(w * (0.15f + 0.25f * kotlin.math.cos(a1)), h * (0.75f + 0.2f * kotlin.math.sin(a2)))
            val c5 = Offset(w * (0.85f + 0.2f * kotlin.math.sin(a2)), h * (0.2f + 0.25f * kotlin.math.cos(a3)))

            val centers = listOf(c1, c2, c3, c4, c5)
            val radii = listOf(w * 0.8f, w * 0.7f, w * 0.75f, w * 0.65f, w * 0.6f)

            centers.zip(palette.zip(radii)).forEach { (center, pair) ->
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
