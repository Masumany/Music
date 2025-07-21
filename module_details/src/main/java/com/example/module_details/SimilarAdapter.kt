package com.example.module_details

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.therouter.TheRouter
import data.SimilarData

class SimilarAdapter (private val similarList: List<SimilarData.Artist>):
        RecyclerView.Adapter<SimilarAdapter.SimilarViewHolder>() {

            inner class SimilarViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
                val textView: TextView =itemView.findViewById(R.id.md_s_text)
                val imgView: ImageView =itemView.findViewById(R.id.md_similar)
            }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimilarViewHolder {
        val view = View.inflate(parent.context, R.layout.item_similar, null)
            return SimilarViewHolder(view)

    }

    override fun getItemCount(): Int {
        return similarList.size
    }

    override fun onBindViewHolder(holder: SimilarViewHolder, position: Int) {
        val item=similarList[position]
        holder.textView.text=item.name
        Glide.with(holder.imgView)
            .load(item.picUrl)
            .centerCrop()
            .circleCrop()
            .into(holder.imgView)
        holder.itemView.setOnClickListener {
            val router=TheRouter.build("/singer/SingerActivity")
            router.withLong("id",item.id.toLong())
                .navigation(holder.itemView.context)
        }
    }
}