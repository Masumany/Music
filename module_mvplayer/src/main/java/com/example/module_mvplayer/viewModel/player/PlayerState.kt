package com.example.module_mvplayer.viewModel.player

import androidx.media3.common.C


data class PlayerState (
    //播放状态(空闲，缓冲，就绪)
    val playBackState: PlayBackState = PlayBackState.IDLE,
    //当前播放的位置
    val currentPosition: Long = 0,
    //总时长
    val duration: Long = C.TIME_UNSET,
    //播放模式(是否在播放)
    val isPlaying: Boolean = false,
    //播放列表
//    val playList: List<MvBean> = emptyList(),
    //错误信息
    val errorMessage: String ?= null
)