package com.example.module_recommened

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.module_recommened.databinding.ActivityRecommendBinding
import com.therouter.router.Route

@Route(path = "/module_recommened/recommend")
class RecommendActivity: AppCompatActivity() {

    private lateinit var binding:ActivityRecommendBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityRecommendBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val transaction=supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container,RecommendFragment())
        transaction.commit()
    }
}