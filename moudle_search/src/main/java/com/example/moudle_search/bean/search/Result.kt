package com.example.moudle_search.bean.search

data class Result(
    val song: List<Song>?, // 歌曲列表
    val artist: List<Artist>?, // 歌手列表
    val playlist: List<Playlist>?, // 歌单列表
    val new_mlog: List<Video>? // 相关视频/短视频列表
)