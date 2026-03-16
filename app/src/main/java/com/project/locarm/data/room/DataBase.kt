package com.project.locarm.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.project.locarm.data.room.dao.AddressEntityDao
import com.project.locarm.data.room.dao.AddressRemoteKeyDao
import com.project.locarm.data.room.dao.FavoritesDao
import com.project.locarm.data.room.entitiy.AddressEntity
import com.project.locarm.data.room.entitiy.AddressRemoteKey
import com.project.locarm.data.room.entitiy.Favorite

@Database(
    entities = [Favorite::class, AddressRemoteKey::class, AddressEntity::class],
    version = 3
)
abstract class DataBase : RoomDatabase() {
    abstract fun favoriteDao(): FavoritesDao
    abstract fun addressEntityDao(): AddressEntityDao
    abstract fun addressRemoteKeyDao(): AddressRemoteKeyDao
}
