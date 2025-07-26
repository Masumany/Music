package com.example.moudle_search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.moudle_search.R
import com.example.moudle_search.bean.Singer2
import com.example.moudle_search.databinding.SingersItemBinding

class SingersAdapter (
    private val onItemClick: (Singer2) -> Unit,
): androidx.recyclerview.widget.ListAdapter<Singer2, SingersAdapter.SingerViewHolder>(
    //用于高效更新数据，帮助实现自动刷新
    object : DiffUtil.ItemCallback<Singer2>() {
        //比较两个item是否为同一个
        override fun areItemsTheSame(oldItem: Singer2, newItem: Singer2): Boolean {
            return oldItem.id == newItem.id
        }
        //比较两个item的内容是否相同
        override fun areContentsTheSame(oldItem: Singer2, newItem: Singer2): Boolean {
            return oldItem == newItem
        }
    }
) {
    //item的布局
    inner class SingerViewHolder(val binding: SingersItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(singer : Singer2){
            Glide.with(binding.singersItemImg.context)
                .load(singer.avatarUrl)
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .centerCrop() // 与布局的scaleType一致，避免拉伸
                .circleCrop()
                .into(binding.singersItemImg)

            binding.singersItemName.text = singer.name

            binding.root.setOnClickListener {
                onItemClick(singer)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingerViewHolder {
        val binding = SingersItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SingerViewHolder(binding)
    }
    override fun onBindViewHolder(holder: SingerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}