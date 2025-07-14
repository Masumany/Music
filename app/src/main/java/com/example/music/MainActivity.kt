package com.example.music

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var TopButton:ImageView
    private lateinit var DrawerLayout:DrawerLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
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
