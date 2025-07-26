package com.example.music.viewmodel

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.example.module_musicplayer.MusicPlayService

class MusicServiceConnection(private val viewModel: BottomViewModel) : ServiceConnection {
    override fun onServiceConnected(className: ComponentName, service: IBinder) {
        val binder = service as MusicPlayService.MusicBinder
        viewModel.onServiceConnected(binder.service)
    }//连接成功时调用

    override fun onServiceDisconnected(arg0: ComponentName) {
    }//服务意外断开连接时调用
}
