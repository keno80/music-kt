package com.example.musickt

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

fun normalizeArtistName(name: String): String {
    val base = toHalfWidth(name).trim()
    val lowered = base.lowercase()
    val cleaned = lowered
        .replace(".", "")
        .replace("·", "")
        .replace("-", "")
        .replace("_", "")
        .replace(Regex("\\s+"), " ")
    return if (cleaned.isBlank()) "未知艺术家" else cleaned
}

fun buildArtists(musicList: List<MusicItem>): List<ArtistItem> {
    if (musicList.isEmpty()) return emptyList()
    return musicList
        .groupBy { normalizeArtistName(it.artist) }
        .map { (_, items) ->
            val displayName = items
                .groupBy { toHalfWidth(it.artist).trim().ifBlank { "未知艺术家" } }
                .maxByOrNull { it.value.size }?.key ?: "未知艺术家"
            ArtistItem(name = displayName, songs = items.sortedBy { it.title })
        }
        .sortedBy { normalizeArtistName(it.name) }
}