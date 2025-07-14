// SongData.kt
package com.example.module_recommened.model

/**
 * 根响应类（对应整个JSON）
 */
data class SongRecommendResponse(
  val code: Int,
  val data: SongData?
)

/**
 * 数据容器类（对应JSON中的data字段）
 */
data class SongData(
  val fromCache: Boolean,
  val dailySongs: List<Song>?
)

/**
 * 歌曲类（对应dailySongs中的元素）
 */
data class Song(
  val name: String, // 歌曲名称
  val ar: List<Artist>, // 歌手列表
  val al: Album // 专辑信息
)

/**
 * 歌手类（对应ar中的元素）
 */
data class Artist(
  val id: Long,
  val name: String // 歌手名称
)

/**
 * 专辑类（对应al字段）
 */
data class Album(
  val id: Long,
  val name: String,
  val picUrl: String? // 专辑封面URL
)