package com.example.moudle_search.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.moudle_search.ui.fragment.SingersFragment
import com.example.moudle_search.ui.fragment.SongListsFragment
import com.example.moudle_search.ui.fragment.SongsFragment
import com.example.moudle_search.ui.fragment.VideosFragment

class SearchResultAdapter (
    activity: FragmentActivity
) : FragmentStateAdapter(activity) {
    private val fragments = listOf(
        SongsFragment.newInstance(),
        SingersFragment.newInstance(),
        SongListsFragment.newInstance(),
        VideosFragment.newInstance()
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}