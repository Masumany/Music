package Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.module_details.R
import com.example.module_details.databinding.ItemTopBinding
import com.example.yourproject.converter.DataConverter
import com.therouter.TheRouter
import data.TopData

class TopAdapter : ListAdapter<TopData.Song, TopAdapter.TopViewHolder>(TopDiffCallback()) {

    private var onItemClickListener: ((Int, TopData.Song) -> Unit)? = null

    class TopDiffCallback : DiffUtil.ItemCallback<TopData.Song>() {
        override fun areItemsTheSame(oldItem: TopData.Song, newItem: TopData.Song): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TopData.Song, newItem: TopData.Song): Boolean {
            return oldItem == newItem
        }
    }

    inner class TopViewHolder(private val binding: ItemTopBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val textView: TextView = binding.topTv
        val img: ImageView = binding.topImg
        val moreImg: ImageView = binding.singerSongMore
        fun bind(song: TopData.Song) {
            textView.text = song.name
            val cover = song.al.picUrl
            Glide.with(itemView.context)
                .load(cover)
                .into(img)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopViewHolder {
        val view = ItemTopBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TopViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopViewHolder, position: Int) {
        val song = getItem(position)
        holder.bind(song)

        holder.moreImg.setOnClickListener {
            showPopupWindow(
                holder.moreImg, song,
                currentPosition = position
            )
        }
        holder.itemView.setOnClickListener {
            val currentSong = song
            val currentPosition = position
            val convertedList = DataConverter.convertTopSongList(currentList)
            MusicDataCache.currentSongList = convertedList // 存入缓存


            TheRouter.build("/module_musicplayer/musicplayer")
                .withLong("id", currentSong.id)
                .withString("songListName", currentSong.name)
                .withString("athour", currentSong.ar.firstOrNull()?.name)
                .withString("cover", currentSong.al.picUrl)
                .withInt("currentPosition", currentPosition) // 传递当前索引
                .navigation(holder.itemView.context)

            onItemClickListener?.invoke(currentPosition, currentSong)
        }

    }

    private fun dpToPx(context: Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }

    private fun showPopupWindow(
        moreImg: ImageView,
        currentSong: TopData.Song?,
        currentPosition: Int
    ) {
        val context = moreImg.context
        val popUpView = LayoutInflater.from(context).inflate(R.layout.popuplayout, null)
        val popupWindow = PopupWindow(
            popUpView,
            dpToPx(context, 150),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.setBackgroundDrawable(context.getDrawable(R.color.white))

        popUpView.findViewById<LinearLayout>(R.id.ll_share).setOnClickListener {
            Toast.makeText(context, "分享《${currentSong?.name}》", Toast.LENGTH_SHORT).show()
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, "推荐歌曲：${currentSong?.name}")
            context.startActivity(Intent.createChooser(intent, "分享歌曲:《${currentSong?.name}》"))
            popupWindow.dismiss()
        }
        popUpView.findViewById<LinearLayout>(R.id.ll_download).setOnClickListener {
            Toast.makeText(context, "开始下载", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(moreImg, 0, 0)
    }
}

