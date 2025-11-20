package com.example.musickt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import android.media.MediaMetadataRetriever
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import com.example.musickt.player.MusicPlayer
import com.example.musickt.player.MusicPlayerHolder
import com.example.musickt.ui.components.AnimatedGradientBackground
import com.example.musickt.ui.components.MusicListItem
import com.example.musickt.ui.components.MusicPlayerBar
import com.example.musickt.ui.theme.MusicKtTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val musicList = mutableStateListOf<MusicItem>()
    private lateinit var musicPlayer: MusicPlayer

    private val scanCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loadMusicList()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        musicPlayer = MusicPlayerHolder.get(this)
        registerScanReceiver()
        loadMusicList()
        
        setContent {
            MusicKtTheme(currentMusic = musicPlayer.currentMusic) {
                MainScreen(
                    musicList = musicList,
                    musicPlayer = musicPlayer,
                    onSettingsClick = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    }
                )
            }
        }
    }

    private fun registerScanReceiver() {
        val filter = IntentFilter(SettingsActivity.SCAN_COMPLETE_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(scanCompleteReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(scanCompleteReceiver, filter)
        }
    }

    override fun onResume() {
        super.onResume()
        loadMusicList()
    }

    private fun loadMusicList() {
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            val scannedMusic = mutableListOf<MusicItem>()
            
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE,
                MediaStore.Audio.Media.DATA
            )

            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

            try {
                contentResolver.query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    projection,
                    selection,
                    null,
                    sortOrder
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                    val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                    val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                    val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                    val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                    val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val title = cursor.getString(titleColumn) ?: "Unknown"
                        val artist = cursor.getString(artistColumn) ?: "Unknown Artist"
                        val album = cursor.getString(albumColumn) ?: "Unknown Album"
                        val duration = cursor.getLong(durationColumn)
                        val size = cursor.getLong(sizeColumn)
                        val path = cursor.getString(dataColumn) ?: ""

                        scannedMusic.add(
                            MusicItem(id, title, artist, album, duration, size, path)
                        )
                    }
                }
            } catch (e: SecurityException) {
                // 没有权限时忽略
            }

            withContext(Dispatchers.Main) {
                musicList.clear()
                musicList.addAll(scannedMusic)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(scanCompleteReceiver)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    musicList: List<MusicItem>,
    musicPlayer: MusicPlayer,
    onSettingsClick: () -> Unit
) {
    var currentMusicIndex by remember { mutableStateOf(-1) }
    
    LaunchedEffect(musicPlayer.isPlaying) {
        while (musicPlayer.isPlaying) {
            musicPlayer.updateProgress()
            delay(100)
        }
    }
    
    AnimatedGradientBackground(currentMusic = musicPlayer.currentMusic, colorTransitionDurationMs = 900) {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Zenly") },
                    actions = {
                        val bitmap = remember(musicPlayer.currentMusic?.path) {
                            musicPlayer.currentMusic?.path?.let { p ->
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
                        val dom = remember(bitmap) { bitmap?.let { dominantColorsTopBar(it) } }
                        val base = dom?.getOrNull(0) ?: MaterialTheme.colorScheme.surfaceVariant
                        val targetCapsule = lighten(base, 0.18f)
                        val capsuleColor by animateColorAsState(targetValue = targetCapsule, animationSpec = tween(500, easing = FastOutSlowInEasing), label = "settingsCapsule")
                        Box(modifier = Modifier.padding(end = 16.dp)) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                color = capsuleColor,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp,
                                shape = CircleShape
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clickable(onClick = onSettingsClick),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "设置",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        actionIconContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            bottomBar = {
            MusicPlayerBar(
                currentMusic = musicPlayer.currentMusic,
                isPlaying = musicPlayer.isPlaying,
                currentPosition = musicPlayer.currentPosition,
                duration = musicPlayer.duration,
                modifier = Modifier.padding(bottom = 28.dp),
                onPlayPauseClick = {
                    if (musicPlayer.isPlaying) {
                        musicPlayer.pause()
                    } else {
                        musicPlayer.resume()
                    }
                },
                onNextClick = {
                    if (currentMusicIndex < musicList.size - 1) {
                        currentMusicIndex++
                        musicPlayer.play(musicList[currentMusicIndex])
                    }
                },
                onPreviousClick = {
                    if (currentMusicIndex > 0) {
                        currentMusicIndex--
                        musicPlayer.play(musicList[currentMusicIndex])
                    }
                },
                colorTransitionDurationMs = 500
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(musicList.size) { index ->
                val music = musicList[index]
                MusicListItem(
                    music = music,
                    isPlaying = musicPlayer.currentMusic?.id == music.id,
                    onClick = {
                        currentMusicIndex = index
                        musicPlayer.play(music)
                    }
                )
            }
        }
        }
    }
}

private fun dominantColorsTopBar(bitmap: Bitmap, count: Int = 1): List<Color> {
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

private fun lighten(color: Color, amount: Float): Color {
    val a = color.alpha
    val r = (color.red + (1f - color.red) * amount).coerceIn(0f, 1f)
    val g = (color.green + (1f - color.green) * amount).coerceIn(0f, 1f)
    val b = (color.blue + (1f - color.blue) * amount).coerceIn(0f, 1f)
    return Color(r, g, b, a)
}
