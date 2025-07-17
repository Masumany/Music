package com.example.module_recommened.adapter
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.module_recommened.R
import com.example.lib.base.BannerData

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

        holder.imageView.setOnClickListener {
            val sendUrl=url.url
            if(sendUrl.isNotEmpty()&&sendUrl.startsWith("http")){
                val intent= Intent(Intent.ACTION_VIEW)
                intent.data=Uri.parse(sendUrl)
                holder.itemView.context.startActivity(intent)
            }else{
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(holder.itemView.context, "暂不支持该类型的链接QAQ~", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

}
