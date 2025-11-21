package com.example.musickt.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.musickt.MusicItem

@Composable
fun SongsList(
    musicList: List<MusicItem>,
    isPlayingId: Long?,
    onItemClick: (index: Int, item: MusicItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(musicList) { index, item ->
                MusicListItem(
                    music = item,
                    isPlaying = isPlayingId == item.id,
                    onClick = { onItemClick(index, item) }
                )
            }
        }
    }
}