package data

import com.google.gson.annotations.SerializedName

/**
 * 歌曲播放链接接口的响应模型
 * 修复 peak 字段类型错误，支持小数解析
 */
data class MusicData(
    @SerializedName("code")
    val code: Int, // 200
    @SerializedName("data")
    val `data`: List<Data>
) {
    data class Data(
        @SerializedName("accompany")
        val accompany: Any?, // null
        @SerializedName("auEff")
        val auEff: Any?, // null
        @SerializedName("br")
        val br: Int, // 320000（比特率）
        @SerializedName("canExtend")
        val canExtend: Boolean, // false
        @SerializedName("channelLayout")
        val channelLayout: Any?, // null
        @SerializedName("closedGain")
        val closedGain: Float, // -6
        @SerializedName("closedPeak")
        val closedPeak: Double, // 1.0374（修正：保持Double，支持小数）
        @SerializedName("code")
        val code: Int, // 200
        @SerializedName("effectTypes")
        val effectTypes: Any?, // null
        @SerializedName("encodeType")
        val encodeType: String, // mp3
        @SerializedName("expi")
        val expi: Int, // 1200（有效期）
        @SerializedName("fee")
        val fee: Int, // 0（费用类型）
        @SerializedName("flag")
        val flag: Int, // 1343489
        @SerializedName("freeTimeTrialPrivilege")
        val freeTimeTrialPrivilege: FreeTimeTrialPrivilege,
        @SerializedName("freeTrialInfo")
        val freeTrialInfo: Any?, // null
        @SerializedName("freeTrialPrivilege")
        val freeTrialPrivilege: FreeTrialPrivilege,
        @SerializedName("gain")
        val gain: Double, // -6.3072（修正：音量增益，支持小数）
        @SerializedName("id")
        val id: Long, // 33894312（歌曲ID，用Long避免溢出）
        @SerializedName("level")
        val level: String, // exhigh（音质等级）
        @SerializedName("levelConfuse")
        val levelConfuse: Any?, // null
        @SerializedName("md5")
        val md5: String, // a8772889f38dfcb91c04da915b301617
        @SerializedName("message")
        val message: Any?, // null
        @SerializedName("musicId")
        val musicId: String, // 101045179
        @SerializedName("payed")
        val payed: Int, // 0
        @SerializedName("peak")
        val peak: Double, // 1（关键修正：从Int改为Double，支持小数如0.9231）
        @SerializedName("podcastCtrp")
        val podcastCtrp: Any?, // null
        @SerializedName("rightSource")
        val rightSource: Int, // 0
        @SerializedName("size")
        val size: Long, // 10691439（文件大小，用Long避免大文件溢出）
        @SerializedName("sr")
        val sr: Int, // 44100（采样率）
        @SerializedName("time")
        val time: Int, // 267232（时长，毫秒）
        @SerializedName("type")
        val type: String, // mp3
        @SerializedName("uf")
        val uf: Any?, // null
        @SerializedName("url")
        val url: String, // 播放链接
        @SerializedName("urlSource")
        val urlSource: Int // 0
    ) {
        data class FreeTimeTrialPrivilege(
            @SerializedName("remainTime")
            val remainTime: Int, // 0
            @SerializedName("resConsumable")
            val resConsumable: Boolean, // false
            @SerializedName("type")
            val type: Int, // 0
            @SerializedName("userConsumable")
            val userConsumable: Boolean // false
        )

        data class FreeTrialPrivilege(
            @SerializedName("cannotListenReason")
            val cannotListenReason: Any?, // null
            @SerializedName("freeLimitTagType")
            val freeLimitTagType: Any?, // null
            @SerializedName("listenType")
            val listenType: Any?, // null
            @SerializedName("playReason")
            val playReason: Any?, // null
            @SerializedName("resConsumable")
            val resConsumable: Boolean, // false
            @SerializedName("userConsumable")
            val userConsumable: Boolean // false
        )
    }
}