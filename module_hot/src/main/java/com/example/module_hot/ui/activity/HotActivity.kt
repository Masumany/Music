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
import com.example.module_hot.viewModel.HotViewModel

class HotActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHotBinding
    private val viewModel: HotViewModel by lazy {
        ViewModelProvider(this)[HotViewModel::class.java]
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHotBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.hotViewPager.adapter = HotAdapter(this)

        // 导航与ViewPager2联动
        binding.hotBottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_item_song_list -> binding.hotViewPager.currentItem = 0
                R.id.menu_item_history -> binding.hotViewPager.currentItem = 1
            }
            true
        }

        // ViewPager2滑动时更新底部导航选中项
        binding.hotViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.hotBottomNavigationView.selectedItemId = when (position) {
                    0 -> R.id.menu_item_song_list
                    1 -> R.id.menu_item_history
                    else -> R.id.menu_item_song_list
                }
            }
        })
    }
}