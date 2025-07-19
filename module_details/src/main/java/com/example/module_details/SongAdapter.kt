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

class SongAdapter (private val SongText:List<ListMusicData.Song>):
    RecyclerView.Adapter<SongAdapter.SongViewHolder>(){

        inner class SongViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
            var textView: TextView =itemView.findViewById(R.id.list_tv)
            var textView1: TextView =itemView.findViewById(R.id.list_tv1)
            val imgView: ImageView =itemView.findViewById(R.id.list_img)
        }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongAdapter.SongViewHolder {
       val view=LayoutInflater.from(parent.context)
        .inflate(R.layout.item_songlist,parent,false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongAdapter.SongViewHolder, position: Int) {
        val item=SongText[position]
        holder.textView.text=item.name
        holder.textView1.text=item.ar[0].name
        Glide.with(holder.imgView.context).load(item.al.picUrl).into(holder.imgView)
        holder.itemView.setOnClickListener {
            val router=TheRouter.build("/module_musicplayer/musicplayer")
                // 传递歌曲ID（Long类型用withLong）
                .withLong("id", item.id)
                .withString("cover", item.al.picUrl)
                // 传递歌曲名称
                .withString("songListName", item.name)
                // 传递歌手名称
                .withString("athour", item.ar[0].name)
                .withInt("currentPosition", position)
                .withSerializable("songList", ArrayList(SongText))
                Log.d("TAG", "onBindViewHolder: ${item.id}")
                // 传递专辑封面URL


            // 执行跳转
            router.navigation(holder.itemView.context)
        }
    }

    override fun getItemCount(): Int {
        return SongText.size
    }
}