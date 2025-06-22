package com.oasis.bite.data.model

import com.google.gson.annotations.SerializedName

class PassRequest (
    @SerializedName("email")
    val email: String,

    @SerializedName("oldPassword")
    val oldPassword: String,

    @SerializedName("newPassword")
    val newPassword: String
)
