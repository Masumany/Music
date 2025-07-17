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
            // 1. 构建路由请求，指定目标 Activity 的路由路径
            val router = TheRouter.build("/module_musicplayer/musicplayer")

            // 2. 可选：传递参数（如果目标 Activity 需要）
            router.withString("id", item.id.toString()) // 传递字符串参数
                .withInt("position", position) // 传递整型参数
            // 其他参数类型：withLong、withBoolean 等

            // 3. 执行跳转（在当前上下文启动 Activity）
            router.navigation(holder.itemView.context)
        }
    }
    }

