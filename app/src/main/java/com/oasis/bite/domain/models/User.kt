package com.oasis.bite.domain.models

data class User(
    val email: String,
    val username: String,
    val firstname: String,
    val lastname: String,
    val role: Role
)

