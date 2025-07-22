package Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.module_details.R
import com.example.module_details.databinding.ItemSimilarBinding
import com.therouter.TheRouter
import data.SimilarData

class SimilarAdapter (private val similarList: List<SimilarData.Artist>):
        RecyclerView.Adapter<SimilarAdapter.SimilarViewHolder>() {

            inner class SimilarViewHolder(private val  binding: ItemSimilarBinding):RecyclerView.ViewHolder(binding.root) {
                val textView: TextView =binding.mdSText
                val imgView: ImageView =binding.mdSimilar
            }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimilarViewHolder {
        val view = ItemSimilarBinding.inflate(  LayoutInflater.from(parent.context),
            parent,
            false )
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