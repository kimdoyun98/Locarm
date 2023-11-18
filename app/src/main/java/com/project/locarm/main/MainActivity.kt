package com.project.locarm.main

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.MultiTapKeyListener
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.project.locarm.BackgroundLocationUpdateService
import com.project.locarm.R
import com.project.locarm.common.MyApplication
import com.project.locarm.databinding.ActivityMainBinding
import com.project.locarm.room.DataBase
import com.project.locarm.room.Favorite
import com.project.locarm.search.SearchActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.NumberFormatException

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

        binding.distanceText.text = (MyApplication.prefs.getAlarmDistance("distance")/1000).toString()

        binding.searchText.setOnClickListener {
            startActivityForResult(Intent(this, SearchActivity::class.java), 1)
        }

        /**
         * 즐겨찾기
         */
        viewModel.listAll.observe(this){
            val adapter = FavoritesAdapter().apply {
                setData(it)
            }
            binding.favorites.adapter = adapter
            adapter.setOnItemClickListener(object : FavoritesAdapter.OnItemClickListener {
                override fun onItemClicked(data: Favorite) {
                    address = data.name
                    binding.destination.text = address
                    MyApplication.prefs.setLocation("latitude", data.latitude)
                    MyApplication.prefs.setLocation("longitude", data.longitude)
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

        /**
         * 즐겨찾기 전체 삭제
         */
        binding.allDelete.setOnClickListener {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    dao.deleteAll()
                }
            }
            catch (e: Exception){
            }
        }

        /**
         * 알람 거리 변경
         */
        binding.change.setOnClickListener {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.alarm_distance_dialog)

            dialog.show()
            val check = dialog.findViewById<Button>(R.id.check)
            val cancel = dialog.findViewById<Button>(R.id.cancel)
            val radioGroup = dialog.findViewById<RadioGroup>(R.id.radioGroup)

            radioGroup.setOnCheckedChangeListener { _, p1 ->
                val anotherLayout = dialog.findViewById<LinearLayout>(R.id.another_layout)
                when(p1){
                    R.id.another -> anotherLayout.visibility = View.VISIBLE
                    else -> anotherLayout.visibility = View.INVISIBLE
                }
            }

            check.setOnClickListener {
                lateinit var distance: String
                try{
                    when(radioGroup.checkedRadioButtonId){
                        R.id.one -> {
                            MyApplication.prefs.setAlarmDistance("distance", 1000)
                            distance = "1"
                        }
                        R.id.two -> {
                            MyApplication.prefs.setAlarmDistance("distance", 2000)
                            distance = "2"
                        }
                        R.id.three -> {
                            MyApplication.prefs.setAlarmDistance("distance", 3000)
                            distance = "3"
                        }
                        else -> {
                            distance = dialog.findViewById<EditText?>(R.id.another_text).text.toString()
                            MyApplication.prefs.setAlarmDistance("distance", distance.toInt()*1000)
                        }
                    }
                    binding.distanceText.text = distance
                    Toast.makeText(this, "목적지로부터 ${distance}Km 이내 접근 시 알람이 울립니다.", Toast.LENGTH_LONG).show()
                    dialog.cancel()
                }
                catch (e:NumberFormatException){
                    Toast.makeText(this, "숫자를 입력해주세요.", Toast.LENGTH_LONG).show()
                }
            }

            cancel.setOnClickListener {
                dialog.cancel()
            }
        }

        /**
         * 알람 시작 버튼
         */
        binding.button.setOnClickListener {
            if(address == null){
                Toast.makeText(this, "목적지를 입력하세요", Toast.LENGTH_LONG).show()
            }
            else{
                MyApplication.prefs.setAddress("name", address!!)

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
                address = data.getStringExtra("name")
                binding.destination.text = address
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