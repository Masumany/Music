package com.example.moudle_search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moudle_search.R
import com.example.moudle_search.bean.search.Video
import com.example.moudle_search.databinding.VideosItemBinding

class VideoAdapter (
    private val onItemClick: (Video) -> Unit,
): androidx.recyclerview.widget.ListAdapter<Video, VideoAdapter.VideoViewHolder>(
    //用于高效更新数据，帮助实现自动刷新
    object : DiffUtil.ItemCallback<Video>() {
        //比较两个item是否为同一个
        override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean {
            return oldItem.id == newItem.id
        }
        //比较两个item的内容是否相同
        override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean {
            return oldItem == newItem
        }
    }
) {
    //item的布局
    inner class VideoViewHolder(val binding: VideosItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(video : Video){
            Glide.with(binding.videosItemImg.context)
                .load(video.coverUrl)
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .centerCrop() // 与布局的scaleType一致，避免拉伸
                .circleCrop() // 圆形
                .into(binding.videosItemImg)

            binding.videosItemName.text = video.title
            binding.videosItemArtistName.text = video.playCount.toString()

            binding.root.setOnClickListener {
                onItemClick(video)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = VideosItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }
    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}