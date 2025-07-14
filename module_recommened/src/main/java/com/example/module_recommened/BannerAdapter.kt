package com.example.module_recommened

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.module_recommened.api.BannerData

class BannerAdapter(private val BannnerUrls: List<BannerData.Banner>):
    RecyclerView.Adapter<BannerAdapter.BannerViewHolder>() {

        inner class BannerViewHolder(itemView: ImageView):RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val imageView=LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image,parent,false) as ImageView
        return BannerViewHolder(imageView)
    }

    override fun getItemCount(): Int {
        return BannnerUrls.size
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val url=BannnerUrls[ position]
        Glide.with(holder.imageView.context)
            .load( url.bigImageUrl)
            .into(holder.itemView as ImageView)
    }

}
