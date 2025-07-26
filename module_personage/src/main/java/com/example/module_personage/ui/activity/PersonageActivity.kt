package com.example.module_personage.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.module_login_register.R
import com.example.module_login_register.databinding.ActivityPersonageBinding
import com.example.module_personage.ui.fragment.PersonageFragment

class PersonageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPersonageBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, PersonageFragment())
        transaction.commit()
    }
}