package com.example.module_hot.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.module_hot.R
import com.example.module_hot.bean.mv.Data
import com.example.module_hot.databinding.MvRankItemBinding
import com.example.module_hot.databinding.SingerItemBinding

class MvAdapter (
    private val onItemClick: (Data) -> Unit,
): androidx.recyclerview.widget.ListAdapter<Data, MvAdapter.MvViewHolder>(
    //用于高效更新数据，帮助实现自动刷新
    object : DiffUtil.ItemCallback<Data>() {
        //比较两个item是否为同一个
        override fun areItemsTheSame(oldItem: Data, newItem: Data): Boolean {
            return oldItem.id == newItem.id
        }
        //比较两个item的内容是否相同
        override fun areContentsTheSame(oldItem: Data, newItem: Data): Boolean {
            return oldItem == newItem
        }
    }
) {
    //item的布局
    inner class MvViewHolder(val binding: MvRankItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(data : Data){
            Glide.with(binding.mvImg.context)
                .load(data.cover)
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .centerCrop() // 与布局的scaleType一致，避免拉伸
                .apply(RequestOptions.bitmapTransform(RoundedCorners(16))) // 设置圆角半径
                .into(binding.mvImg)

            binding.mvName.text = data.name
            binding.mvArtistName.text = data.artistName

            binding.root.setOnClickListener {
                onItemClick(data)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MvViewHolder {
        val binding = MvRankItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MvViewHolder(binding)
    }
    override fun onBindViewHolder(holder: MvViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}