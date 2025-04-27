package com.project.locarm.common

import com.project.locarm.room.DataBase

class ServiceLocator(
    private val application: MyApplication
) {
    private val database = DataBase.getInstance(application)!!
    val dao = database.favoriteDao()


}
