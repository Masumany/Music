package com.example.music

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.music.databinding.ActivityHeaderBinding

class HeaderActivity : AppCompatActivity(){
    private lateinit var binding:ActivityHeaderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHeaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.weBack.setOnClickListener {
            finish()
        }

        val img=binding.gifImageView
        Glide.with( this)
            .asGif()
            .load(R.drawable.power)
            .into(img)
    }
}