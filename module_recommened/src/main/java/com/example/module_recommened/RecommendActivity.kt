package com.example.module_recommened

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class RecommendActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recommend)

        val transaction=supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container,RecommendFragment())
        transaction.commit()
    }
}