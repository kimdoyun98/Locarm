package com.project.locarm.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.project.locarm.room.DataBase
import com.project.locarm.room.Favorite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = DataBase.getInstance(application)!!
    private val dao = database.favoriteDao()

    private val _alarm = MutableLiveData<Boolean>().apply { value = false }
    val alarm : LiveData<Boolean> = _alarm

    val listAll : LiveData<List<Favorite>> = dao.getAll()

    fun alarmCheck(){
        if(alarm.value!!) _alarm.postValue(false)
        else _alarm.postValue(true)
    }

    fun allDelete(){
        try {
            CoroutineScope(Dispatchers.IO).launch {
                dao.deleteAll()
            }
        }
        catch (e: Exception){
        }
    }

    fun delete(id:Int){
        try {
            CoroutineScope(Dispatchers.IO).launch {
                dao.delete(id)
            }
        }
        catch (e: Exception){
        }
    }
}