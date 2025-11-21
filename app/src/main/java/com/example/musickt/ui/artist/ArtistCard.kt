package com.example.musickt.ui.artist

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import com.example.musickt.ArtistItem
import com.example.musickt.AlbumArtCache
import com.example.musickt.ui.theme.LocalPlayerTextColor
import android.media.MediaMetadataRetriever
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ArtistCard(artist: ArtistItem) {
    val path = artist.songs.firstOrNull()?.path
    val cacheKey = "artist_" + artist.name
    var bitmap by remember(cacheKey) { mutableStateOf<Bitmap?>(AlbumArtCache.get(cacheKey)) }

    LaunchedEffect(path) {
        if (bitmap == null && path != null) {
            val loaded = withContext(Dispatchers.IO) {
                try {
                    val mmr = MediaMetadataRetriever()
                    mmr.setDataSource(path)
                    val art = mmr.embeddedPicture
                    mmr.release()
                    art?.let { bytes -> decodeScaledByteArray(bytes, 512) }
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

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${artist.songs.size} 首 · ${artist.name}",
                style = MaterialTheme.typography.titleSmall,
                color = if (LocalPlayerTextColor.current == Color.Unspecified) MaterialTheme.colorScheme.onSurface else LocalPlayerTextColor.current,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
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