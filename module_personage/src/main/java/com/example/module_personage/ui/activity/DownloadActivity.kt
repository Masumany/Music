package com.example.module_personage.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.module_login_register.R
import com.example.module_login_register.databinding.ActivityDownloadBinding
import com.example.module_login_register.databinding.ActivityLikeBinding

class DownloadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDownloadBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initClick()
    }
    private fun initClick() {
        binding.button.setOnClickListener {
            finish()
        }
    }
}