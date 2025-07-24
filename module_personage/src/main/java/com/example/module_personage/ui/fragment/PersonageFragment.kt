package com.example.module_personage.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.module_login_register.R
import com.example.module_login_register.databinding.FragmentPersonageBinding
import com.example.module_personage.adapter.ViewPagerAdapter
import com.example.module_personage.ui.activity.DownloadActivity
import com.example.module_personage.ui.activity.LikedActivity
import com.example.module_personage.ui.activity.LikeActivity
import com.example.module_personage.viewModel.PersonageViewModel

class PersonageFragment : Fragment() {
    // 视图绑定
    private var _binding: FragmentPersonageBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private lateinit var viewModel: PersonageViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // 初始化绑定
        _binding = FragmentPersonageBinding.inflate(inflater, container, false)
        // 初始化 ViewModel
        viewModel = ViewModelProvider(this)[PersonageViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 初始化 ViewPager2 和底部导航
        initViewPager()
        // 初始化按钮点击事件
        initClick()
    }

    // 初始化 ViewPager2 与底部导航联动
    private fun initViewPager() {
        // 初始化 ViewPager2 的适配器（传入当前 Fragment 作为父容器）
        val adapter = ViewPagerAdapter(this)
        binding.personageViewPager.adapter = adapter

        // 底部导航点击切换 ViewPager2 页面
        binding.personageBottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_item_song_list -> binding.personageViewPager.currentItem = 0
                R.id.menu_item_history -> binding.personageViewPager.currentItem = 1
            }
            true
        }

        // ViewPager2 滑动时同步更新底部导航选中状态
        binding.personageViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // 根据页面位置切换底部导航选中项
                val selectedItemId = when (position) {
                    0 -> R.id.menu_item_song_list
                    1 -> R.id.menu_item_history
                    else -> R.id.menu_item_song_list
                }
                binding.personageBottomNavigationView.selectedItemId = selectedItemId
            }
        })
    }

    // 初始化按钮点击事件
    private fun initClick() {
        binding.personageImageView.setOnClickListener {
            //跳转
        }

        binding.likeButton.setOnClickListener {
            context?.let { it ->
                val intent = Intent(it, LikeActivity::class.java)
                startActivity(intent)
            }
        }

        binding.likedButton.setOnClickListener {
            context?.let { it ->
                val intent = Intent(it, LikedActivity::class.java)
                startActivity(intent)
            }
        }

        binding.downloadButton.setOnClickListener {
            context?.let { it ->
                val intent = Intent(it, DownloadActivity::class.java)
                startActivity(intent)
            }
        }
    }

    // 销毁时释放绑定（避免内存泄漏）
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 创建 Fragment 实例便于外部调用
    companion object {
        fun newInstance(): PersonageFragment {
            return PersonageFragment()
        }
    }
}