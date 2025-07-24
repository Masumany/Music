package com.example.module_hot.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.module_hot.R
import com.example.module_hot.bean.list.Item0
import com.example.module_hot.bean.list.ListItem
import com.example.module_hot.bean.list_songs.Song
import com.example.module_hot.databinding.ListItemBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ListAdapter (
    private val onListItemClick: (ListItem) -> Unit,
): androidx.recyclerview.widget.ListAdapter<ListItem, ListAdapter.ListItemViewHolder>(

    object : DiffUtil.ItemCallback<ListItem>() {
        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem == newItem
        }
    }
) {

    private fun formatTime(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date(timestamp))
    }

    inner class ListItemViewHolder(private val binding: ListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ListItem) {
            binding.apply {
                Glide.with(binding.root)
                    .load(item.songCoverUrl)
                    .placeholder(R.drawable.loading)
                    .error(R.drawable.error)
                    .centerCrop() // 与布局的scaleType一致，避免拉伸
                    .apply(RequestOptions.bitmapTransform(RoundedCorners(16))) // 设置圆角半径
                    .into(binding.listImg)
            }

            binding.listFirst.text = item.firstSongText
            binding.listSecond.text = item.secondSongText
            binding.listThird.text = item.thirdSongText
            binding.listTitle.text = item.listName
            binding.listDate.text = formatTime(item.listUpdateTime)

            binding.root.setOnClickListener {
                onListItemClick(item)  // 用于跳转详情
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemViewHolder {
        val binding = ListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}