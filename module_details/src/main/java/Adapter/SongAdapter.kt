package Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.Gravity
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
import com.therouter.TheRouter
import data.ListMusicData

object MusicDataCache {
    var currentSongList: List<ListMusicData.Song>? = null // 临时存储列表
}

class SongAdapter(private val songList: List<ListMusicData.Song>) :
    RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    // 只保留前30首歌曲（如果总数不足30则取全部）
    private val limitedSongList = songList.take(30)

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var textView: TextView = itemView.findViewById(R.id.list_tv)
        var textView1: TextView = itemView.findViewById(R.id.list_tv1)
        val imgView: ImageView = itemView.findViewById(R.id.list_img)
        val moreImg: ImageView = itemView.findViewById(R.id.md_more)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_songlist, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val item = limitedSongList[position]
        holder.textView.text = item.name
        holder.textView1.text = item.ar[0].name
        Glide.with(holder.imgView.context).load(item.al.picUrl).into(holder.imgView)

        // 点击更多按钮显示弹窗
        holder.moreImg.setOnClickListener {
            showPopupWindow(holder.moreImg, item)
        }

        holder.itemView.setOnClickListener {
            MusicDataCache.currentSongList = limitedSongList
            val router = TheRouter.build("/module_musicplayer/musicplayer")
                .withLong("id", item.id)
                .withString("cover", item.al.picUrl)
                .withString("songListName", item.name)
                .withString("athour", item.ar[0].name)
                .withInt("currentPosition", position)
            Log.d("TAG", "onBindViewHolder: 跳转携带ID=${item.id}，位置=$position")
            router.navigation(holder.itemView.context)
        }
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
    private fun showPopupWindow(anchor: View, song: ListMusicData.Song) {
        val context = anchor.context
        val popupView = LayoutInflater.from(context).inflate(R.layout.popuplayout, null)

        val popupWindow = PopupWindow(
            popupView,
            dpToPx(context, 150),
            ViewGroup.LayoutParams.WRAP_CONTENT,  // 高度自适应内容
            true // 可点击外部关闭
        )

        popupWindow.setBackgroundDrawable(context.getDrawable(android.R.color.white))

        popupView.findViewById<LinearLayout>(R.id.ll_share).setOnClickListener {
            Toast.makeText(context, "分享: ${song.name}", Toast.LENGTH_SHORT).show()
             val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, "推荐歌曲：${song.name} - ${song.ar[0].name}")
            context.startActivity(Intent.createChooser(intent, "分享歌曲"))
            popupWindow.dismiss() // 关闭弹窗
        }

        popupView.findViewById<LinearLayout>(R.id.ll_download).setOnClickListener {
            Toast.makeText(context, "开始下载: ${song.name}", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss() // 关闭弹窗
        }


        popupWindow.showAsDropDown(anchor, 0, 0, Gravity.END)
    }

    override fun getItemCount(): Int {
        return limitedSongList.size
    }

}
