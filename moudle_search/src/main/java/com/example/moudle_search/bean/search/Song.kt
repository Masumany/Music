package com.example.moudle_search.bean.search

data class Song(
    val id: Long, // 歌曲ID
    val name: String, // 歌曲名称
    val artists: List<SimpleArtist>, // 歌手信息
    val album: SimpleAlbum, // 所属专辑
    val duration: Long, // 时长
    val popularity: Int // 流行度
)

data class SimpleArtist(
    val id: Long, // 歌手ID
    val name: String // 歌手名称
)

data class SimpleAlbum(
    val id: Long, // 专辑ID
    val name: String, // 专辑名称
    val picUrl: String? // 专辑封面
)