package com.example.module_recommened.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lib.base.RecommenedData
import com.example.module_recommened.R


class ReAdapter (private val ReText:List<RecommenedData.Result>):
    RecyclerView.Adapter<ReAdapter.ReViewHolder>(){

    inner class ReViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var textView: TextView =itemView.findViewById(R.id.TextView)
        val imgView:ImageView=itemView.findViewById(R.id.img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReViewHolder {
        val view=LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommmened,parent,false)
        return ReViewHolder(view)
    }

    override fun getItemCount(): Int {
        return ReText.size
    }

    override fun onBindViewHolder(holder: ReViewHolder, position: Int) {
        val item=ReText[position]
        holder.textView.text=item.name
        Glide.with(holder.imgView.context)
            .load(item.picUrl)
            .into(holder.imgView )

    }

}