package com.example.music

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
//import com.example.module_recommened.RecommendFragment

class MainActivity : AppCompatActivity() {

    private lateinit var TopButton:ImageView
    private lateinit var DrawerLayout:DrawerLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        try {
            val fragmentTransactionTooLargeException=supportFragmentManager.beginTransaction()
//            fragmentTransactionTooLargeException.replace(R.id.mainContent,RecommendFragment())
//            fragmentTransactionTooLargeException.commit()
        }catch (e: Exception){
            Log.e("MainActivity", "Error loading fragment: ${e.message}", e)
        }


        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        DrawerLayout = findViewById(R.id.mainDrawerLayout)
        TopButton = findViewById(R.id.drawerButton)
        TopButton.setOnClickListener{
            DrawerLayout.openDrawer(GravityCompat.START)
        }

    }
    override fun onBackPressed() {
        if (DrawerLayout.isDrawerOpen(GravityCompat.START)) {
            DrawerLayout.closeDrawer(GravityCompat.START)
        }else {
            super.onBackPressed()
            finish()
        }
    }
}