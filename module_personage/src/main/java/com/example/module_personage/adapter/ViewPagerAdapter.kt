package com.example.module_personage.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.module_personage.ui.fragment.HistoryFragment
import com.example.module_personage.ui.fragment.SongListsFragment

class ViewPagerAdapter(
    private val parentFragment: Fragment // 接收 Fragment 作为父容器
) : FragmentStateAdapter(
    parentFragment.childFragmentManager, // 使用父 Fragment 的子 FragmentManager
    parentFragment.lifecycle // 使用父 Fragment 的生命周期
) {

    private val fragments = listOf(
        SongListsFragment.newInstance(),
        HistoryFragment.newInstance()
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}