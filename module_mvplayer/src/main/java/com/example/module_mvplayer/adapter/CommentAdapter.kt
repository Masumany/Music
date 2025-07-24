package com.example.module_mvplayer.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.module_mvplayer.R
import com.example.module_mvplayer.bean.commentData.Comment
import com.example.module_mvplayer.databinding.MvCommentItemBinding

class CommentAdapter (
): androidx.recyclerview.widget.ListAdapter<Comment, CommentAdapter.CommentViewHolder>(
    object : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem.commentId == newItem.commentId
        }

        override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
            return oldItem == newItem
        }
    }
) {
    inner class CommentViewHolder(val binding: MvCommentItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            Glide.with(binding.mvCommentAvatar.context)
                .load(comment.user.avatarUrl)
                .circleCrop()
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .into(binding.mvCommentAvatar)

            binding.mvCommentNickname.text = comment.user.nickname
            binding.mvCommentContent.text = comment.content
            binding.mvCommentTime.text = comment.timeStr
            binding.mvCommentLocation.text = comment.ipLocation?.location
        }
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): CommentViewHolder {
        val binding = MvCommentItemBinding.inflate(android.view.LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = getItem(position)
        holder.bind(comment)
    }
}