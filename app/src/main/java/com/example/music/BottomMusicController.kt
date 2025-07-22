package com.example.music

import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import Adapter.MusicDataCache
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

    // 列表加载完成的回调接口
    interface OnSongListLoadListener {
        fun onSongListLoaded(songList: List<ListMusicData.Song>)
    }

    // 保存加载好的列表（核心数据）
    private var loadedSongList: List<ListMusicData.Song> = emptyList()

    init {
        initService()
        initClickEvents()
    }

    // 供外部（MainActivity）调用，传递加载好的列表
    fun setLoadedSongList(songList: List<ListMusicData.Song>) {
        loadedSongList = songList
        // 列表加载完成后自动播放第一首（仅当无当前播放歌曲时）
        if (songList.isNotEmpty() && musicPlayService?.currentUrl.isNullOrBlank()) {
            loadFirstSong(songList.first())
        }
    }

    // 加载并播放第一首歌
    private fun loadFirstSong(firstSong: ListMusicData.Song) {
        if (isServiceBound) {
            musicPlayService?.play(firstSong.al.picUrl, firstSong)
            updateSongInfo(firstSong)
            updatePlayState(true)
        }
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
        // 播放/暂停按钮点击事件（使用loadedSongList）
        val playButton = view.findViewById<ImageView>(R.id.stop)
        playButton.setOnClickListener {
            val service = musicPlayService ?: return@setOnClickListener
            if (service.isPlaying) {
                service.pause()
                playButton.setImageResource(com.example.module_recommened.R.drawable.list_start)
            } else {
                if (loadedSongList.isEmpty()) {
                    Toast.makeText(context, "列表加载中，请稍后", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                // 从已加载列表中取歌曲播放
                val currentUrl = service.currentUrl
                if (currentUrl.isNullOrBlank() || service.getCurrentPosition() == 0) {
                    // 无播放记录时，直接播放列表第一首
                    val firstSong = loadedSongList.first()
                    service.play(firstSong.al.picUrl, firstSong)
                } else {
                    // 有播放记录时恢复播放
                    service.resume()
                }
                playButton.setImageResource(R.drawable.now_play)
            }
        }

        // 底部栏封面点击事件（使用loadedSongList）
        view.findViewById<ImageView>(R.id.music).setOnClickListener {
            if (loadedSongList.isEmpty()) {
                Toast.makeText(context, "列表加载中，请稍后", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val service = musicPlayService ?: return@setOnClickListener
            // 从已加载列表中获取当前歌曲
            val currentSong = service.currentSong ?: loadedSongList.first()
            // 获取当前歌曲在列表中的索引
            val currentIndex = loadedSongList.indexOfFirst { it.id == currentSong.id }
            val safeIndex = if (currentIndex in loadedSongList.indices) currentIndex else 0
            // 获取播放进度
            val playProgress = service.getCurrentPosition()

            // 跳转详情页
            TheRouter.build("/module_musicplayer/musicplayer")
                .withString("id", currentSong.id.toString())
                .withString("cover", currentSong.al?.picUrl)
                .withString("songListName", currentSong.name)
                .withString("athour", currentSong.ar.firstOrNull()?.name ?: "未知歌手")
                .withInt("currentPosition", safeIndex)
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
}