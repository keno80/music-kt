package com.example.musickt

data class MusicItem(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val size: Long,
    val path: String,
    val sortAscii: String = "",
    val sortGroup: Int = 2,
    val sortLetter: Char = '#'
)

fun MusicItem.qualityLabel(): String {
    val seconds = duration / 1000.0
    if (seconds <= 0) return ""
    val bitrateKbps = (size * 8.0) / 1024.0 / seconds
    return when {
        bitrateKbps >= 800 -> "HR"
        bitrateKbps >= 256 -> "SQ"
        else -> ""
    }
}
