package com.project.locarm.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.project.locarm.ApiService
import com.project.locarm.RetrofitManager
import com.project.locarm.data.AddressDTO
import com.project.locarm.room.DataBase
import com.project.locarm.room.Favorite
import kotlinx.coroutines.*

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val addressApi = RetrofitManager.getRetrofitInstance().create(ApiService::class.java)
    private var coroutine : Job = Job()

    private var _address = MutableLiveData<AddressDTO.Result.Juso>()
    val address : LiveData<AddressDTO.Result.Juso> = _address

    private val database = DataBase.getInstance(application)!!
    private val dao = database.favoriteDao()

    private val _result = MutableLiveData<AddressDTO>()
    val result : LiveData<AddressDTO> = _result

    private val _back = MutableLiveData<Boolean>().apply { value = false }
    val back : LiveData<Boolean> = _back

    private val _next = MutableLiveData<Boolean>().apply { value = false }
    val next : LiveData<Boolean> = _next

    fun searchAddress(keyword:String, page: Int){
        coroutine =
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    val response = addressApi.getAddress(keyword, page)
                    withContext(Dispatchers.Main){
                        if(response.isSuccessful){
                            _result.postValue(response.body())
                        }
                    }
                }
            }
            catch (e:Exception){ Job() }
    }

    fun pageCheck() {
        val it = result.value!!
        if(it.result.common.currentPage.toInt() > 1){
            //TODO 이전 버튼
            _back.postValue(true)
        }
        else _back.postValue(false)

        if(it.result.common.totalCount.toInt() > it.result.common.currentPage.toInt()*it.result.common.countPerPage.toInt()){
            //TODO 전체 개수 > (현재 페이지 * 페이지당 개수) 보이게
            _next.postValue(true)
        }
        else _next.postValue(false)

    }

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

    override fun onCleared() {
        super.onCleared()
        coroutine.cancel()
    }
}