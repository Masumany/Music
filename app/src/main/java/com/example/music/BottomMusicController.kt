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
import androidx.core.content.ContextCompat.startActivity
import com.bumptech.glide.Glide
import com.example.lib.base.Song
import com.example.module_musicplayer.MusicPlayService
import com.example.module_musicplayer.MusicPlayerActivity

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
            // 安全调用，避免空指针
            musicPlayService?.let { service ->
                if (service.isPlaying) {
                    // 可以在这里更新进度条（如果有）
                }
            }
            handler.postDelayed(this, 1000)
        }
    }

    init {
        // 启动服务后再绑定，确保服务已初始化
        val intent = Intent(context, MusicPlayService::class.java)
        context.startService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        initClickEvents()
    }

    private fun initClickEvents() {
        // 播放/暂停按钮点击事件
        val playButton = view.findViewById<ImageView>(R.id.stop)
        playButton.setOnClickListener {
            musicPlayService?.let { service ->
                if (service.isPlaying) {
                    service.pause()
                    playButton.setImageResource(com.example.module_recommened.R.drawable.list_start) // 确保资源存在于当前模块
                } else {
                    // 检查当前播放地址是否有效
                    val currentUrl = service.currentUrl
                    if (currentUrl.isNullOrBlank()) {
                        // 没有播放地址时的处理（如播放默认歌曲）
                        return@setOnClickListener
                    }

                    if (service.getCurrentPosition() > 0) {
                        service.resume()
                    } else {
                        service.play(
                            currentUrl,
                            song = service.currentSong
                        )
                    }
                    playButton.setImageResource(R.drawable.now_play)
                }
            } ?: run {
                // 服务未连接时的提示
                // Toast.makeText(context, "服务未准备好", Toast.LENGTH_SHORT).show()
            }
        }

        // 点击歌曲封面跳转到全屏播放器
        // 点击歌曲封面跳转到全屏播放器
        view.findViewById<ImageView>(R.id.music).setOnClickListener {
            // 1. 从服务中获取当前播放的歌曲和URL（关键：必须是实时的）
            val currentSong = musicPlayService?.currentSong
            val currentUrl = musicPlayService?.currentUrl
            val currentPosition = musicPlayService?.getCurrentPosition() ?: 0
            val isPlaying = musicPlayService?.isPlaying == true


            // 2. 若没有播放中的歌曲，直接返回（避免跳转后空白）
            if (currentSong == null || currentUrl.isNullOrBlank()) {
                return@setOnClickListener
            }

            // 3. 构建携带完整参数的Intent
            val intent = Intent(context, MusicPlayerActivity::class.java).apply {
                // 传递歌曲唯一ID（用于定位歌曲在列表中的位置）
                putExtra("target_song_id", currentSong.id.toString())
                // 传递当前播放URL（确保继续播放同一首）
                putExtra("current_play_url", currentUrl)
                // 传递歌曲元数据（用于直接显示，无需再次请求）
                putExtra("current_position", currentPosition) // 播放进度
                putExtra("song_name", currentSong.name)
                putExtra("song_cover", currentSong.al?.picUrl)
                putExtra("song_artist", currentSong.ar?.joinToString { it.name })
                putExtra("is_playing_origin", isPlaying)
            }

            // 4. 启动全屏播放器
            context.startActivity(intent)
        }
    }

    // 服务连接成功时调用
    fun onServiceConnected(service: MusicPlayService) {
        musicPlayService = service
        isServiceBound = true
        // 同步服务状态到UI

        (context as? AppCompatActivity)?.runOnUiThread {
            updatePlayState(service.isPlaying)
            updateSongInfo(service.currentSong)
        }
        updatePlayState(service.isPlaying)
        updateSongInfo(service.currentSong)

        service.currentSongLiveData.observeForever({ song ->
            (context as? AppCompatActivity)?.runOnUiThread {
                updateSongInfo(song)
            }        })
        service.isPlayingLiveData.observeForever({ isPlaying ->
            (context as? AppCompatActivity)?.runOnUiThread {
                updatePlayState(isPlaying)
            }
        })
        // 启动定时更新
        handler.post(updateRunnable)
    }

    // 更新歌曲信息到UI
    private fun updateSongInfo(song: Song?) {
        if (song == null) return

        val tvSong = view.findViewById<TextView>(R.id.tv_song)
        val tvArtist = view.findViewById<TextView>(R.id.tv_artist)
        val ivCover = view.findViewById<ImageView>(R.id.music)

        tvSong.text = song.name
        tvArtist.text = song.ar.joinToString { it.name }
        Glide.with(context)
            .load(song.al.picUrl)
            .error(R.drawable.drawerimg) // 添加默认封面避免加载失败
            .into(ivCover)
    }

    // 更新播放状态图标
    private fun updatePlayState(isPlaying: Boolean) {
        val playButton = view.findViewById<ImageView>(R.id.stop)
        R.drawable.now_play
        playButton.setImageResource(
            if (isPlaying) {
                R.drawable.now_play // 确保此资源在当前模块的drawable目录
            } else {
                com.example.module_recommened.R.drawable.list_start // 跨模块资源需完整路径
            }
        )
    }

    // 清理资源
    fun onDestroy() {
        handler.removeCallbacks(updateRunnable)
        if (isServiceBound) {
            try {
                context.unbindService(serviceConnection)
            } catch (e: Exception) {
                // 避免重复解绑导致的异常
            }
            isServiceBound = false
        }
        musicPlayService = null
    }
}
