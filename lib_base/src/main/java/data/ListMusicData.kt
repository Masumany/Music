package data


import com.google.gson.annotations.SerializedName

data class ListMusicData(
    @SerializedName("code")
    val code: Int, // 200
    @SerializedName("privileges")
    val privileges: List<Privilege>,
    @SerializedName("songs")
    val songs: List<Song>
) {
    data class Privilege(
        @SerializedName("chargeInfoList")
        val chargeInfoList: List<ChargeInfo>,
        @SerializedName("code")
        val code: Int, // 0
        @SerializedName("cp")
        val cp: Int, // 1
        @SerializedName("cs")
        val cs: Boolean, // false
        @SerializedName("dl")
        val dl: Int, // 0
        @SerializedName("dlLevel")
        val dlLevel: String, // none
        @SerializedName("dlLevels")
        val dlLevels: Any?, // null
        @SerializedName("downloadMaxBrLevel")
        val downloadMaxBrLevel: String, // sky
        @SerializedName("downloadMaxbr")
        val downloadMaxbr: Int, // 999000
        @SerializedName("fee")
        val fee: Int, // 8
        @SerializedName("fl")
        val fl: Int, // 320000
        @SerializedName("flLevel")
        val flLevel: String, // exhigh
        @SerializedName("flag")
        val flag: Int, // 2064644
        @SerializedName("freeTrialPrivilege")
        val freeTrialPrivilege: FreeTrialPrivilege,
        @SerializedName("id")
        val id: Long, // 2604213594
        @SerializedName("ignoreCache")
        val ignoreCache: Any?, // null
        @SerializedName("maxBrLevel")
        val maxBrLevel: String, // sky
        @SerializedName("maxbr")
        val maxbr: Int, // 999000
        @SerializedName("message")
        val message: Any?, // null
        @SerializedName("payed")
        val payed: Int, // 0
        @SerializedName("pl")
        val pl: Int, // 320000
        @SerializedName("plLevel")
        val plLevel: String, // exhigh
        @SerializedName("plLevels")
        val plLevels: Any?, // null
        @SerializedName("playMaxBrLevel")
        val playMaxBrLevel: String, // sky
        @SerializedName("playMaxbr")
        val playMaxbr: Int, // 999000
        @SerializedName("preSell")
        val preSell: Boolean, // false
        @SerializedName("rightSource")
        val rightSource: Int, // 0
        @SerializedName("rscl")
        val rscl: Any?, // null
        @SerializedName("sp")
        val sp: Int, // 7
        @SerializedName("st")
        val st: Int, // 0
        @SerializedName("subp")
        val subp: Int, // 1
        @SerializedName("toast")
        val toast: Boolean // false
    ) {
        data class ChargeInfo(
            @SerializedName("chargeMessage")
            val chargeMessage: Any?, // null
            @SerializedName("chargeType")
            val chargeType: Int, // 0
            @SerializedName("chargeUrl")
            val chargeUrl: Any?, // null
            @SerializedName("rate")
            val rate: Int // 128000
        )

        data class FreeTrialPrivilege(
            @SerializedName("cannotListenReason")
            val cannotListenReason: Int, // 1
            @SerializedName("freeLimitTagType")
            val freeLimitTagType: Any?, // null
            @SerializedName("listenType")
            val listenType: Int, // 0
            @SerializedName("playReason")
            val playReason: Any?, // null
            @SerializedName("resConsumable")
            val resConsumable: Boolean, // false
            @SerializedName("userConsumable")
            val userConsumable: Boolean // false
        )
    }

    data class Song(
        @SerializedName("a")
        val a: Any?, // null
        @SerializedName("additionalTitle")
        val additionalTitle: String?, // (Steel Ball Run Unofficial Theme) (Original)
        @SerializedName("al")
        val al: Al,
        @SerializedName("alia")
        val alia: List<String>,
        @SerializedName("ar")
        val ar: List<Ar>,
        @SerializedName("awardTags")
        val awardTags: Any?, // null
        @SerializedName("cd")
        val cd: String, // 1
        @SerializedName("cf")
        val cf: String,
        @SerializedName("copyright")
        val copyright: Int, // 1
        @SerializedName("cp")
        val cp: Int, // 7003
        @SerializedName("crbt")
        val crbt: Any?, // null
        @SerializedName("displayTags")
        val displayTags: Any?, // null
        @SerializedName("djId")
        val djId: Int, // 0
        @SerializedName("dt")
        val dt: Int, // 213000
        @SerializedName("entertainmentTags")
        val entertainmentTags: Any?, // null
        @SerializedName("fee")
        val fee: Int, // 8
        @SerializedName("ftype")
        val ftype: Int, // 0
        @SerializedName("h")
        val h: H,
        @SerializedName("hr")
        val hr: Hr?,
        @SerializedName("id")
        val id: Long, // 2604213594
        @SerializedName("l")
        val l: L,
        @SerializedName("m")
        val m: M,
        @SerializedName("mainTitle")
        val mainTitle: String?, // Johnny Joestar Theme 
        @SerializedName("mark")
        val mark: Long, // 17180139520
        @SerializedName("mst")
        val mst: Int, // 9
        @SerializedName("mv")
        val mv: Int, // 14231198
        @SerializedName("name")
        val name: String, // Crucified
        @SerializedName("no")
        val no: Int, // 5
        @SerializedName("noCopyrightRcmd")
        val noCopyrightRcmd: Any?, // null
        @SerializedName("originCoverType")
        val originCoverType: Int, // 1
        @SerializedName("originSongSimpleData")
        val originSongSimpleData: Any?, // null
        @SerializedName("pop")
        val pop: Int, // 100
        @SerializedName("pst")
        val pst: Int, // 0
        @SerializedName("publishTime")
        val publishTime: Long, // 1364313600007
        @SerializedName("resourceState")
        val resourceState: Boolean, // true
        @SerializedName("rt")
        val rt: String, // 600902000006129840
        @SerializedName("rtUrl")
        val rtUrl: Any?, // null
        @SerializedName("rtUrls")
        val rtUrls: List<Any?>,
        @SerializedName("rtype")
        val rtype: Int, // 0
        @SerializedName("rurl")
        val rurl: Any?, // null
        @SerializedName("s_id")
        val sId: Int, // 0
        @SerializedName("single")
        val single: Int, // 0
        @SerializedName("songJumpInfo")
        val songJumpInfo: Any?, // null
        @SerializedName("sq")
        val sq: Sq,
        @SerializedName("st")
        val st: Int, // 0
        @SerializedName("t")
        val t: Int, // 0
        @SerializedName("tagPicList")
        val tagPicList: Any?, // null
        @SerializedName("tns")
        val tns: List<String>?,
        @SerializedName("v")
        val v: Int, // 49
        @SerializedName("version")
        val version: Int // 15
    ) {
        data class Al(
            @SerializedName("id")
            val id: Int, // 2430770
            @SerializedName("name")
            val name: String, // Big Battle of Egos
            @SerializedName("pic")
            val pic: Long, // 6625657070008115
            @SerializedName("pic_str")
            val picStr: String?, // 109951165090247904
            @SerializedName("picUrl")
            val picUrl: String, // https://p1.music.126.net/z8nX5tL0SCV9ohvAY9nY5A==/6625657070008115.jpg
            @SerializedName("tns")
            val tns: List<Any?>
        )

        data class Ar(
            @SerializedName("alias")
            val alias: List<Any?>,
            @SerializedName("id")
            val id: Int, // 85832
            @SerializedName("name")
            val name: String, // Army of Lovers
            @SerializedName("tns")
            val tns: List<Any?>
        )

        data class H(
            @SerializedName("br")
            val br: Int, // 320000
            @SerializedName("fid")
            val fid: Int, // 0
            @SerializedName("size")
            val size: Int, // 8531759
            @SerializedName("sr")
            val sr: Int, // 44100
            @SerializedName("vd")
            val vd: Int // -18500
        )

        data class Hr(
            @SerializedName("br")
            val br: Int, // 1799228
            @SerializedName("fid")
            val fid: Int, // 0
            @SerializedName("size")
            val size: Int, // 60360668
            @SerializedName("sr")
            val sr: Int, // 48000
            @SerializedName("vd")
            val vd: Int // -60532
        )

        data class L(
            @SerializedName("br")
            val br: Int, // 128000
            @SerializedName("fid")
            val fid: Int, // 0
            @SerializedName("size")
            val size: Int, // 3412803
            @SerializedName("sr")
            val sr: Int, // 44100
            @SerializedName("vd")
            val vd: Int // -15100
        )

        data class M(
            @SerializedName("br")
            val br: Int, // 192000
            @SerializedName("fid")
            val fid: Int, // 0
            @SerializedName("size")
            val size: Int, // 5119122
            @SerializedName("sr")
            val sr: Int, // 44100
            @SerializedName("vd")
            val vd: Int // -16100
        )

        data class Sq(
            @SerializedName("br")
            val br: Int, // 1086975
            @SerializedName("fid")
            val fid: Int, // 0
            @SerializedName("size")
            val size: Int, // 28971517
            @SerializedName("sr")
            val sr: Int, // 44100
            @SerializedName("vd")
            val vd: Int // -18500
        )
    }
}