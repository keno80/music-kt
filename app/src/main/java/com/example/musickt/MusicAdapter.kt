package com.example.musickt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.musickt.databinding.ItemMusicBinding

class MusicAdapter(private var musicList: List<MusicItem>) :
    RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    inner class MusicViewHolder(private val binding: ItemMusicBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(music: MusicItem) {
            binding.tvTitle.text = music.title
            binding.tvArtist.text = music.artist
            binding.tvDuration.text = formatDuration(music.duration)
            binding.tvSize.text = formatSize(music.size)
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val binding = ItemMusicBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MusicViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        holder.bind(musicList[position])
    }

    override fun getItemCount(): Int = musicList.size

    fun updateList(newList: List<MusicItem>) {
        musicList = newList
        notifyDataSetChanged()
    }
}
