package com.example.musickt.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.musickt.MusicItem
import android.media.MediaMetadataRetriever
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MusicPlayerBar(
    currentMusic: MusicItem?,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPreviousClick: () -> Unit,
    currentPosition: Long = 0L,
    duration: Long = 0L,
    modifier: Modifier = Modifier,
    colorTransitionDurationMs: Int = 500
) {
    AnimatedVisibility(
        visible = currentMusic != null,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
                val context = LocalContext.current
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
                val cover = remember(bitmap) {
                    bitmap?.let { coverColors(it) }
                }
                val targetLeft = cover?.dominant?.getOrNull(0) ?: MaterialTheme.colorScheme.surfaceVariant
                val leftCapsuleColor by animateColorAsState(targetValue = targetLeft, animationSpec = tween(colorTransitionDurationMs, easing = FastOutSlowInEasing), label = "left")
                val targetRight = lighten(targetLeft, 0.18f)
                val rightCapsuleColor by animateColorAsState(targetValue = targetRight, animationSpec = tween(colorTransitionDurationMs, easing = FastOutSlowInEasing), label = "right")
                val titleTarget = if (luminance(targetLeft) < 0.5f) {
                    lighten(targetLeft, 0.75f)
                } else {
                    darken(targetLeft, 0.75f)
                }
                val textColor by animateColorAsState(targetValue = titleTarget, animationSpec = tween(colorTransitionDurationMs, easing = FastOutSlowInEasing), label = "text")
                val subtitleTarget = lighten(titleTarget, 0.15f)
                val subtitleColor by animateColorAsState(targetValue = subtitleTarget, animationSpec = tween(colorTransitionDurationMs, easing = FastOutSlowInEasing), label = "subtitle")
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp),
                    color = leftCapsuleColor.copy(alpha = 0.9f),
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    shape = RoundedCornerShape(
                        topStart = 32.dp,
                        bottomStart = 32.dp,
                        topEnd = 8.dp,
                        bottomEnd = 8.dp
                    )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 9.dp, end = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(40.dp)
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            ) {
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            val progressRaw = if (duration > 0L) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
                            val progress by androidx.compose.animation.core.animateFloatAsState(
                                targetValue = progressRaw,
                                animationSpec = androidx.compose.animation.core.tween(300, easing = FastOutSlowInEasing),
                                label = "coverProgress"
                            )
                            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 2.dp.toPx()
                                val inset = strokeWidth / 2f
                                drawArc(
                                    color = textColor,
                                    startAngle = -90f,
                                    sweepAngle = progress * 360f,
                                    useCenter = false,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                                    topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
                                    size = androidx.compose.ui.geometry.Size(size.width - strokeWidth, size.height - strokeWidth)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = currentMusic?.title ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = MaterialTheme.typography.titleMedium.fontSize * 1.02f,
                                color = textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.basicMarquee()
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentMusic?.artist ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize * 0.9f,
                                color = subtitleColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    modifier = Modifier
                        .height(58.dp)
                        .clickable(onClick = onPlayPauseClick),
                    color = rightCapsuleColor,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "暂停" else "播放",
                            tint = textColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    modifier = Modifier.height(58.dp),
                    color = rightCapsuleColor,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    shape = RoundedCornerShape(
                        topStart = 8.dp,
                        bottomStart = 8.dp,
                        topEnd = 32.dp,
                        bottomEnd = 32.dp
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "下一首",
                            tint = textColor
                        )
                    }
                }
            }
    }
}

private data class CoverColors(
    val dominant: List<Color>,
    val lightTop: Color?,
    val darkTop: Color?,
    val contrast: Color?,
    val isMostlyDark: Boolean
)

private fun dominantColors(bitmap: Bitmap, count: Int = 2): List<Color> {
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

private fun coverColors(bitmap: Bitmap): CoverColors {
    val scaled = Bitmap.createScaledBitmap(bitmap, 32, 32, true)
    val step = 32
    val all = HashMap<Int, Float>()
    val light = HashMap<Int, Float>()
    val dark = HashMap<Int, Float>()
    var darkCount = 0f
    var lightCount = 0f
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
            all[key] = (all[key] ?: 0f) + weight
            if (v < 0.5f) {
                dark[key] = (dark[key] ?: 0f) + weight
                darkCount += weight
            } else {
                light[key] = (light[key] ?: 0f) + weight
                lightCount += weight
            }
        }
    }
    val topKey = all.entries.maxByOrNull { it.value }?.key
    val dom = topKey?.let { listOf(toColor(it, step)) } ?: emptyList()
    val lightTop = light.entries.maxByOrNull { it.value }?.key?.let { toColor(it, step) }
    val darkTop = dark.entries.maxByOrNull { it.value }?.key?.let { toColor(it, step) }
    val contrastKey = if (topKey != null) {
        all.keys.maxByOrNull { k ->
            val c1 = toColor(topKey, step)
            val c2 = toColor(k, step)
            colorDistance(c1, c2)
        }
    } else null
    val contrast = contrastKey?.let { toColor(it, step) }
    val mostlyDark = darkCount > lightCount
    return CoverColors(dom, lightTop, darkTop, contrast, mostlyDark)
}

private fun toColor(key: Int, step: Int): Color {
    val rq = (key shr 6) and 0x7
    val gq = (key shr 3) and 0x7
    val bq = key and 0x7
    val r = rq * step + step / 2
    val g = gq * step + step / 2
    val b = bq * step + step / 2
    return Color(r / 255f, g / 255f, b / 255f)
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

private fun colorDistance(c1: Color, c2: Color): Float {
    val dr = c1.red - c2.red
    val dg = c1.green - c2.green
    val db = c1.blue - c2.blue
    return kotlin.math.sqrt(dr * dr + dg * dg + db * db)
}

private fun luminance(color: Color): Float {
    return 0.2126f * color.red + 0.7152f * color.green + 0.0722f * color.blue
}
