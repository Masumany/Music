package com.example.module_musicplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import data.CommentData

class CommentAdapter(
    // 初始化时可以传空列表
    private var commentText: List<CommentData.HotComment> = emptyList()
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNickname: TextView = itemView.findViewById(R.id.comment_tv)
        val tvContent: TextView = itemView.findViewById(R.id.comment_tv1)
        val ivAvatar: ImageView = itemView.findViewById(R.id.comment_img)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val item = commentText[position]
        holder.tvNickname.text = item.user?.nickname ?: "匿名用户"
        holder.tvContent.text = item.content ?: "暂无内容"

        Glide.with(holder.itemView.context)
            .load(item.user?.avatarUrl)
            .apply(
                RequestOptions()
                    .error(R.drawable.ic_launcher_background)
                    .transform(CircleCrop())
            )
            .into(holder.ivAvatar)
    }

    override fun getItemCount() = commentText.size

    // 关键修复：更新内部数据源并刷新
    fun updateComments(newComments: List<CommentData.HotComment>) {
        commentText = newComments  // 替换内部数据源
        notifyDataSetChanged()    // 通知列表刷新
    }
}
