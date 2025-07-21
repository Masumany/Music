package com.example.module_details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import data.SingerMvData

class SingMvAdapter (private val singMvList: List<SingerMvData.Mv>):
    RecyclerView.Adapter<SingMvAdapter.SingMvViewHolder>() {

        inner class SingMvViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
            var textView: TextView =itemView.findViewById(R.id.singer_mv_text)
            val imgView: ImageView =itemView.findViewById(R.id.singer_mv_img)
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SingMvViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_singermv,parent,false)
        return SingMvViewHolder(view)
    }

    override fun getItemCount(): Int {
        return singMvList.size
    }

    override fun onBindViewHolder(holder: SingMvViewHolder, position: Int) {
        val item=singMvList[position]
        holder.textView.text=item.name
        Glide.with(holder.imgView.context).load(item.imgurl).into(holder.imgView)


    }
}