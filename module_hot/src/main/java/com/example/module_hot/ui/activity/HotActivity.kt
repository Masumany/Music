package com.example.module_hot.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.module_hot.R
import com.example.module_hot.adapter.HotAdapter
import com.example.module_hot.databinding.ActivityHotBinding

class HotActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHotBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHotBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.hotViewPager.adapter = HotAdapter(this)

        // 导航与ViewPager2联动
        binding.hotBottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_item_list -> binding.hotViewPager.currentItem = 0
                R.id.menu_item_singer -> binding.hotViewPager.currentItem = 1
                R.id.menu_item_mv -> binding.hotViewPager.currentItem = 2
            }
            true
        }

        // ViewPager2滑动时更新底部导航选中项
        binding.hotViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.hotBottomNavigationView.selectedItemId = when (position) {
                    0 -> R.id.menu_item_list
                    1 -> R.id.menu_item_singer
                    2 -> R.id.menu_item_mv
                    else -> R.id.menu_item_list
                }
            }
        })
    }
}