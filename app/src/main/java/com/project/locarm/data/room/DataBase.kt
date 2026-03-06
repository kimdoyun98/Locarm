package com.project.locarm.data.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Favorite::class], version = 2)
//@TypeConverters(DayListConverter::class)
abstract class DataBase : RoomDatabase() {
    abstract fun favoriteDao(): FavoritesDao
}
