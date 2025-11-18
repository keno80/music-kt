package com.example.musickt

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musickt.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var musicAdapter: MusicAdapter
    private val musicList = mutableListOf<MusicItem>()

    private val scanCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // 扫描完成后可以在这里更新UI或加载音乐列表
            // 目前音乐列表为空，等待用户从设置页面扫描
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupClickListeners()
        registerScanReceiver()
    }

    private fun setupRecyclerView() {
        musicAdapter = MusicAdapter(musicList)
        binding.rvMusicList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = musicAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
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

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(scanCompleteReceiver)
    }
}
