package com.oasis.bite.localdata.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.oasis.bite.domain.models.Role
import java.util.UUID

@Entity(tableName = "user_session")
data class LocalUser(
    @PrimaryKey val email: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val role: Role
)