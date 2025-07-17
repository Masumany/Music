package data


import com.google.gson.annotations.SerializedName

/**
{
  "code": 200,
  "data": [
    {
      "id": 33894312,
      "url": "http://m7.music.126.net/20250717142827/bf4e29de325b55cf8e23c64c5335dae0/ymusic/0fd6/4f65/43ed/a8772889f38dfcb91c04da915b301617.mp3?vuutv=7iCEow9REOnu2ngSBcQIGLWOgi2uaBMWvz3Z3MuIVnny1pppb1cDrc0Hfvgl6zjiczDZMpA7OF7IJj/4oX7DAKI624bcm1CdAITERdV4Xts=",
      "br": 320000,
      "size": 10691439,
      "md5": "a8772889f38dfcb91c04da915b301617",
      "code": 200,
      "expi": 1200,
      "type": "mp3",
      "gain": -6.3072,
      "peak": 1,
      "closedGain": -6,
      "closedPeak": 1.0374,
      "fee": 0,
      "uf": null,
      "payed": 0,
      "flag": 1343489,
      "canExtend": false,
      "freeTrialInfo": null,
      "level": "exhigh",
      "encodeType": "mp3",
      "channelLayout": null,
      "freeTrialPrivilege": {
        "resConsumable": false,
        "userConsumable": false,
        "listenType": null,
        "cannotListenReason": null,
        "playReason": null,
        "freeLimitTagType": null
      },
      "freeTimeTrialPrivilege": {
        "resConsumable": false,
        "userConsumable": false,
        "type": 0,
        "remainTime": 0
      },
      "urlSource": 0,
      "rightSource": 0,
      "podcastCtrp": null,
      "effectTypes": null,
      "time": 267232,
      "message": null,
      "levelConfuse": null,
      "musicId": "101045179",
      "accompany": null,
      "sr": 44100,
      "auEff": null
    }
  ]
}
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
        val br: Int, // 320000
        @SerializedName("canExtend")
        val canExtend: Boolean, // false
        @SerializedName("channelLayout")
        val channelLayout: Any?, // null
        @SerializedName("closedGain")
        val closedGain: Int, // -6
        @SerializedName("closedPeak")
        val closedPeak: Double, // 1.0374
        @SerializedName("code")
        val code: Int, // 200
        @SerializedName("effectTypes")
        val effectTypes: Any?, // null
        @SerializedName("encodeType")
        val encodeType: String, // mp3
        @SerializedName("expi")
        val expi: Int, // 1200
        @SerializedName("fee")
        val fee: Int, // 0
        @SerializedName("flag")
        val flag: Int, // 1343489
        @SerializedName("freeTimeTrialPrivilege")
        val freeTimeTrialPrivilege: FreeTimeTrialPrivilege,
        @SerializedName("freeTrialInfo")
        val freeTrialInfo: Any?, // null
        @SerializedName("freeTrialPrivilege")
        val freeTrialPrivilege: FreeTrialPrivilege,
        @SerializedName("gain")
        val gain: Double, // -6.3072
        @SerializedName("id")
        val id: Int, // 33894312
        @SerializedName("level")
        val level: String, // exhigh
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
        val peak: Int, // 1
        @SerializedName("podcastCtrp")
        val podcastCtrp: Any?, // null
        @SerializedName("rightSource")
        val rightSource: Int, // 0
        @SerializedName("size")
        val size: Int, // 10691439
        @SerializedName("sr")
        val sr: Int, // 44100
        @SerializedName("time")
        val time: Int, // 267232
        @SerializedName("type")
        val type: String, // mp3
        @SerializedName("uf")
        val uf: Any?, // null
        @SerializedName("url")
        val url: String, // http://m7.music.126.net/20250717142827/bf4e29de325b55cf8e23c64c5335dae0/ymusic/0fd6/4f65/43ed/a8772889f38dfcb91c04da915b301617.mp3?vuutv=7iCEow9REOnu2ngSBcQIGLWOgi2uaBMWvz3Z3MuIVnny1pppb1cDrc0Hfvgl6zjiczDZMpA7OF7IJj/4oX7DAKI624bcm1CdAITERdV4Xts=
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