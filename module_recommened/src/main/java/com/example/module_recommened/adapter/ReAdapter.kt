package com.example.module_recommened.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lib.base.RecommenedData
import com.example.module_recommened.R
import com.therouter.TheRouter


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
        holder.itemView.setOnClickListener {
            Log.d("ReAdapter", "点击的item.id = ${item.id}")
            try {
                // TheRouter跳转并获取返回值
                val result = TheRouter.build("/song/SongActivity")
                    .withLong("id", item.id)
                    .withString("recommendName", item.name)
                    .withString("recommendCover", item.picUrl)
                    .navigation(holder.itemView.context)

            } catch (e: Exception) {
                // 捕获所有异常（如类找不到、权限问题等）
                Log.e("ReAdapter", "跳转发生异常", e)
            }
        }
    }
    }

