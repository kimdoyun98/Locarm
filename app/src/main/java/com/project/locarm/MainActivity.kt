package com.project.locarm

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.project.locarm.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermission()

        val alarmStatus : Boolean = MyApplication.prefs.getBoolean("alarm", false)
        if(alarmStatus) binding.button.text = "알림 끄기"
        else binding.button.text = "위치 알림"

        binding.searchText.setOnClickListener {
            startActivityForResult(Intent(this, SearchActivity::class.java), 1)
            //startActivity(Intent(this, SearchActivity::class.java))
        }

        //TODO 실시간 위치
        binding.button.setOnClickListener {
            Log.e("alarmStatus", alarmStatus.toString())
            if(!alarmStatus){
                MyApplication.prefs.setBoolean("alarm", true)
                binding.button.text = "알림 끄기"
                startService(Intent(this, BackgroundLocationUpdateService::class.java))
            }
            else {
                MyApplication.prefs.setBoolean("alarm", false)
                binding.button.text = "위치 알림"
                stopService(Intent(this, BackgroundLocationUpdateService::class.java))
            }

        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK){
            if(data != null){
                binding.destination.text = data.getStringExtra("name")
                //data.getStringExtra("address")
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