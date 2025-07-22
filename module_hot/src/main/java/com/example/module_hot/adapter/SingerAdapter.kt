package com.example.module_hot.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.module_hot.R
import com.example.module_hot.bean.singer.Artist
import com.example.module_hot.databinding.SingerItemBinding
import com.example.module_hot.databinding.SongsListItemBinding

class SingerAdapter (
    private val onItemClick: (Artist) -> Unit,
): androidx.recyclerview.widget.ListAdapter<Artist, SingerAdapter.SingerViewHolder>(
    //用于高效更新数据，帮助实现自动刷新
    object : DiffUtil.ItemCallback<Artist>() {
        //比较两个item是否为同一个
        override fun areItemsTheSame(oldItem: Artist, newItem: Artist): Boolean {
            return oldItem.id == newItem.id
        }
        //比较两个item的内容是否相同
        override fun areContentsTheSame(oldItem: Artist, newItem: Artist): Boolean {
            return oldItem == newItem
        }
    }
) {
    //item的布局
    inner class SingerViewHolder(val binding: SingerItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(singer : Artist){
            Glide.with(binding.singerImg.context)
                .load(singer.picUrl)
                .circleCrop()
                .centerCrop()
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .into(binding.singerImg)

            binding.singerName.text = singer.name
            binding.singerBriefDesc.text = singer.briefDesc
            binding.singerRank.text = singer.lastRank.toString()
            binding.singerScore.text = singer.score.toString()

            binding.root.setOnClickListener {
                onItemClick(singer)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingerViewHolder {
        val binding = SingerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SingerViewHolder(binding)
    }
    override fun onBindViewHolder(holder: SingerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}