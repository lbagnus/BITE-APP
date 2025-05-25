package com.oasis.bite.domain.models

data class User(
    val email: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val role: Role
)
