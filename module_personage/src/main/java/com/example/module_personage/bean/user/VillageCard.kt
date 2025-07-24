package com.example.module_personage.bean.user

data class VillageCard(
    val imageUrl: String,
    val name: String,
    val privacyKey: String,
    val targetUrl: String,
    val type: String,
    val villageCardTabExtensionVO: VillageCardTabExtensionVO
)