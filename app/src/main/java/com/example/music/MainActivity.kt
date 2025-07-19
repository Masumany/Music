package com.example.music

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.module_musicplayer.MusicPlayService
import com.example.module_recommened.RecommendFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.therouter.router.Route
import org.greenrobot.eventbus.EventBus

@Route(path = "/main/main")
class MainActivity : AppCompatActivity() {

    private lateinit var TopButton:ImageView
    private lateinit var DrawerLayout:DrawerLayout
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var serviceConnection: ServiceConnection
    private lateinit var bottomMusicController: BottomMusicController
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        try {
            val fragmentTransactionTooLargeException=supportFragmentManager.beginTransaction()
            fragmentTransactionTooLargeException.replace(R.id.mainContent, RecommendFragment())
            fragmentTransactionTooLargeException.commit()
        }catch (e: Exception){
            Log.e("MainActivity", "Error loading fragment: ${e.message}", e)
        }

        val bottomBarView = findViewById<ConstraintLayout>(R.id.player)
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder=service as MusicPlayService.MusicBinder
                bottomMusicController.onServiceConnected(binder.service)
            }
            override fun onServiceDisconnected(name: ComponentName?) {}
        }
        bottomMusicController = BottomMusicController(this, bottomBarView, serviceConnection)
        startService(Intent(this, MusicPlayService::class.java))

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        DrawerLayout = findViewById(R.id.mainDrawerLayout)
        TopButton = findViewById(R.id.drawerButton)
        TopButton.setOnClickListener{
            DrawerLayout.openDrawer(GravityCompat.START)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bottomMusicController.onDestroy()
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