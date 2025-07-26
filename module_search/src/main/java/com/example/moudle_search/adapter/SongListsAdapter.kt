package com.example.moudle_search.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.moudle_search.R
import com.example.moudle_search.bean.Playlist
import com.example.moudle_search.databinding.SongListsItemBinding

class SongListsAdapter (
    private val onItemClick: (Playlist) -> Unit,
): androidx.recyclerview.widget.ListAdapter<Playlist, SongListsAdapter.SongListsViewHolder>(
//用于高效更新数据，帮助实现自动刷新
    object : DiffUtil.ItemCallback<Playlist>() {
        //比较两个item是否为同一个
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem.id == newItem.id
        }
        //比较两个item的内容是否相同
        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem == newItem
        }
    }
){
    // ViewHolder：绑定布局控件与数据
    inner class SongListsViewHolder(private val binding: SongListsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(songLists: Playlist) {

            Glide.with(binding.listsItemImg.context)
                .load(songLists.coverImgUrl)
                .placeholder(R.drawable.loading)  // 加载中占位图
                .error(R.drawable.error)  // 加载失败图
                .centerCrop()  // 与布局的scaleType一致，避免拉伸
                .apply(RequestOptions.bitmapTransform(RoundedCorners(16))) // 设置圆角半径
                .into(binding.listsItemImg)

            binding.listsItemName.text = songLists.name

            binding.listsItemDec.text = songLists.creator?.name

            binding.root.setOnClickListener {
                onItemClick(songLists)  // 用于跳转详情
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongListsViewHolder {
        val binding = SongListsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongListsViewHolder(binding)
    }
    override fun onBindViewHolder(holder: SongListsViewHolder, position: Int) {
        holder.bind(getItem( position))
    }
}