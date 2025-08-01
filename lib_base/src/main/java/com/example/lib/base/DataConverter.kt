import data.ListMusicData
import data.Song1
import data.SongsResultData
import data.TopData
import com.example.lib.base.Song as BaseSong

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

    fun convertTopSongToMusicData(song: TopData.Song): ListMusicData.Song {
        val convertedArtists = song.ar.map { topArtist ->
            ListMusicData.Song.Ar(
                alias = topArtist.alias ?: emptyList(),
                id = topArtist.id.toLong(),
                name = topArtist.name ?: "未知歌手",
                tns = topArtist.tns ?: emptyList()
            )
        }

        val convertedAlbum = ListMusicData.Song.Al(
            id = song.al.id ?: 0L,
            name = song.al.name ?: "未知专辑",
            pic = song.al.pic ?: 0,
            picStr = song.al.picStr ?: "",
            picUrl = song.al.picUrl ?: "",
            tns = song.al.tns ?: emptyList()
        )

        return ListMusicData.Song(
            a = null,
            additionalTitle = null,
            al = convertedAlbum,
            alia = song.alia ?: emptyList(),
            ar = convertedArtists,
            awardTags = null,
            cd = song.cd ?: "1",
            cf = song.cf ?: "",
            copyright = song.copyright ?: 1,
            cp = song.cp ?: 0,
            crbt = null,
            displayTags = null,
            djId = song.djId ?: 0,
            dt = song.dt ?: 0,
            entertainmentTags = null,
            fee = song.fee ?: 0,
            ftype = song.ftype ?: 0,
            h = ListMusicData.Song.H(0, 0, 0, 0, 0), // 若 TopData.Song 有音质信息可映射
            hr = null,
            id = song.id ?: 0L,
            l = ListMusicData.Song.L(0, 0, 0, 0, 0), // 若 TopData.Song 有音质信息可映射
            m = ListMusicData.Song.M(0, 0, 0, 0, 0), // 若 TopData.Song 有音质信息可映射
            mainTitle = null,
            mark = song.mark ?: 0,
            mst = song.mst ?: 0,
            mv = song.mv ?: 0,
            name = song.name ?: "未知歌曲",
            no = song.no ?: 0,
            noCopyrightRcmd = null,
            originCoverType = song.originCoverType ?: 0,
            originSongSimpleData = null,
            pop = song.pop ?: 0,
            pst = song.pst ?: 0,
            publishTime = song.publishTime ?: 0,
            resourceState = song.resourceState ?: true,
            rt = song.rt ?: "",
            rtUrl = null,
            rtUrls = emptyList(),
            rtype = song.rtype ?: 0,
            rurl = null,
            sId = song.sId ?: 0,
            single = song.single ?: 0,
            songJumpInfo = null,
            sq = ListMusicData.Song.Sq(0, 0, 0, 0, 0), // 若 TopData.Song 有音质信息可映射
            st = song.st ?: 0,
            t = song.t ?: 0,
            tagPicList = null,

            v = song.v ?: 0,
            version = song.version ?: 0,
            tns = emptyList()
        )
    }

    fun convertTopSongList(songs: List<TopData.Song>): List<ListMusicData.Song> {
        return songs.map { convertTopSongToMusicData(it) }
    }
    fun convertSong1ToMusicDataSong(song1: Song1): ListMusicData.Song {
        // 转换歌手列表
        val convertedArtists = song1.ar.map { artist1 ->
            ListMusicData.Song.Ar(
                alias = emptyList(), // Song1中没有alias，用空列表
                id = artist1.id.toLong(), // 转换为Long类型
                name = artist1.name, // 直接复用歌手名称
                tns = emptyList() // Song1中没有tns，用空列表
            )
        }

        // 转换专辑信息
        val convertedAlbum = ListMusicData.Song.Al(
            id = song1.al.picUrl.hashCode().toLong(), // 用封面URL的hash作为专辑ID（或根据实际业务调整）
            name = "未知专辑", // Song1中没有专辑名称，用默认值
            pic = 0, // 无对应字段，用0
            picStr = song1.al.picUrl, // 专辑封面URL
            picUrl = song1.al.picUrl, // 专辑封面URL
            tns = emptyList() // 无对应字段，用空列表
        )

        // 构建目标Song对象
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
            dt = 0, // Song1中没有时长，用0
            entertainmentTags = null,
            fee = 0,
            ftype = 0,
            h = ListMusicData.Song.H(0, 0, 0, 0, 0),
            hr = null,
            id = song1.id, // 复用歌曲ID
            l = ListMusicData.Song.L(0, 0, 0, 0, 0),
            m = ListMusicData.Song.M(0, 0, 0, 0, 0),
            mainTitle = null,
            mark = 0,
            mst = 0,
            mv = 0, // Song1中没有MV信息，用0
            name = song1.name, // 复用歌曲名称
            no = 0,
            noCopyrightRcmd = null,
            originCoverType = 0,
            originSongSimpleData = null,
            pop = 0,
            pst = 0,
            publishTime = 0, // Song1中没有发布时间，用0
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

    fun convertSong1List(song1List: List<Song1>): List<ListMusicData.Song> {
        return song1List.map { convertSong1ToMusicDataSong(it) }
    }

    fun convertSongsResultData(resultData: SongsResultData): List<ListMusicData.Song> {
        // 先判断响应是否成功
        if (resultData.code != 200) {
            return emptyList()
        }
        // 转换歌曲列表
        return convertSong1List(resultData.result.songs)
    }
}