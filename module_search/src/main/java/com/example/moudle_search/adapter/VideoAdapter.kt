package com.example.moudle_search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.moudle_search.R
import com.example.moudle_search.bean.MV
import com.example.moudle_search.databinding.VideosItemBinding

class VideoAdapter (
    private val onItemClick: (MV) -> Unit,
): androidx.recyclerview.widget.ListAdapter<MV, VideoAdapter.VideoViewHolder>(
    //用于高效更新数据，帮助实现自动刷新
    object : DiffUtil.ItemCallback<MV>() {
        //比较两个item是否为同一个
        override fun areItemsTheSame(oldItem: MV, newItem: MV): Boolean {
            return oldItem.id == newItem.id
        }
        //比较两个item的内容是否相同
        override fun areContentsTheSame(oldItem: MV, newItem: MV): Boolean {
            return oldItem == newItem
        }
    }
) {
    //item的布局
    inner class VideoViewHolder(val binding: VideosItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(video : MV){
            Glide.with(binding.videosItemImg.context)
                .load(video.coverUrl)
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .centerCrop() // 与布局的scaleType一致，避免拉伸
                .apply(RequestOptions.bitmapTransform(RoundedCorners(16))) // 设置圆角半径
                .into(binding.videosItemImg)

            binding.videosItemName.text = video.name
            binding.videosItemArtistName.text = video.creator?.name ?:"未知"

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