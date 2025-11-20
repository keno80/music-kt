package com.example.musickt.ui.theme

import android.app.Activity
import android.os.Build
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.musickt.MusicItem

private val DarkColorScheme = darkColorScheme(
    primary = ExpressivePrimary,
    onPrimary = ExpressiveOnPrimary,
    primaryContainer = ExpressivePrimaryContainer,
    onPrimaryContainer = ExpressiveOnPrimaryContainer,
    secondary = ExpressiveSecondary,
    onSecondary = ExpressiveOnSecondary,
    secondaryContainer = ExpressiveSecondaryContainer,
    onSecondaryContainer = ExpressiveOnSecondaryContainer,
    tertiary = ExpressiveTertiary,
    onTertiary = ExpressiveOnTertiary,
    tertiaryContainer = ExpressiveTertiaryContainer,
    onTertiaryContainer = ExpressiveOnTertiaryContainer,
    error = ExpressiveError,
    onError = ExpressiveOnError,
    errorContainer = ExpressiveErrorContainer,
    onErrorContainer = ExpressiveOnErrorContainer,
    background = ExpressiveBackgroundDark,
    onBackground = ExpressiveOnBackgroundDark,
    surface = ExpressiveSurfaceDark,
    onSurface = ExpressiveOnSurfaceDark,
    surfaceVariant = ExpressiveSurfaceVariantDark,
    onSurfaceVariant = ExpressiveOnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = ExpressivePrimary,
    onPrimary = ExpressiveOnPrimary,
    primaryContainer = ExpressivePrimaryContainer,
    onPrimaryContainer = ExpressiveOnPrimaryContainer,
    secondary = ExpressiveSecondary,
    onSecondary = ExpressiveOnSecondary,
    secondaryContainer = ExpressiveSecondaryContainer,
    onSecondaryContainer = ExpressiveOnSecondaryContainer,
    tertiary = ExpressiveTertiary,
    onTertiary = ExpressiveOnTertiary,
    tertiaryContainer = ExpressiveTertiaryContainer,
    onTertiaryContainer = ExpressiveOnTertiaryContainer,
    error = ExpressiveError,
    onError = ExpressiveOnError,
    errorContainer = ExpressiveErrorContainer,
    onErrorContainer = ExpressiveOnErrorContainer,
    background = ExpressiveBackground,
    onBackground = ExpressiveOnBackground,
    surface = ExpressiveSurface,
    onSurface = ExpressiveOnSurface,
    surfaceVariant = ExpressiveSurfaceVariantLight,
    onSurfaceVariant = ExpressiveOnSurfaceVariantLight
)

@Composable
fun MusicKtTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    currentMusic: MusicItem? = null,
    content: @Composable () -> Unit
) {
    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
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
    val contrastColor = cover?.contrast
    val colorScheme = if (contrastColor != null) {
        if (darkTheme) {
            darkColorScheme(
                primary = contrastColor,
                onPrimary = if (luminance(contrastColor) > 0.5f) Color.Black else Color.White,
                primaryContainer = baseScheme.primaryContainer,
                onPrimaryContainer = baseScheme.onPrimaryContainer,
                secondary = baseScheme.secondary,
                onSecondary = baseScheme.onSecondary,
                secondaryContainer = baseScheme.secondaryContainer,
                onSecondaryContainer = baseScheme.onSecondaryContainer,
                tertiary = baseScheme.tertiary,
                onTertiary = baseScheme.onTertiary,
                tertiaryContainer = baseScheme.tertiaryContainer,
                onTertiaryContainer = baseScheme.onTertiaryContainer,
                error = baseScheme.error,
                onError = baseScheme.onError,
                errorContainer = baseScheme.errorContainer,
                onErrorContainer = baseScheme.onErrorContainer,
                background = baseScheme.background,
                onBackground = baseScheme.onBackground,
                surface = baseScheme.surface,
                onSurface = baseScheme.onSurface,
                surfaceVariant = baseScheme.surfaceVariant,
                onSurfaceVariant = baseScheme.onSurfaceVariant
            )
        } else {
            lightColorScheme(
                primary = contrastColor,
                onPrimary = if (luminance(contrastColor) > 0.5f) Color.Black else Color.White,
                primaryContainer = baseScheme.primaryContainer,
                onPrimaryContainer = baseScheme.onPrimaryContainer,
                secondary = baseScheme.secondary,
                onSecondary = baseScheme.onSecondary,
                secondaryContainer = baseScheme.secondaryContainer,
                onSecondaryContainer = baseScheme.onSecondaryContainer,
                tertiary = baseScheme.tertiary,
                onTertiary = baseScheme.onTertiary,
                tertiaryContainer = baseScheme.tertiaryContainer,
                onTertiaryContainer = baseScheme.onTertiaryContainer,
                error = baseScheme.error,
                onError = baseScheme.onError,
                errorContainer = baseScheme.errorContainer,
                onErrorContainer = baseScheme.onErrorContainer,
                background = baseScheme.background,
                onBackground = baseScheme.onBackground,
                surface = baseScheme.surface,
                onSurface = baseScheme.onSurface,
                surfaceVariant = baseScheme.surfaceVariant,
                onSurfaceVariant = baseScheme.onSurfaceVariant
            )
        }
    } else baseScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

private data class CoverColors(
    val dominant: List<Color>,
    val contrast: Color?
)

private fun coverColors(bitmap: Bitmap): CoverColors {
    val step = 32
    val scaled = Bitmap.createScaledBitmap(bitmap, 32, 32, true)
    val freq = HashMap<Int, Int>()
    for (y in 0 until scaled.height) {
        for (x in 0 until scaled.width) {
            val c = scaled.getPixel(x, y)
            val a = (c shr 24) and 0xFF
            if (a < 128) continue
            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val b = c and 0xFF
            val rq = r / step
            val gq = g / step
            val bq = b / step
            val key = (rq shl 6) or (gq shl 3) or bq
            freq[key] = (freq[key] ?: 0) + 1
        }
    }
    val top = freq.entries.maxByOrNull { it.value }?.key
    val dominant = top?.let { listOf(toColor(it, step)) } ?: emptyList()
    val contrastKey = if (top != null) {
        freq.keys.maxByOrNull { k ->
            val c1 = toColor(top, step)
            val c2 = toColor(k, step)
            colorDistance(c1, c2)
        }
    } else null
    val contrast = contrastKey?.let { toColor(it, step) }
    return CoverColors(dominant, contrast)
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

private fun colorDistance(c1: Color, c2: Color): Float {
    val dr = c1.red - c2.red
    val dg = c1.green - c2.green
    val db = c1.blue - c2.blue
    return kotlin.math.sqrt(dr * dr + dg * dg + db * db)
}

private fun luminance(color: Color): Float {
    return 0.2126f * color.red + 0.7152f * color.green + 0.0722f * color.blue
}
