package com.example.module_recommened.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import Adapter.MusicDataCache
import com.example.module_recommened.R
import com.therouter.TheRouter
import data.ListMusicData

class LiAdapter : ListAdapter<ListMusicData.Song, LiAdapter.LiViewHolder>(SongDiffCallback()) {

    private var onItemClickListener: ((Int, ListMusicData.Song) -> Unit)? = null


    class SongDiffCallback : DiffUtil.ItemCallback<ListMusicData.Song>() {
        override fun areItemsTheSame(oldItem: ListMusicData.Song, newItem: ListMusicData.Song): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ListMusicData.Song, newItem: ListMusicData.Song): Boolean {
            return oldItem == newItem
        }
    }

    inner class LiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.list_tv)
        val textView1: TextView = itemView.findViewById(R.id.list_tv1)
        val imgView: ImageView = itemView.findViewById(R.id.list_img)

        fun bind(song: ListMusicData.Song) {
            textView.text = song.name
            textView1.text = song.ar.firstOrNull()?.name ?: "未知歌手"
            val cover = song.al.picUrl
            Glide.with(itemView.context)
                .load(cover)
                .error(R.drawable.ic_launcher_background)
                .into(imgView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list, parent, false)
        return LiViewHolder(view)
    }

    override fun onBindViewHolder(holder: LiViewHolder, position: Int) {
        val song = getItem(position)
        val singerId = song.ar.firstOrNull()?.id ?: 0L  // 核心修改
        Log.d("LiAdapter", "歌曲名: ${song.name}, 歌手ID: $singerId")  // 确认是否为0
        holder.bind(song)

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(position, song)
            MusicDataCache.currentSongList = currentList.toList()

            // 构建路由（无需let函数，直接使用singerId）
            val route = TheRouter.build("/module_musicplayer/musicplayer")
                .withString("songListName", song.name)
                .withString("singer", song.ar.firstOrNull()?.name ?: "未知歌手")  // 修复：歌手名应为ar.name
                .withString("cover", song.al.picUrl)
                .withLong("id", song.id)  // 修复：用Long传递歌曲ID
                .withString("athour", song.ar.firstOrNull()?.name ?: "未知歌手")
                .withInt("currentPosition", position)
                .withLong("singerId", singerId)  // 传递非null的Long类型

            route.navigation(holder.itemView.context)
        }
    }
    fun addMoreData(newData: List<ListMusicData.Song>) {
        val currentList = currentList.toMutableList()
        currentList.addAll(newData)
        submitList(currentList)
    }

}