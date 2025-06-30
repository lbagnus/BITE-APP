package com.oasis.bite.localdata.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.oasis.bite.localdata.database.dao.UserDao
import com.oasis.bite.localdata.database.entities.LocalUser
import androidx.room.TypeConverters
import com.oasis.bite.localdata.database.dao.RecetaDao
import com.oasis.bite.localdata.database.entities.LocalReceta

@Database(
    entities = [LocalUser::class, LocalReceta::class],
    version = 4
)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun recetaDao(): RecetaDao
}


