package com.example.moudle_search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moudle_search.R
import com.example.moudle_search.databinding.SongsItemBinding
import data.ListMusicData

class SongsAdapter(
    var onItemClick: ((ListMusicData.Song) -> Unit)?
) : androidx.recyclerview.widget.ListAdapter<ListMusicData.Song, SongsAdapter.SongsViewHolder>(
    // 改为ListMusicData.Song的DiffUtil回调
    object : DiffUtil.ItemCallback<ListMusicData.Song>() {
        override fun areItemsTheSame(oldItem: ListMusicData.Song, newItem: ListMusicData.Song): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ListMusicData.Song, newItem: ListMusicData.Song): Boolean {
            return oldItem == newItem
        }
    }
) {
    // 保持原有ViewHolder逻辑，仅修改绑定数据类型
    inner class SongsViewHolder(private val binding: SongsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(song: ListMusicData.Song) {
            // 专辑封面加载逻辑不变
            Glide.with(binding.songsItemImg.context)
                .load(song.al.picUrl)
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .centerCrop()
                .circleCrop()
                .into(binding.songsItemImg)

            // 歌曲名称显示逻辑不变
            binding.songItemName.text = song.name

            // 歌手名称拼接逻辑不变
            val singerNames = song.ar.joinToString("/") { it.name }
            binding.songItemSinger.text = singerNames

            // 点击事件传递转换后的数据
            binding.root.setOnClickListener {
                onItemClick?.let { it1 -> it1(song) }
            }
        }
    }

    // 保持原有ViewHolder创建逻辑
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongsViewHolder {
        val binding = SongsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongsViewHolder(binding)
    }

    // 绑定转换后的数据
    override fun onBindViewHolder(holder: SongsViewHolder, position: Int) {
        val song = getItem(position)
        holder.bind(song)
    }
}
