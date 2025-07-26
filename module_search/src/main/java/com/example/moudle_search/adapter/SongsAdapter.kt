package com.example.moudle_search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moudle_search.R
import com.example.moudle_search.bean.Song1
import com.example.moudle_search.databinding.SongsItemBinding

class SongsAdapter (
    private val onItemClick: (Song1) -> Unit
): androidx.recyclerview.widget.ListAdapter<Song1, SongsAdapter.SongsViewHolder>(
//用于高效更新数据，帮助实现自动刷新
    object : DiffUtil.ItemCallback<Song1>() {
        //比较两个item是否为同一个
        override fun areItemsTheSame(oldItem: Song1, newItem:Song1): Boolean {
            return oldItem.id == newItem.id
        }
        //比较两个item的内容是否相同
        override fun areContentsTheSame(oldItem: Song1, newItem: Song1): Boolean {
            return oldItem == newItem
        }
    }
){
    // ViewHolder：绑定布局控件与数据
    inner class SongsViewHolder(private val binding: SongsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song1) {

            Glide.with(binding.songsItemImg.context)
                .load(song.al.picUrl)
                .placeholder(R.drawable.loading)  // 加载中占位图
                .error(R.drawable.error)  // 加载失败图
                .centerCrop()  // 与布局的scaleType一致，避免拉伸
                .circleCrop()
                .into(binding.songsItemImg)

            binding.songItemName.text = song.name

            val singerNames = song.ar.joinToString("/") { it.name }  // 多歌手用"/"分隔
            binding.songItemSinger.text = singerNames

            binding.root.setOnClickListener {
                onItemClick(song)  // 用于跳转
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongsViewHolder {
        val binding = SongsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongsViewHolder(binding)
    }
    override fun onBindViewHolder(holder: SongsViewHolder, position: Int) {
        holder.bind(getItem( position))
    }
}