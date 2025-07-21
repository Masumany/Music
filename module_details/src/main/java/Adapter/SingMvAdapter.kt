package Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.module_details.R
import data.SingerMvData

class SingMvAdapter (private val singMvList: List<SingerMvData.Mv>):
    RecyclerView.Adapter<SingMvAdapter.SingMvViewHolder>() {

        inner class SingMvViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
            var textView: TextView =itemView.findViewById(R.id.singer_mv_text)
            val imgView: ImageView =itemView.findViewById(R.id.singer_mv_img)
            val moreImg: ImageView =itemView.findViewById(R.id.singer_mv_more)
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
        holder.moreImg.setOnClickListener{
            showPopupWindow(holder.moreImg, item)
        }

    }
    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
    private fun showPopupWindow(moreImg: ImageView, item: SingerMvData.Mv) {
            val context = moreImg.context
            val popupView=LayoutInflater.from(context).inflate(R.layout.popuplayout, null)
            val popupWindow = PopupWindow(popupView, dpToPx(context, 150), ViewGroup.LayoutParams.WRAP_CONTENT,true)

        popupWindow.setBackgroundDrawable(context.getDrawable(R.color.white))

        popupView.findViewById<LinearLayout>(R.id.ll_share).setOnClickListener {
            Toast.makeText(context, "分享", Toast.LENGTH_SHORT).show()
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, "推荐MV${item.name}")
            context.startActivity(Intent.createChooser(intent, "分享MV《${item.name}》"))
            popupWindow.dismiss()
        }
        popupView.findViewById<LinearLayout>(R.id.ll_download).setOnClickListener {
            Toast.makeText(context, "开始下载《${item.name}》", Toast.LENGTH_SHORT)
            popupWindow.dismiss()

        }
        popupWindow.showAsDropDown(moreImg, 0, 0)
    }

}