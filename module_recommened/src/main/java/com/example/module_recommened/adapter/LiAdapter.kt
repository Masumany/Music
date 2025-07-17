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
import com.example.lib.base.Song
import com.example.module_recommened.R
import com.therouter.TheRouter
import com.therouter.router.Route

class LiAdapter: ListAdapter<Song, LiAdapter.LiViewHolder>(SongDiffCallback()) {
    class SongDiffCallback: DiffUtil.ItemCallback<Song>() {


        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.name == newItem.name
        }
        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }
            inner class LiViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
                var textView: TextView =itemView.findViewById(R.id.list_tv)
                var textView1: TextView =itemView.findViewById(R.id.list_tv1)
                val imgView: ImageView =itemView.findViewById(R.id.list_img)
            }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list, parent, false)
        return LiViewHolder(view)
    }

    override fun onBindViewHolder(holder: LiViewHolder, position: Int) {
        val song= getItem( position)

        if (song != null) {
            holder.textView.text = song.name
        }
        if (song != null) {
            holder.textView1.text = song.ar.firstOrNull()?.name?:"未知歌手"
        }
        val id=song.id
        val cover= song?.al?.picUrl
        if (cover.isNullOrEmpty()){
            holder.imgView.setImageResource(R.drawable.ic_launcher_background)
        }else{
            Glide.with(holder.itemView.context)
                .load(cover)
                .error(R.drawable.ic_launcher_background)
                .into(holder.imgView)

        }
        holder.itemView.setOnClickListener {
            val route=TheRouter.build("/module_musicplayer/musicplayer")
                .withString("songListName",song.name)
                .withString("singer",song.al.name)
                .withString("cover",cover)
                .withString("id",id.toString())
                .withString("athour",song.ar.firstOrNull()?.name?:"未知歌手")
            route.navigation(holder.itemView.context)
        }
    }
    fun addMoreData(newData: List<Song>) {
        val currentList = currentList.toMutableList()
        currentList.addAll(newData)
        submitList(currentList)
    }
}