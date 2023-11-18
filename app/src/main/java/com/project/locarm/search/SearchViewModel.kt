package com.project.locarm.search

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.project.locarm.data.AddressDTO
import com.project.locarm.room.DataBase
import com.project.locarm.room.Favorite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : ViewModel() {
    private var _address = MutableLiveData<AddressDTO.Result.Juso>()
    private val database = DataBase.getInstance(application)!!
    private val dao = database.favoriteDao()

    val address : LiveData<AddressDTO.Result.Juso> = _address

    fun setData(data: AddressDTO.Result.Juso){
        _address.postValue(data)
    }

    fun getFavorite(name:String):LiveData<Favorite>{
        return dao.getFavorite(name)
    }

    fun insertFavorite(favorite: Favorite){
        try{
            CoroutineScope(Dispatchers.IO).launch {
                dao.insert(favorite)
            }
        }
        catch (e : Exception){ }
    }
}