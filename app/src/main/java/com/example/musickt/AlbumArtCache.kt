package com.example.musickt

import android.graphics.Bitmap
import androidx.collection.LruCache

object AlbumArtCache {
    private val cacheSize = (Runtime.getRuntime().maxMemory() / 1024 / 16).toInt()
    private val lru = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, value: Bitmap): Int {
            return (value.byteCount / 1024)
        }
    }

    fun get(key: String): Bitmap? = lru.get(key)
    fun put(key: String, bitmap: Bitmap) { lru.put(key, bitmap) }
}