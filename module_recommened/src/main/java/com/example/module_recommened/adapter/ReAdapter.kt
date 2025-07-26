package com.example.module_recommened.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.lib.base.RecommenedData
import com.example.module_recommened.databinding.ItemRecommmenedBinding
import com.therouter.TheRouter


class ReAdapter(private val ReText: List<RecommenedData.Result>) :
    RecyclerView.Adapter<ReAdapter.ReViewHolder>() {

    inner class ReViewHolder(private val binding: ItemRecommmenedBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var textView: TextView = binding.TextView
        val imgView: ImageView = binding.img
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReViewHolder {
        val view =
            ItemRecommmenedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReViewHolder(view)
    }

    override fun getItemCount(): Int {
        return ReText.size
    }

    override fun onBindViewHolder(holder: ReViewHolder, position: Int) {
        val item = ReText[position]
        holder.textView.text = item.name
        Glide.with(holder.imgView.context)
            .load(item.picUrl)
            .into(holder.imgView)
        holder.itemView.setOnClickListener {
            Log.d("ReAdapter", "点击的item.id = ${item.id}")
            try {
                val result = TheRouter.build("/song/SongActivity")
                    .withLong("id", item.id)
                    .withString("recommendName", item.name)
                    .withString("recommendCover", item.picUrl)
                    .withString("recommendAuthor", item.copywriter)
                    .navigation(holder.itemView.context)

            } catch (e: Exception) {
                Log.e("ReAdapter", "跳转发生异常", e)
            }
        }
    }
}