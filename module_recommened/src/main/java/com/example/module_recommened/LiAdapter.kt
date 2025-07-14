package com.example.module_recommened

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.module_recommened.model.ListData

class LiAdapter(private val LiText: ListData):
        RecyclerView.Adapter<LiAdapter.LiViewHolder>() {
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

    override fun getItemCount(): Int {
        return LiText.data?.dailySongs?.size?:0
    }

    override fun onBindViewHolder(holder: LiViewHolder, position: Int) {
        val song= LiText.data?.dailySongs?.get(position)

        if (song != null) {
            holder.textView.text = song.name
        }
        if (song != null) {
            holder.textView1.text = song.ar.firstOrNull()?.name?:"未知歌手"
        }

        val cover= song?.al?.picUrl
        if (cover.isNullOrEmpty()){
            holder.imgView.setImageResource(R.drawable.ic_launcher_background)
        }else{
            Glide.with(holder.itemView.context)
                .load(cover)
                .error(R.drawable.ic_launcher_background)
                .into(holder.imgView)

        }
    }
}