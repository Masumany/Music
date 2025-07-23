package com.example.music.event

import data.ListMusicData

// 播放状态变更事件
data class PlayStateChangedEvent(val isPlaying: Boolean)

// 歌曲切换事件
data class SongChangedEvent(val song: ListMusicData.Song)

// 进度变更事件（根据实际需求调整参数）
data class ProgressChangedEvent(val progress: Int, val duration: Int)

data class PlayStateEvent(
    val isPlaying: Boolean,
    val position: Int,
    val duration: Int
)
class CloseLyricEvent