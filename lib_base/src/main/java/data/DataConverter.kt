// 文件名: DataConverter.kt
// 包路径: 可以是 com.example.yourproject.converter (根据你的项目结构调整)

package com.example.yourproject.converter

import com.example.lib.base.Song as BaseSong
import com.example.lib.base.Artist as BaseArtist
import com.example.lib.base.Album as BaseAlbum
import data.ListMusicData

object DataConverter {
    // 将单个BaseSong转换为ListMusicData.Song
    fun convertBaseSongToMusicDataSong(baseSong: BaseSong): ListMusicData.Song {
        // 转换歌手列表
        val convertedArtists = baseSong.ar.map { baseArtist ->
            ListMusicData.Song.Ar(
                alias = emptyList(),
                id = baseArtist.id.toLong(),
                name = baseArtist.name,
                tns = emptyList()
            )
        }

        // 转换专辑信息
        val convertedAlbum = ListMusicData.Song.Al(
            id = baseSong.al.id.toLong(),
            name = baseSong.al.name,
            pic = baseSong.al.id,
            picStr = baseSong.al.picUrl,
            picUrl = baseSong.al.picUrl ?: "",
            tns = emptyList()
        )

        // 返回转换后的 Song 对象
        return ListMusicData.Song(
            a = null,
            additionalTitle = null,
            al = convertedAlbum,
            alia = emptyList(),
            ar = convertedArtists,
            awardTags = null,
            cd = "1",
            cf = "",
            copyright = 1,
            cp = 0,
            crbt = null,
            displayTags = null,
            djId = 0,
            dt = 0,
            entertainmentTags = null,
            fee = 0,
            ftype = 0,
            h = ListMusicData.Song.H(0, 0, 0, 0, 0),
            hr = null,
            id = baseSong.id,
            l = ListMusicData.Song.L(0, 0, 0, 0, 0),
            m = ListMusicData.Song.M(0, 0, 0, 0, 0),
            mainTitle = null,
            mark = 0,
            mst = 0,
            mv = 0,
            name = baseSong.name,
            no = 0,
            noCopyrightRcmd = null,
            originCoverType = 0,
            originSongSimpleData = null,
            pop = 0,
            pst = 0,
            publishTime = 0,
            resourceState = true,
            rt = "",
            rtUrl = null,
            rtUrls = emptyList(),
            rtype = 0,
            rurl = null,
            sId = 0,
            single = 0,
            songJumpInfo = null,
            sq = ListMusicData.Song.Sq(0, 0, 0, 0, 0),
            st = 0,
            t = 0,
            tagPicList = null,
            tns = null,
            v = 0,
            version = 0
        )
    }

    // 批量转换列表
    fun convertBaseSongList(baseSongs: List<BaseSong>): List<ListMusicData.Song> {
        return baseSongs.map { convertBaseSongToMusicDataSong(it) }
    }
}