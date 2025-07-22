package com.example.module_mvplayer.viewModel.player

enum class PlayBackState {
    IDLE,
    //空闲状态
    BUFFERING,
    //缓冲状态
    READY,
    //就绪状态
    ENDED,
    //结束状态
    ERROR
    //错误状态
}