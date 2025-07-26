package com.example.music.viewmodel

import Adapter.MusicDataCache
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.module_musicplayer.MusicPlayService
import com.therouter.TheRouter
import data.ListMusicData

class BottomViewModel(application: Application) : AndroidViewModel(application) {
    private val _currentSong = MutableLiveData<ListMusicData.Song?>()
    private val _isPlaying = MutableLiveData<Boolean>(false)
    private val _currentPosition = MutableLiveData<Int>(0)

    // LiveData，供View观察
    val currentSong: LiveData<ListMusicData.Song?> = _currentSong
    val isPlaying: LiveData<Boolean> = _isPlaying
    val currentPosition: LiveData<Int> = _currentPosition

    private var musicPlayService: MusicPlayService? = null
    private var isServiceBound = false

    // 服务连接后的回调
    fun onServiceConnected(service: MusicPlayService) {
        musicPlayService = service
        isServiceBound = true

        // 同步服务状态
        _isPlaying.value = service.isPlaying
        _currentSong.value = service.currentSong

        // 注册服务中的LiveData观察者
        service.currentSongLiveData.observeForever(songObserver)
        service.isPlayingLiveData.observeForever(playStateObserver)
    }

    // 歌曲变化观察者
    private val songObserver = Observer<ListMusicData.Song?> { song ->
        _currentSong.value = song
    }

    // 播放状态变化观察者
    private val playStateObserver = Observer<Boolean> { isPlaying ->
        _isPlaying.value = isPlaying
    }

    // 播放/暂停切换
    fun togglePlayPause(): Boolean {
        val service = musicPlayService ?: return false

        // 检查是否有歌曲
        if (MusicDataCache.currentSongList.isNullOrEmpty()) {
            return false
        }

        if (service.isPlaying) {
            service.pause()
        } else {
            if (service.currentUrl.isNullOrBlank()) {
                val firstSong = MusicDataCache.currentSongList?.firstOrNull()
                firstSong?.let {
                    service.play(it.al.picUrl, it)
                } ?: return false
            } else {
                if (service.getCurrentPosition() > 0) {
                    service.resume()
                } else {
                    service.play(service.currentUrl!!, service.currentSong)
                }
            }
        }
        return true
    }

    // 获取当前播放进度
    fun getCurrentPlayProgress(): Int {
        return musicPlayService?.getCurrentPosition() ?: 0
    }

    // 封面点击事件
    fun handleCoverClick(context: Context): Boolean {
        val currentSongList = MusicDataCache.currentSongList ?: return false
        if (currentSongList.isEmpty()) return false

        val currentSong = _currentSong.value ?: return false

        // 获取当前歌曲索引
        val currentIndex = currentSongList.indexOfFirst { it.id == currentSong.id }
        val safeIndex = if (currentIndex in currentSongList.indices) currentIndex else 0

        // 构建路由跳转
        TheRouter.build("/module_musicplayer/musicplayer")
            .withString("id", currentSong.id.toString())
            .withString("cover", currentSong.al?.picUrl)
            .withString("songListName", currentSong.name)
            .withString("athour", currentSong.ar.firstOrNull()?.name ?: "未知歌手")
            .withInt("currentPosition", safeIndex)
            .withInt("playProgress", getCurrentPlayProgress())
            .navigation(context)

        return true
    }

    // 启动并绑定服务
    fun initService(context: Context) {
        val intent = Intent(context, MusicPlayService::class.java)
        context.startService(intent)
        context.applicationContext.bindService(
            intent,
            MusicServiceConnection(this),
            Context.BIND_AUTO_CREATE
        )
    }

    // 清理资源
    override fun onCleared() {
        super.onCleared()
        musicPlayService?.let {
            it.currentSongLiveData.removeObserver(songObserver)
            it.isPlayingLiveData.removeObserver(playStateObserver)
        }
        musicPlayService = null
    }
}
