package com.project.locarm.di

import androidx.room.Room
import com.project.locarm.common.MyApplication
import com.project.locarm.data.room.DataBase

object DatabaseFactory {

    fun createDatabase(): DataBase {
        return Room.databaseBuilder(
            MyApplication.instance,
            DataBase::class.java,
            "db"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }
}
