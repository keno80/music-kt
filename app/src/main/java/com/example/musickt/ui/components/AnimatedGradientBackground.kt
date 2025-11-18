package com.example.musickt.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.musickt.ui.theme.*

@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    
    // 选择颜色
    val colors = if (isDark) {
        listOf(DarkGradient1, DarkGradient2, DarkGradient3)
    } else {
        listOf(LightGradient1, LightGradient2, LightGradient3)
    }
    
    // 创建无限循环动画
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetX"
    )
    
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )
    
    // 创建动态渐变
    val brush = Brush.radialGradient(
        colors = colors,
        center = Offset(offsetX, offsetY),
        radius = 1500f
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush)
    ) {
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
