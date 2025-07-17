package com.example.module_personage.bean.history

data class FreeTrialPrivilege(
    val cannotListenReason: Int,
    val freeLimitTagType: Any,
    val listenType: Int,
    val playReason: Any,
    val resConsumable: Boolean,
    val userConsumable: Boolean
)