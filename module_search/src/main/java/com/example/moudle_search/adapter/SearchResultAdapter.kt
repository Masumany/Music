package com.example.moudle_search.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.moudle_search.ui.fragment.SingersFragment
import com.example.moudle_search.ui.fragment.SongsFragment
import com.example.moudle_search.ui.fragment.VideosFragment

class SearchResultAdapter (
    activity: FragmentActivity,
    private var keywords: String
) : FragmentStateAdapter(activity) {

    private val fragments = listOf(
        SongsFragment(),
        SingersFragment(),
        VideosFragment
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SongsFragment.newInstance(keywords)
            1 -> SingersFragment.newInstance(keywords)
//            2 -> SongListsFragment.newInstance(keywords)
            2 -> VideosFragment.newInstance(keywords)
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
    fun updateKeyword(newKeywords: String) {
        this.keywords = newKeywords
        fragments.forEach { fragment ->
            if (fragment is Searchable) {
                fragment.onNewSearch(newKeywords)
            }
        }
    }
    interface Searchable {
        fun onNewSearch(keyword: String)
    }
}