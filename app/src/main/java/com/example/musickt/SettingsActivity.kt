package com.example.musickt

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Window
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.musickt.databinding.ActivitySettingsBinding
import com.example.musickt.databinding.DialogScanResultBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scanMusic()
        } else {
            Toast.makeText(this, "需要存储权限来扫描音乐", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupClickListeners()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        binding.btnScanMusic.setOnClickListener {
            checkPermissionAndScan()
        }
    }

    private fun checkPermissionAndScan() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                scanMusic()
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun scanMusic() {
        binding.btnScanMusic.isEnabled = false
        binding.btnScanMusic.text = "扫描中..."

        CoroutineScope(Dispatchers.IO).launch {
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
                binding.btnScanMusic.isEnabled = true
                binding.btnScanMusic.text = "扫描音乐"
                
                // 保存扫描结果到 SharedPreferences 或数据库
                saveScanResult(scannedMusic)
                
                // 显示扫描结果弹窗
                showScanResultDialog(scannedMusic)
            }
        }
    }

    private fun saveScanResult(musicList: List<MusicItem>) {
        // 这里可以保存到数据库或 SharedPreferences
        // 为简化，我们通过 Intent 传递数据
        val intent = Intent(SCAN_COMPLETE_ACTION)
        intent.putExtra(EXTRA_SONG_COUNT, musicList.size)
        intent.putExtra(EXTRA_TOTAL_SIZE, musicList.sumOf { it.size })
        sendBroadcast(intent)
    }

    private fun showScanResultDialog(musicList: List<MusicItem>) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogScanResultBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        val totalSize = musicList.sumOf { it.size }
        dialogBinding.tvDialogSongCount.text = musicList.size.toString()
        dialogBinding.tvDialogTotalSize.text = formatSize(totalSize)

        dialogBinding.btnStartZenly.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
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
