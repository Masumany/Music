package com.example.module_details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yourproject.converter.DataConverter
import com.therouter.TheRouter
import data.TopData

class TopAdapter :ListAdapter<TopData.Song,TopAdapter.TopViewHolder>(TopDiffCallback()){

    private var onItemClickListener:((Int,TopData.Song)->Unit)?=null
    class TopDiffCallback:DiffUtil.ItemCallback<TopData.Song>() {
        override fun areItemsTheSame(oldItem: TopData.Song, newItem: TopData.Song): Boolean {
            return oldItem.id==newItem.id
        }

        override fun areContentsTheSame(oldItem: TopData.Song, newItem: TopData.Song): Boolean {
            return oldItem==newItem
        }
    }

        inner class TopViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
            val textView: TextView =itemView.findViewById(R.id.top_tv)
            val img:ImageView=itemView.findViewById(R.id.top_img)
            fun bind(song: TopData.Song){
                textView.text = song.name
                val cover=song.al.picUrl
                Glide.with(itemView.context)
                    .load(cover)
                    .into(img)

            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopViewHolder {
        val view=LayoutInflater.from(parent.context)
            .inflate(R.layout.item_top,parent,false)
        return TopViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopViewHolder, position: Int) {
        val song=getItem(position)
        holder.bind(song)
        // 在TopAdapter的onBindViewHolder中
        holder.itemView.setOnClickListener {
            val currentSong = song
            val currentPosition = position // 当前点击的索引

            val convertedList = DataConverter.convertTopSongList(currentList)
            MusicDataCache.currentSongList = convertedList // 存入缓存

            // 2. 传递必要参数（包含当前索引）
            TheRouter.build("/module_musicplayer/musicplayer")
                .withLong("id", currentSong.id)
                .withString("songListName", currentSong.name)
                .withString("athour", currentSong.ar.firstOrNull()?.name)
                .withString("cover", currentSong.al.picUrl)
                .withInt("currentPosition", currentPosition) // 传递当前索引
                .navigation(holder.itemView.context)

            // 3. 回调点击事件（可选）
            onItemClickListener?.invoke(currentPosition, currentSong)
        }

    }

}

