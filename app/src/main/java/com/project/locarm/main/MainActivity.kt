package com.project.locarm.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.project.locarm.BackgroundLocationUpdateService
import com.project.locarm.common.MyApplication
import com.project.locarm.databinding.ActivityMainBinding
import com.project.locarm.room.DataBase
import com.project.locarm.room.Favorite
import com.project.locarm.search.SearchActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    private var address : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel = FavoriteViewModel(application)
        val database = DataBase.getInstance(application)!!
        val dao = database.favoriteDao()

        checkPermission()

        val alarmStatus : Boolean = MyApplication.prefs.getBoolean("alarm", false)
        if(alarmStatus) binding.button.text = "알림 끄기"
        else binding.button.text = "위치 알림"

        binding.searchText.setOnClickListener {
            startActivityForResult(Intent(this, SearchActivity::class.java), 1)
        }

        /**
         * 즐겨찾기
         */
        viewModel.listAll.observe(this){
            Log.e("favorite", it.toString())
            val adapter = FavoritesAdapter().apply {
                setData(it)
            }
            binding.favorites.adapter = adapter
            adapter.setOnItemClickListener(object : FavoritesAdapter.OnItemClickListener {
                override fun onItemClicked(data: Favorite) {
                    //TODO
                    binding.destination.text = data.name
                    address = data.jibunAddress
                    MyApplication.prefs.setAddress("address", address!!)
                }

                override fun onDeleteClicked(data: Favorite) {
                    try {
                        CoroutineScope(Dispatchers.IO).launch {
                            dao.delete(data.id)
                        }
                    }
                    catch (e: Exception){
                    }
                }
            })
        }

        binding.allDelete.setOnClickListener {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    dao.deleteAll()
                }
            }
            catch (e: Exception){
            }
        }


        //TODO 실시간 위치
        binding.button.setOnClickListener {
            if(address == null){
                Toast.makeText(this, "목적지를 입력하세요", Toast.LENGTH_LONG).show()
            }
            else{
                if(!MyApplication.prefs.getBoolean("alarm", false)){
                    MyApplication.prefs.setBoolean("alarm", true)
                    binding.button.text = "알림 끄기"
                    startService(Intent(this, BackgroundLocationUpdateService::class.java))
                }
                else {
                    stopService(Intent(this, BackgroundLocationUpdateService::class.java))
                    MyApplication.prefs.setBoolean("alarm", false)
                    binding.button.text = "위치 알림"
                }
            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK){
            if(data != null){
                binding.destination.text = data.getStringExtra("name")
                address = data.getStringExtra("address")
                MyApplication.prefs.setAddress("address", address!!)
                MyApplication.prefs.setAddress("name", data.getStringExtra("name")!!)
            }
        }
    }

    private fun checkPermission(){
        val permissionArray =
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

        if (permissionArray.all
            { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED}){
            requestPermissions(permissionArray, 1000)
        }
    }

}