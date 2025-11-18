package com.example.musickt

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.musickt.ui.components.AnimatedGradientBackground
import com.example.musickt.ui.components.ScanResultDialog
import com.example.musickt.ui.theme.MusicKtTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scanMusic()
        } else {
            Toast.makeText(this, "需要存储权限来扫描音乐", Toast.LENGTH_SHORT).show()
        }
    }
    
    private var showDialog by mutableStateOf(false)
    private var scannedMusicList by mutableStateOf<List<MusicItem>>(emptyList())
    private var isScanning by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            MusicKtTheme {
                SettingsScreen(
                    isScanning = isScanning,
                    onBackClick = { finish() },
                    onScanClick = { checkPermissionAndScan() }
                )
                
                if (showDialog) {
                    val totalSize = scannedMusicList.sumOf { it.size }
                    ScanResultDialog(
                        songCount = scannedMusicList.size,
                        totalSize = formatSize(totalSize),
                        onDismiss = { showDialog = false },
                        onStartZenly = {
                            showDialog = false
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun checkPermissionAndScan() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                scanMusic()
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun scanMusic() {
        isScanning = true

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

            withContext(Dispatchers.Main) {
                isScanning = false
                scannedMusicList = scannedMusic
                saveScanResult(scannedMusic)
                showDialog = true
            }
        }
    }

    private fun saveScanResult(musicList: List<MusicItem>) {
        val intent = Intent(SCAN_COMPLETE_ACTION)
        intent.putExtra(EXTRA_SONG_COUNT, musicList.size)
        intent.putExtra(EXTRA_TOTAL_SIZE, musicList.sumOf { it.size })
        sendBroadcast(intent)
    }

    private fun formatSize(size: Long): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1 -> String.format("%.2f GB", gb)
            mb >= 1 -> String.format("%.2f MB", mb)
            else -> String.format("%.2f KB", kb)
        }
    }

    companion object {
        const val SCAN_COMPLETE_ACTION = "com.example.musickt.SCAN_COMPLETE"
        const val EXTRA_SONG_COUNT = "song_count"
        const val EXTRA_TOTAL_SIZE = "total_size"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isScanning: Boolean,
    onBackClick: () -> Unit,
    onScanClick: () -> Unit
) {
    AnimatedGradientBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("设置") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "音乐库管理",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "扫描设备上的音乐文件",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = onScanClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isScanning
                    ) {
                        Text(if (isScanning) "扫描中..." else "扫描音乐")
                    }
                }
            }
        }
        }
    }
}
