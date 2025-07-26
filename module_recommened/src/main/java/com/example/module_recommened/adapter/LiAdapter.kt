package com.example.module_recommened.adapter

import Adapter.MusicDataCache
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.module_recommened.R
import com.example.module_recommened.databinding.ItemListBinding
import com.therouter.TheRouter
import data.ListMusicData

class LiAdapter : ListAdapter<ListMusicData.Song, LiAdapter.LiViewHolder>(SongDiffCallback()) {

    private var onItemClickListener: ((Int, ListMusicData.Song) -> Unit)? = null


    class SongDiffCallback : DiffUtil.ItemCallback<ListMusicData.Song>() {
        override fun areItemsTheSame(
            oldItem: ListMusicData.Song,
            newItem: ListMusicData.Song
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ListMusicData.Song,
            newItem: ListMusicData.Song
        ): Boolean {
            return oldItem == newItem
        }
    }

    inner class LiViewHolder(private val binding: ItemListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val textView: TextView = binding.listTv
        val textView1: TextView = binding.listTv1
        val imgView: ImageView = binding.listImg

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
        val view = ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LiViewHolder(view)
    }

    override fun onBindViewHolder(holder: LiViewHolder, position: Int) {
        val song = getItem(position)
        val singerId = song.ar.firstOrNull()?.id ?: 0L
        Log.d("LiAdapter", "歌曲名: ${song.name}, 歌手ID: $singerId")
        holder.bind(song)

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(position, song)
            MusicDataCache.currentSongList = currentList.toList()

            val route = TheRouter.build("/module_musicplayer/musicplayer")
                .withString("songListName", song.name)
                .withString("singer", song.ar.firstOrNull()?.name ?: "未知歌手")
                .withString("cover", song.al.picUrl)
                .withLong("id", song.id)
                .withString("athour", song.ar.firstOrNull()?.name ?: "未知歌手")
                .withInt("currentPosition", position)
                .withLong("singerId", singerId)

            route.navigation(holder.itemView.context)
        }
    }

    fun addMoreData(newData: List<ListMusicData.Song>) {
        val currentList = currentList.toMutableList()
        currentList.addAll(newData)
        submitList(currentList)
    }

}