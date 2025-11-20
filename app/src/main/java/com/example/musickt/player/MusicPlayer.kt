package com.example.musickt.player

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.musickt.MusicItem

class MusicPlayer(context: Context) {
    private val player: ExoPlayer = ExoPlayer.Builder(context).build()
    
    var currentMusic by mutableStateOf<MusicItem?>(null)
        private set
    
    var isPlaying by mutableStateOf(false)
        private set
    
    var currentPosition by mutableStateOf(0L)
        private set
    
    var duration by mutableStateOf(0L)
        private set

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
            
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    duration = player.duration
                }
            }
        })
    }

    fun play(music: MusicItem) {
        if (currentMusic?.id != music.id) {
            currentMusic = music
            val mediaItem = MediaItem.fromUri(music.path)
            player.setMediaItem(mediaItem)
            player.prepare()
        }
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun resume() {
        player.play()
    }

    fun stop() {
        player.stop()
        currentMusic = null
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    fun updateProgress() {
        if (player.isPlaying) {
            currentPosition = player.currentPosition
        }
    }

    fun release() {
        player.release()
    }
}

object MusicPlayerHolder {
    @Volatile
    private var instance: MusicPlayer? = null

    fun get(context: Context): MusicPlayer {
        val appCtx = context.applicationContext
        return instance ?: synchronized(this) {
            instance ?: MusicPlayer(appCtx).also { instance = it }
        }
    }
}
