package com.project.locarm.main

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.project.locarm.room.DataBase
import com.project.locarm.room.Favorite

class FavoriteViewModel(application: Application) : ViewModel() {
    private val database = DataBase.getInstance(application)!!
    private val dao = database.favoriteDao()
    val listAll : LiveData<List<Favorite>> = dao.getAll()

    fun getAll() : LiveData<List<Favorite>>{
        return dao.getAll()
    }

}