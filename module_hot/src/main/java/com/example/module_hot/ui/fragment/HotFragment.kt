package com.example.module_hot.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.module_hot.R
import com.example.module_hot.adapter.HotAdapter
import com.example.module_hot.databinding.FragmentHotBinding

class HotFragment : Fragment() {
    private var _binding: FragmentHotBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.hotViewPager.adapter = HotAdapter(requireActivity())

        // 导航与ViewPager2联动
        binding.hotBottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id. menu_item_singer-> binding.hotViewPager.currentItem = 0
                R.id.menu_item_list -> binding.hotViewPager.currentItem = 1
                R.id.menu_item_mv -> binding.hotViewPager.currentItem = 2
            }
            true
        }

        // ViewPager2滑动时更新底部导航选中项
        binding.hotViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.hotBottomNavigationView.selectedItemId = when (position) {
                    0 -> R.id.menu_item_singer
                    1 -> R.id.menu_item_list
                    2 -> R.id.menu_item_mv
                    else -> R.id.menu_item_singer
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}