package Event

data class SongChangeEvent (
    val newSongId: String,       // 新歌曲ID
    val initialPosition: Int = 0
 )
