package com.example.musickt

import android.content.Context
import android.util.JsonReader
import android.util.JsonWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

object MusicCache {
    private const val FILE_NAME = "music_cache.json"

    fun exists(context: Context): Boolean {
        return try { context.getFileStreamPath(FILE_NAME)?.exists() == true } catch (_: Exception) { false }
    }

    fun save(context: Context, list: List<MusicItem>) {
        try {
            context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use { fos ->
                val writer = JsonWriter(OutputStreamWriter(fos, Charsets.UTF_8))
                writer.beginArray()
                for (m in list) {
                    writer.beginObject()
                    writer.name("id").value(m.id)
                    writer.name("title").value(m.title)
                    writer.name("artist").value(m.artist)
                    writer.name("album").value(m.album)
                    writer.name("duration").value(m.duration)
                    writer.name("size").value(m.size)
                    writer.name("path").value(m.path)
                    writer.endObject()
                }
                writer.endArray()
                writer.close()
            }
        } catch (_: Exception) {}
    }

    fun load(context: Context): List<MusicItem> {
        try {
            if (!exists(context)) return emptyList()
            context.openFileInput(FILE_NAME).use { fis ->
                val reader = JsonReader(InputStreamReader(fis, Charsets.UTF_8))
                val list = mutableListOf<MusicItem>()
                reader.beginArray()
                while (reader.hasNext()) {
                    var id = 0L
                    var title = ""
                    var artist = ""
                    var album = ""
                    var duration = 0L
                    var size = 0L
                    var path = ""
                    reader.beginObject()
                    while (reader.hasNext()) {
                        when (reader.nextName()) {
                            "id" -> id = reader.nextLong()
                            "title" -> title = reader.nextString()
                            "artist" -> artist = reader.nextString()
                            "album" -> album = reader.nextString()
                            "duration" -> duration = reader.nextLong()
                            "size" -> size = reader.nextLong()
                            "path" -> path = reader.nextString()
                            else -> reader.skipValue()
                        }
                    }
                    reader.endObject()
                    list.add(MusicItem(id, title, artist, album, duration, size, path))
                }
                reader.endArray()
                reader.close()
                return list
            }
        } catch (_: Exception) {}
        return emptyList()
    }
}