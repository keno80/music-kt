package com.example.musickt

fun buildAlbums(musicList: List<MusicItem>): List<AlbumItem> {
    if (musicList.isEmpty()) return emptyList()
    return musicList
        .groupBy { Pair(it.album.ifBlank { "未知专辑" }, it.artist.ifBlank { "未知艺术家" }) }
        .map { (key, items) ->
            AlbumItem(name = key.first, artist = key.second, songs = items.sortedBy { it.title })
        }
        .sortedBy { it.name }
}