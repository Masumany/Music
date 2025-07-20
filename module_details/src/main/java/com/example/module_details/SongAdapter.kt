package com.example.module_details

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.therouter.TheRouter
import data.ListMusicData

object MusicDataCache {
    var currentSongList: List<ListMusicData.Song>? = null // 临时存储列表
}

class SongAdapter(private val songList: List<ListMusicData.Song>) :
    RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    // 只保留前30首歌曲（如果总数不足30则取全部）
    private val limitedSongList = songList.take(30)

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView = itemView.findViewById(R.id.list_tv)
        var textView1: TextView = itemView.findViewById(R.id.list_tv1)
        val imgView: ImageView = itemView.findViewById(R.id.list_img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongAdapter.SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_songlist, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongAdapter.SongViewHolder, position: Int) {
        val item = limitedSongList[position]
        holder.textView.text = item.name
        holder.textView1.text = item.ar[0].name
        Glide.with(holder.imgView.context).load(item.al.picUrl).into(holder.imgView)

        holder.itemView.setOnClickListener {
            // 缓存时也只保留30首
            MusicDataCache.currentSongList = limitedSongList
            val router = TheRouter.build("/module_musicplayer/musicplayer")
                .withLong("id", item.id)
                .withString("cover", item.al.picUrl)
                .withString("songListName", item.name)
                .withString("athour", item.ar[0].name)
                .withInt("currentPosition", position)
            Log.d("TAG", "onBindViewHolder: 跳转携带ID=${item.id}，位置=$position")
            router.navigation(holder.itemView.context)
        }

    }

    override fun getItemCount(): Int {
        return limitedSongList.size
    }
}
