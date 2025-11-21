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

            val processed = withContext(Dispatchers.Default) { preprocessAndSort(scannedMusic) }
            withContext(Dispatchers.Main) {
                isScanning = false
                scannedMusicList = processed
                saveScanResult(processed)
                finish()
            }
        }
    }

    private fun saveScanResult(musicList: List<MusicItem>) {
        try {
            MusicCache.save(this, musicList)
        } catch (_: Exception) {}
        val intent = Intent(SCAN_COMPLETE_ACTION)
        intent.putExtra(EXTRA_SONG_COUNT, musicList.size)
        intent.putExtra(EXTRA_TOTAL_SIZE, musicList.sumOf { it.size })
        sendBroadcast(intent)
    }

    private var transliteratorCached: Any? = null
    private var transliterateMethodCached: java.lang.reflect.Method? = null
    private fun initTransliterator() {
        if (transliterateMethodCached != null) return
        if (android.os.Build.VERSION.SDK_INT >= 24) {
            try {
                val cls = Class.forName("android.icu.text.Transliterator")
                val getInstance = cls.getMethod("getInstance", String::class.java)
                transliteratorCached = getInstance.invoke(null, "Han-Latin/Names; Latin-ASCII")
                transliterateMethodCached = cls.getMethod("transliterate", String::class.java)
            } catch (_: Exception) { transliterateMethodCached = null; transliteratorCached = null }
        }
    }

    private fun toHalfWidth(input: String): String {
        val sb = StringBuilder(input.length)
        for (ch in input) {
            val code = ch.code
            if (code == 12288) sb.append(' ')
            else if (code in 65281..65374) sb.append((code - 65248).toChar())
            else sb.append(ch)
        }
        return sb.toString()
    }

    private fun transliterateToAsciiCached(s: String): String {
        val t = toHalfWidth(s).trim()
        if (t.isEmpty()) return ""
        initTransliterator()
        val m = transliterateMethodCached
        val inst = transliteratorCached
        if (m != null && inst != null) {
            try {
                val out = m.invoke(inst, t) as String
                return java.text.Normalizer.normalize(out, java.text.Normalizer.Form.NFD)
                    .replace("\\p{InCombiningDiacriticalMarks}".toRegex(), "")
            } catch (_: Exception) { /* fall through */ }
        }
        return t
    }

    private fun preprocessAndSort(list: List<MusicItem>): List<MusicItem> {
        return list.map { item ->
            val t = toHalfWidth(item.title).trim()
            val group: Int
            val letter: Char
            if (t.isEmpty()) {
                group = 2; letter = '#'
            } else {
                val c = t.first()
                if (c.isDigit()) { group = 0; letter = '0' }
                else if (c in 'A'..'Z' || c in 'a'..'z') { group = 1; letter = c.uppercaseChar() }
                else {
                    val ascii = transliterateToAsciiCached(t)
                    val first = ascii.firstOrNull { it.isLetter() }?.uppercaseChar() ?: '#'
                    group = 1; letter = first
                }
            }
            val asciiFull = transliterateToAsciiCached(t).lowercase()
            item.copy(sortGroup = group, sortLetter = letter, sortAscii = asciiFull)
        }.sortedWith(compareBy(
            { it.sortGroup },
            { it.sortLetter },
            { it.sortAscii }
        ))
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
