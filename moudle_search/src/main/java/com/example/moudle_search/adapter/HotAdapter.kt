package com.example.moudle_search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.moudle_search.bean.searchHot.Hot
import com.example.moudle_search.databinding.HotItemBinding

class HotAdapter (
    private val onItemClick: (Hot) -> Unit
): androidx.recyclerview.widget.ListAdapter<Hot, HotAdapter.HotViewHolder>(
//用于高效更新数据，帮助实现自动刷新
    object : DiffUtil.ItemCallback<Hot>() {
        //比较两个item是否为同一个
        override fun areItemsTheSame(oldItem: Hot, newItem:Hot): Boolean {
            return oldItem.first == newItem.first
        }
        //比较两个item的内容是否相同
        override fun areContentsTheSame(oldItem: Hot, newItem: Hot): Boolean {
            return oldItem == newItem
        }
    }
){
    // ViewHolder：绑定布局控件与数据
    inner class HotViewHolder(private val binding: HotItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(hot: Hot) {

            binding.tvHotItem.text = hot.first

            binding.root.setOnClickListener {
                onItemClick(hot)  // 用于跳转详情
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotViewHolder {
        val binding = HotItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HotViewHolder(binding)
    }
    override fun onBindViewHolder(holder: HotViewHolder, position: Int) {
        holder.bind(getItem( position))
    }

}