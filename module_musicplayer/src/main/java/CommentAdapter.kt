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
import com.example.module_musicplayer.databinding.ItemCommentBinding
import data.CommentData

class CommentAdapter(
    private var commentText: List<CommentData.HotComment> = emptyList()
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(private val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        val tvNickname: TextView = binding.commentTv
        val tvContent: TextView = binding.commentTv1
        val ivAvatar: ImageView = binding.commentImg
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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

    fun updateComments(newComments: List<CommentData.HotComment>) {
        commentText = newComments  // 替换内部数据源
        notifyDataSetChanged()    // 通知列表刷新
    }
}
