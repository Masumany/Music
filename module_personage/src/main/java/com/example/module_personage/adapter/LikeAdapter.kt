package com.example.module_personage.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.module_login_register.R
import com.example.module_login_register.databinding.LikeItemBinding
import com.example.module_personage.bean.like.Follow

class LikeAdapter (
    private val onItemClick: (Follow) -> Unit,
//    private val onItemLikeClick: (Follow) -> Unit
): androidx.recyclerview.widget.ListAdapter<Follow, LikeAdapter.LikeViewHolder>(
    //用于高效更新数据，帮助实现自动刷新
    object : DiffUtil.ItemCallback<Follow>() {
        //比较两个item是否为同一个
        override fun areItemsTheSame(oldItem: Follow, newItem: Follow): Boolean {
            return oldItem.userId == newItem.userId
        }
        //比较两个item的内容是否相同
        override fun areContentsTheSame(oldItem: Follow, newItem: Follow): Boolean {
            return oldItem == newItem
        }
    }
) {
    //item的布局
    inner class LikeViewHolder(val binding: LikeItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(follow : Follow){
            Glide.with(binding.likeItemImage.context)
                 .load(follow.avatarUrl)
                 .circleCrop()
                 .placeholder(R.drawable.loading)
                 .into(binding.likeItemImage)

            binding.likeItemText.text = follow.nickname

//            binding.likeItemButton.setOnClickListener {
//                onItemLikeClick(follow)
//            }

            binding.root.setOnClickListener {
                onItemClick(follow)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LikeViewHolder {
        val binding = LikeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LikeViewHolder(binding)
    }
    override fun onBindViewHolder(holder: LikeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    class LikeDiffCallback : DiffUtil.ItemCallback<Follow>() {
        override fun areItemsTheSame(oldItem: Follow, newItem: Follow): Boolean {
            return oldItem.userId == newItem.userId
        }
        override fun areContentsTheSame(oldItem: Follow, newItem: Follow): Boolean {
            return oldItem == newItem
        }
    }
}