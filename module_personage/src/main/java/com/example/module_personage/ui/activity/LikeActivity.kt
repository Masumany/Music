package com.example.module_personage.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.module_login_register.R
import com.example.module_login_register.databinding.ActivityLikeBinding
import com.example.module_personage.adapter.RvAdapter
import com.example.module_personage.viewModel.LikeViewModel

class LikeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLikeBinding
    private lateinit var viewModel: LikeViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLikeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[LikeViewModel::class.java]
        binding.rvLike.adapter = RvAdapter()
    }
}