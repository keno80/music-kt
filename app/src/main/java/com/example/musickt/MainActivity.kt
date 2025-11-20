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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
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
            MusicKtTheme {
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
    
    AnimatedGradientBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Zenly") },
                    actions = {
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "设置"
                            )
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
                }
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
