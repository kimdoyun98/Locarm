package com.project.locarm.main

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.project.locarm.BackgroundLocationUpdateService
import com.project.locarm.R
import com.project.locarm.common.MyApplication
import com.project.locarm.databinding.ActivityMainBinding
import com.project.locarm.room.Favorite
import com.project.locarm.search.SearchActivity

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    private var address : String? = null
    val viewModel : MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermission()

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        /** 알람 거리 **/
        binding.distanceText.text = (MyApplication.prefs.getAlarmDistance("distance")/1000).toString()


        /**
         * 목적지 검색
         */
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
                    viewModel.delete(data.id)
                }
            })
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
                viewModel.alarmCheck()

                MyApplication.prefs.setAddress("name", address!!)

                viewModel.alarm.observe(this){
                    if(it){
                        startService(Intent(this, BackgroundLocationUpdateService::class.java))
                    }
                    else{
                        stopService(Intent(this, BackgroundLocationUpdateService::class.java))
                    }
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