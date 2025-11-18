package com.example.musickt.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.musickt.MusicItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicListItem(
    music: MusicItem,
    isPlaying: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = null,
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                androidx.compose.ui.graphics.Color.Transparent
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = music.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = music.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration(music.duration),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Text(
                    text = formatSize(music.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

private fun formatDuration(duration: Long): String {
    val seconds = (duration / 1000) % 60
    val minutes = (duration / (1000 * 60)) % 60
    val hours = (duration / (1000 * 60 * 60))
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}

private fun formatSize(size: Long): String {
    val kb = size / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1) {
        String.format("%.2f MB", mb)
    } else {
        String.format("%.2f KB", kb)
    }
}
