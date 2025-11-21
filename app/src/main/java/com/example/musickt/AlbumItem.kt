package com.example.musickt

data class AlbumItem(
    val name: String,
    val artist: String,
    val songs: List<MusicItem>
)