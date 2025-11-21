package com.example.musickt.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import com.example.musickt.MusicItem
import com.example.musickt.AlbumArtCache
import com.example.musickt.ui.theme.LocalPlayerTextColor
import android.media.MediaMetadataRetriever
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.AudioFormat
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicListItem(
    music: MusicItem,
    isPlaying: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = null,
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            val cacheKey = "song_" + music.id
            var bitmap by remember(cacheKey) { mutableStateOf<Bitmap?>(AlbumArtCache.get(cacheKey)) }
            LaunchedEffect(music.path) {
                if (bitmap == null) {
                    val loaded = withContext(Dispatchers.IO) {
                        try {
                            val mmr = MediaMetadataRetriever()
                            mmr.setDataSource(music.path)
                            val art = mmr.embeddedPicture
                            mmr.release()
                            art?.let { bytes -> decodeScaledByteArray(bytes, 256) }
                        } catch (_: Exception) {
                            null
                        }
                    }
                    if (loaded != null) {
                        AlbumArtCache.put(cacheKey, loaded)
                        bitmap = loaded
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {}
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                val titleColor = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                val subtitleColor = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                var quality by remember(music.path) { mutableStateOf("") }
                LaunchedEffect(music.path) {
                    quality = withContext(Dispatchers.IO) { detectQualityLabel(music) }
                }
                Text(
                    text = music.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = titleColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    if (quality.isNotEmpty()) {
                        QualityBadge(label = quality, highlight = isPlaying)
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = "${music.artist} · ${music.album}",
                        style = MaterialTheme.typography.bodySmall,
                        color = subtitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }
        }
    }
}

@Composable
private fun QualityBadge(label: String, highlight: Boolean) {
    val content = if (highlight && LocalPlayerTextColor.current != androidx.compose.ui.graphics.Color.Unspecified) LocalPlayerTextColor.current else MaterialTheme.colorScheme.onSurface
    val container = if (highlight && LocalPlayerTextColor.current != androidx.compose.ui.graphics.Color.Unspecified) LocalPlayerTextColor.current.copy(alpha = 0.15f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    Surface(
        color = container,
        contentColor = content,
        shape = RoundedCornerShape(5.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.88f,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.2.dp)
        )
    }
}

private fun detectQualityLabel(music: MusicItem): String {
    try {
        val extractor = MediaExtractor()
        extractor.setDataSource(music.path)
        var bitrate = 0
        var sampleRate = 0
        var bitDepthEnc = 0
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            if (mime.startsWith("audio")) {
                if (format.containsKey(MediaFormat.KEY_BIT_RATE)) bitrate = format.getInteger(MediaFormat.KEY_BIT_RATE)
                if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                if (format.containsKey(MediaFormat.KEY_PCM_ENCODING)) bitDepthEnc = format.getInteger(MediaFormat.KEY_PCM_ENCODING)
                break
            }
        }
        extractor.release()

        val lowerPath = music.path.lowercase()
        val isLosslessExt = lowerPath.endsWith(".flac") || lowerPath.endsWith(".alac") || lowerPath.endsWith(".ape") || lowerPath.endsWith(".wav")
        val nameHint24 = lowerPath.contains("24bit") || lowerPath.contains("24/") || lowerPath.contains("24-")
        val is24bit = bitDepthEnc == AudioFormat.ENCODING_PCM_24BIT_PACKED || bitDepthEnc == AudioFormat.ENCODING_PCM_32BIT || nameHint24 || (sampleRate >= 96000 && isLosslessExt)
        if (is24bit) return "24bit"

        val kbps = if (bitrate > 0) bitrate / 1000 else ((music.size * 8.0) / 1024.0 / (music.duration / 1000.0)).toInt()
        return when {
            sampleRate >= 96000 || kbps >= 800 -> "HR"
            kbps >= 320 -> "HQ"
            kbps >= 192 -> "SQ"
            else -> ""
        }
    } catch (_: Exception) {
        val lowerPath = music.path.lowercase()
        val isLosslessExt = lowerPath.endsWith(".flac") || lowerPath.endsWith(".alac") || lowerPath.endsWith(".ape") || lowerPath.endsWith(".wav")
        val nameHint24 = lowerPath.contains("24bit") || lowerPath.contains("24/") || lowerPath.contains("24-")
        if (nameHint24 || (isLosslessExt && (music.duration > 0 && (music.size * 8.0 / 1024.0 / (music.duration / 1000.0)) >= 800))) return "24bit"
        val kbps = ((music.size * 8.0) / 1024.0 / (music.duration / 1000.0))
        return when {
            kbps >= 800 -> "HR"
            kbps >= 320 -> "HQ"
            kbps >= 192 -> "SQ"
            else -> ""
        }
    }
}

private fun decodeScaledByteArray(bytes: ByteArray, maxSide: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    val maxDim = maxOf(bounds.outWidth, bounds.outHeight)
    var sample = 1
    while (maxDim / sample > maxSide) sample *= 2
    val opts = BitmapFactory.Options().apply {
        inSampleSize = sample
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
}
