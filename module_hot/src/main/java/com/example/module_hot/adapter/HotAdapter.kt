package com.example.module_hot.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.module_hot.ui.fragment.ListFragment
import com.example.module_hot.ui.fragment.MvRankFragment
import com.example.module_hot.ui.fragment.SingerFragment

class HotAdapter (
    activity: FragmentActivity
) : FragmentStateAdapter(activity) {
    private val fragments = listOf(
        SingerFragment.newInstance(),
        MvRankFragment.newInstance(),
        ListFragment.newInstance()
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}