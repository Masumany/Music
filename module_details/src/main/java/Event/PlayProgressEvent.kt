package Event

data class PlayProgressEvent(
    val songId: String,
    val position: Int,  // 当前播放进度（毫秒）
    val duration: Int   // 歌曲总时长（毫秒）
)
