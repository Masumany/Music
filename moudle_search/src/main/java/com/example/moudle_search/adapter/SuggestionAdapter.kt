package com.example.module_search.adapter

import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.moudle_search.bean.searchSuggestionData.AllMatch
import com.example.moudle_search.databinding.SuggestionItemBinding

class SuggestionAdapter(
    private val onItemClick: (AllMatch) -> Unit
) : androidx.recyclerview.widget.ListAdapter<AllMatch, SuggestionAdapter.SuggestionViewHolder>(
    object : DiffUtil.ItemCallback<AllMatch>() {
        override fun areItemsTheSame(oldItem: AllMatch, newItem: AllMatch): Boolean {
            return oldItem.keyword == newItem.keyword && oldItem.feature == newItem.feature
        }

        override fun areContentsTheSame(oldItem: AllMatch, newItem: AllMatch): Boolean {
            return oldItem == newItem
        }
    }
) {
    inner class SuggestionViewHolder(private val binding: SuggestionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(match: AllMatch) {
            val keywords = match.keyword
            val suggestionContent = highlightMatches(keywords, listOf(match))
            // 将高亮后的内容显示到TextView（需要配合富文本解析）
            binding.tvSuggestionItem.text = Html.fromHtml(suggestionContent, Html.FROM_HTML_MODE_COMPACT)

            // 点击事件
            binding.root.setOnClickListener {
                onItemClick(match)
            }
        }

        private fun highlightMatches(content: String, matches: List<AllMatch>): String {
            return matches.fold(content) { result, match ->
                if (match.keyword.isEmpty()) {
                    result
                }
                else {
                    result.replace(match.keyword, "<font color='#FF5722'><b>${match.keyword}</b></font>",ignoreCase = true)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val binding = SuggestionItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SuggestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}