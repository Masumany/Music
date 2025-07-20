package com.example.music

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.example.module_details.MusicDataCache
import com.example.module_musicplayer.MusicPlayService
import com.therouter.TheRouter
import data.ListMusicData

class BottomMusicController(
    private val context: Context,
    private val view: View,
    private val serviceConnection: ServiceConnection
) {
    private var musicPlayService: MusicPlayService? = null
    private var isServiceBound = false
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            handler.postDelayed(this, 1000)
        }
    }

    // LiveData观察者
    private val songObserver = Observer<ListMusicData.Song?> { song ->
        (context as? AppCompatActivity)?.runOnUiThread {
            updateSongInfo(song)
        }
    }
    private val playStateObserver = Observer<Boolean> { isPlaying ->
        (context as? AppCompatActivity)?.runOnUiThread {
            updatePlayState(isPlaying)
        }
    }

    init {
        initService()
        initClickEvents()
    }

    private fun initService() {
        val intent = Intent(context, MusicPlayService::class.java)
        context.startService(intent)
        context.applicationContext.bindService(
            intent,
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    private fun initClickEvents() {
        // 播放/暂停按钮点击事件
        val playButton = view.findViewById<ImageView>(R.id.stop)
        playButton.setOnClickListener {
            val service = musicPlayService ?: return@setOnClickListener
            if (service.isPlaying) {
                service.pause()
                playButton.setImageResource(com.example.module_recommened.R.drawable.list_start)
            } else {
                val currentUrl = service.currentUrl
                if (currentUrl.isNullOrBlank()) {
                    val firstSong = MusicDataCache.currentSongList?.firstOrNull()
                    if (firstSong != null) {
                        service.play(firstSong.al.picUrl, firstSong)
                    }
                    return@setOnClickListener
                }

                if (service.getCurrentPosition() > 0) {
                    service.resume()
                } else {
                    service.play(currentUrl, service.currentSong)
                }
                playButton.setImageResource(R.drawable.now_play)
            }
        }

        // 底部栏封面点击事件（核心修改：传递完整进度参数）
        view.findViewById<ImageView>(R.id.music).setOnClickListener {
            val service = musicPlayService ?: return@setOnClickListener
            val currentSong = service.currentSong ?: return@setOnClickListener
            val currentSongList = MusicDataCache.currentSongList ?: emptyList()

            // 1. 获取当前歌曲在列表中的索引
            val currentIndex = currentSongList.indexOfFirst { it.id == currentSong.id }
            val safeIndex = if (currentIndex in currentSongList.indices) currentIndex else 0

            // 2. 获取当前播放进度（毫秒）
            val playProgress = service.getCurrentPosition()

            // 3. 获取服务中的核心状态参数
            val currentPlayUrl = service.currentUrl ?: ""
            val isPlayingOrigin = service.isPlaying

            // 4. 构建路由参数，匹配MusicPlayerActivity接收逻辑
            TheRouter.build("/module_musicplayer/musicplayer")
                // 基础信息
                .withString("id", currentSong.id.toString())
                .withString("cover", currentSong.al?.picUrl)
                .withString("songListName", currentSong.name)
                .withString("athour", currentSong.ar.firstOrNull()?.name ?: "未知歌手")
                // 列表索引
                .withInt("currentPosition", safeIndex)
                // 关键：传递播放进度
                .withInt("playProgress", playProgress)
                .navigation(context)
        }
    }

    fun onServiceConnected(service: MusicPlayService) {
        musicPlayService = service
        isServiceBound = true
        (context as? AppCompatActivity)?.runOnUiThread {
            updatePlayState(service.isPlaying)
            updateSongInfo(service.currentSong)
        }
        service.currentSongLiveData.observeForever(songObserver)
        service.isPlayingLiveData.observeForever(playStateObserver)
        handler.post(updateRunnable)
    }

    private fun updateSongInfo(song: ListMusicData.Song?) {
        if (song == null) return
        val tvSong = view.findViewById<TextView>(R.id.tv_song)
        val tvArtist = view.findViewById<TextView>(R.id.tv_artist)
        val ivCover = view.findViewById<ImageView>(R.id.music)

        tvSong.text = song.name
        tvArtist.text = song.ar.joinToString { it.name }
        Glide.with(context)
            .load(song.al.picUrl)
            .error(R.drawable.drawerimg)
            .into(ivCover)
    }

    private fun updatePlayState(isPlaying: Boolean) {
        val playButton = view.findViewById<ImageView>(R.id.stop)
        playButton.setImageResource(
            if (isPlaying) R.drawable.now_play
            else com.example.module_recommened.R.drawable.list_start
        )
    }

    fun onDestroy() {
        musicPlayService?.let {
            it.currentSongLiveData.removeObserver(songObserver)
            it.isPlayingLiveData.removeObserver(playStateObserver)
        }
        handler.removeCallbacksAndMessages(null)
        if (isServiceBound) {
            try {
                context.applicationContext.unbindService(serviceConnection)
            } catch (e: IllegalArgumentException) {
                // 忽略异常
            }
            isServiceBound = false
        }
        musicPlayService = null
    }
}