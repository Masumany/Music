package com.example.module_personage.bean.user

data class Userdetail(
    val adValid: Boolean,
    val bindings: List<Binding>,
    val code: Int,
    val createDays: Int,
    val createTime: Long,
    val level: Int,
    val listenSongs: Int,
    val mobileSign: Boolean,
    val newUser: Boolean,
    val pcSign: Boolean,
    val peopleCanSeeMyPlayRecord: Boolean,
    val profile: Profile,
    val profileVillageInfo: ProfileVillageInfo,
    val recallUser: Boolean,
    val userPoint: UserPoint,
    val villageCard: VillageCard
)