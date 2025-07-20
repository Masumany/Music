package com.example.module_recommened.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.module_details.MusicDataCache
import com.example.module_recommened.R
import com.example.yourproject.converter.DataConverter
import com.therouter.TheRouter
import data.ListMusicData
import com.example.lib.base.Song as BaseSong  // 仅作为转换用

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
        holder.bind(song)

        holder.itemView.setOnClickListener {
            // 触发外部监听器（传递 ListMusicData.Song）
            onItemClickListener?.invoke(position, song)
            val convertedList = currentList.toList()
            MusicDataCache.currentSongList = convertedList

            val route = TheRouter.build("/module_musicplayer/musicplayer")
                .withString("songListName", song.name)
                .withString("singer", song.al.name)
                .withString("cover", song.al.picUrl)
                .withString("id", song.id.toString())
                .withString("athour", song.ar.firstOrNull()?.name ?: "未知歌手")
                .withInt("currentPosition", position)

            route.navigation(holder.itemView.context)
        }
    }

    fun addMoreData(newData: List<ListMusicData.Song>) {
        val currentList = currentList.toMutableList()
        currentList.addAll(newData)
        submitList(currentList)
    }

}