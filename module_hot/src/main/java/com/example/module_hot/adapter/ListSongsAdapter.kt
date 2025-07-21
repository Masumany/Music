package com.example.module_hot.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.module_hot.R
import com.example.module_hot.bean.list_songs.Song
import com.example.module_hot.databinding.SongsListItemBinding

class ListSongsAdapter (
    private val onItemClick: (Song) -> Unit,
): androidx.recyclerview.widget.ListAdapter<Song, ListSongsAdapter.SongsListViewHolder>(
    //用于高效更新数据，帮助实现自动刷新
    object : DiffUtil.ItemCallback<Song>() {
        //比较两个item是否为同一个
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }
        //比较两个item的内容是否相同
        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }
) {
    //item的布局
    inner class SongsListViewHolder(val binding: SongsListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(song : Song){
            Glide.with(binding.songsListImg.context)
                .load(song.al.picUrl)
                .circleCrop()
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .into(binding.songsListImg)

            binding.songName.text = song.name
            binding.songSinger.text = song.ar[0].name

            binding.root.setOnClickListener {
                onItemClick(song)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongsListViewHolder {
        val binding = SongsListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongsListViewHolder(binding)
    }
    override fun onBindViewHolder(holder: SongsListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}