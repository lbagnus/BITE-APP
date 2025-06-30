package com.oasis.bite.data.model

import com.google.gson.annotations.SerializedName

class PassResetRequest (
    @SerializedName("email")
    val email: String,

    @SerializedName("newPassword")
    val newPassword: String
)