package com.tarush27.fbauthdemo

import com.google.gson.annotations.SerializedName

data class ConfirmationMessage(
    @SerializedName("logout_message")
    val logout_message: String? = null,
    @SerializedName("btn_yes")
    val btn_yes: String? = null,
    @SerializedName("btn_no")
    val btn_no: String? = null
)
