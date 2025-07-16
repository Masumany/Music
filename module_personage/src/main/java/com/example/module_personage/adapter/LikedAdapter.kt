package com.example.module_personage.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.module_login_register.R
import com.example.module_login_register.databinding.LikeItemBinding
import com.example.module_login_register.databinding.LikedItemBinding
import com.example.module_personage.bean.liked.Followed

class LikedAdapter (
    private val onItemClick: (Followed) -> Unit,
): androidx.recyclerview.widget.ListAdapter<Followed, LikedAdapter.LikedViewHolder>(
    //用于高效更新数据，帮助实现自动刷新
    object : DiffUtil.ItemCallback<Followed>() {
        //比较两个item是否为同一个
        override fun areItemsTheSame(oldItem: Followed, newItem: Followed): Boolean {
            return oldItem.userId == newItem.userId
        }
        //比较两个item的内容是否相同
        override fun areContentsTheSame(oldItem: Followed, newItem: Followed): Boolean {
            return oldItem == newItem
        }
    }
) {
    //item的布局
    inner class LikedViewHolder(val binding: LikedItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(followed : Followed){
            Glide.with(binding.likedItemImage.context)
                .load(followed.avatarUrl)
                .circleCrop()
                .placeholder(R.drawable.loading)
                .into(binding.likedItemImage)

            binding.likedItemText.text = followed.nickname

            binding.root.setOnClickListener {
                onItemClick(followed)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikedViewHolder {
        val binding = LikedItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LikedViewHolder(binding)
    }
    override fun onBindViewHolder(holder: LikedViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    class LikedDiffCallback : DiffUtil.ItemCallback<Followed>() {
        override fun areItemsTheSame(oldItem: Followed, newItem: Followed): Boolean {
            return oldItem.userId == newItem.userId
        }
        override fun areContentsTheSame(oldItem: Followed, newItem: Followed): Boolean {
            return oldItem == newItem
        }
    }
}