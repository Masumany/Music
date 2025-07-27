package com.example.module_personage.ui.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.module_login_register.R
import com.example.module_login_register.databinding.FragmentPersonageBinding
import com.example.module_personage.adapter.ViewPagerAdapter
import com.example.module_personage.ui.activity.DownloadActivity
import com.example.module_personage.ui.activity.LikedActivity
import com.example.module_personage.ui.activity.LikeActivity
import com.example.module_personage.utility.AvatarStorageUtil
import com.example.module_personage.viewModel.PersonageViewModel
import com.therouter.TheRouter
import kotlinx.coroutines.launch
import java.io.File

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
        loadLocalAvatar()
        getUserDetail()
    }

    private fun getUserDetail() {
        viewModel.getUserDetail()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userDetailData.collect { userDetail ->
                    // 处理数据为null的情况
                    userDetail?.let { detail ->
                        // 显示用户名
                        binding.personageTextView.text = detail.profile.nickname ?: "未知用户"

                        // 显示默认头像
                        val avatarUrl = detail.profile.avatarUrl
                        if (!avatarUrl.isNullOrEmpty()) {
                            // 只有当本地没有自定义头像时，才显示默认头像
                            val localAvatarUri = context?.let { AvatarStorageUtil.getAvatarUri(it) }
                            if (localAvatarUri == null) {
                                Glide.with(this@PersonageFragment)
                                    .load(avatarUrl)
                                    .circleCrop() // 圆形裁剪（头像常用样式）
                                    .placeholder(R.drawable.loading) // 加载中占位图
                                    .error(R.drawable.error) // 加载失败默认图
                                    .into(binding.personageImageView)
                            }
                        }
                    } ?: run {
                        // 数据为null时的容错处理
                        binding.personageTextView.text = "加载失败"
                        binding.personageImageView.setImageResource(R.drawable.error)
                    }
                }
            }
        }
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

    private val REQUEST_CODE_AVATAR = 1001
    private val REQUEST_CODE_CAMERA = 1002
    private var tempImageUri: Uri? = null
    // 初始化按钮点击事件
    private fun initClick() {
        binding.personageImageView.setOnClickListener {
            showAvatarOptionsDialog()
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
    private fun showAvatarOptionsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("更换头像")
            .setItems(arrayOf("拍照", "从相册选择", "取消")) { dialog, which ->
                when (which) {
                    0 -> takePhoto()
                    1 -> pickFromGallery()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }
    // 拍照功能
    private fun takePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            // 创建临时文件存储拍照结果
            val tempFile = File.createTempFile(
                "avatar_temp",
                ".jpg",
                requireContext().externalCacheDir
            )
            tempImageUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                tempFile
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri)
            startActivityForResult(intent, REQUEST_CODE_CAMERA)
        }
    }
    // 从相册选择
    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE_AVATAR)
    }

    // 处理结果
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_AVATAR -> {
                if (resultCode == Activity.RESULT_OK && data?.data != null) {
                    updateAvatar(data.data!!)
                }
            }
            REQUEST_CODE_CAMERA -> {
                if (resultCode == Activity.RESULT_OK && tempImageUri != null) {
                    updateAvatar(tempImageUri!!)
                }
            }
        }
    }

    // 更新头像显示
    private fun updateAvatar(uri: Uri) {
        Glide.with(this)
            .load(uri)
            .circleCrop()
            .into(binding.personageImageView)

        //保存到ViewModel（用于当前会话）
        viewModel.saveAvatarUri(uri)

        //保存到本地（SharedPreferences，用于持久化）
        context?.let {
            AvatarStorageUtil.saveAvatarUri(it, uri)
        }
    }
    private fun loadLocalAvatar() {
        // 从本地存储获取头像URI
        val savedUri = context?.let { AvatarStorageUtil.getAvatarUri(it) }
        savedUri?.let { uri ->
            // 显示本地保存的头像
            Glide.with(this)
                .load(uri)
                .circleCrop()
                .into(binding.personageImageView)
            // 同步到ViewModel
            viewModel.saveAvatarUri(uri)
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