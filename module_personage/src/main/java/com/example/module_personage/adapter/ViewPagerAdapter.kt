package com.example.module_personage.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.module_personage.ui.fragment.HistoryFragment
import com.example.module_personage.ui.fragment.SongListFragment

class ViewPagerAdapter (
    activity: FragmentActivity
) : FragmentStateAdapter(activity) {
    private val fragments = listOf(
        SongListFragment.newInstance(),
        HistoryFragment.newInstance()
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}