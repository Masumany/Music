package com.example.module_hot.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.module_hot.R
import com.example.module_hot.databinding.ActivityHotBinding
import com.example.module_hot.ui.fragment.HotFragment

class HotActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHotBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, HotFragment())
        transaction.commit()
    }
}