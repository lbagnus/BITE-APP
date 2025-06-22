package com.oasis.bite.data.model

import com.google.gson.annotations.SerializedName

class CodeRequest (
    @SerializedName("email")
    val email: String,

    @SerializedName("code")
    val code: String
)