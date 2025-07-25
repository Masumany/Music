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
    var currentQuery: String = ""
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class SuggestionViewHolder(private val binding: SuggestionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(match: AllMatch) {
            val keywords = match.keyword
            val suggestionContent = highlightMatches(keywords, currentQuery)
            // 将高亮后的内容显示到TextView（需要配合富文本解析）
            binding.tvSuggestionItem.text = Html.fromHtml(suggestionContent, Html.FROM_HTML_MODE_COMPACT)

            // 点击事件
            binding.root.setOnClickListener {
                onItemClick(match)
            }
        }

        private fun highlightMatches(keyword: String, query: String): String {
            val startIndex = keyword.indexOf(query, ignoreCase = true)
            return if (startIndex >= 0) {
                val before = keyword.substring(0, startIndex)
                val match = keyword.substring(startIndex, startIndex + query.length)
                val after = keyword.substring(startIndex + query.length)
                "<font color='#999999'>$before</font><font color='#000000'>$match</font><font color='#999999'>$after</font>"
            } else {
                // 如果没匹配上，整行变灰
                "<font color='#999999'>$keyword</font>"
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