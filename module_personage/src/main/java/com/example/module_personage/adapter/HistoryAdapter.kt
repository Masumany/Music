package com.example.module_personage.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.module_login_register.R
import com.example.module_login_register.databinding.HistoryItemBinding
import com.example.module_personage.bean.history.Song

class HistoryAdapter (
    private val onItemClick: (Song) -> Unit,
    private val onPlayClick: (Song) -> Unit
): androidx.recyclerview.widget.ListAdapter<Song, HistoryAdapter.HistoryViewHolder>(
//用于高效更新数据，帮助实现自动刷新
    object : DiffUtil.ItemCallback<Song>() {
        //比较两个item是否为同一个
        override fun areItemsTheSame(oldItem: Song, newItem:Song): Boolean {
            return oldItem.id == newItem.id
        }
        //比较两个item的内容是否相同
        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }
){
    // ViewHolder：绑定布局控件与数据
    inner class HistoryViewHolder(private val binding: HistoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song) {

            Glide.with(binding.historyItemImage.context)
                .load(song.al.picUrl)
                .circleCrop()
                .placeholder(R.drawable.loading)  // 加载中占位图
                .error(R.drawable.error)  // 加载失败图
                .centerCrop()  // 与布局的scaleType一致，避免拉伸
                .into(binding.historyItemImage)

            binding.historyItemSong.text = song.name

            val singerNames = song.ar.joinToString("/") { it.name }  // 多歌手用"/"分隔
            binding.historyItemSinger.text = singerNames

            binding.historyItemButton.setOnClickListener {
                onPlayClick(song)  //后续播放
            }

            binding.root.setOnClickListener {
                onItemClick(song)  // 用于跳转详情
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = HistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem( position))
    }
}