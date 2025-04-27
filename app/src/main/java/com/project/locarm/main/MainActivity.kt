package com.project.locarm.main

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.project.locarm.BackgroundLocationUpdateService
import com.project.locarm.R
import com.project.locarm.common.MyApplication
import com.project.locarm.common.PreferenceUtil.Companion.DISTANCE
import com.project.locarm.common.PreferenceUtil.Companion.LATITUDE
import com.project.locarm.common.PreferenceUtil.Companion.LONGITUDE
import com.project.locarm.databinding.ActivityMainBinding
import com.project.locarm.room.Favorite
import com.project.locarm.search.SearchActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        checkPermission()

        binding.searchDestination.setOnClickListener {
            startActivityForResult(Intent(this, SearchActivity::class.java), 1)
        }

        val adapter = FavoritesAdapter()
        binding.favorites.adapter = adapter
        viewModel.favoriteList.observe(this) {
            adapter.setData(it)

            adapter.setOnItemClickListener(object : FavoritesAdapter.OnItemClickListener {
                override fun onItemClicked(data: Favorite) {
                    viewModel.setAddress(data.name)
                    MyApplication.prefs.setLocation(LATITUDE, data.latitude)
                    MyApplication.prefs.setLocation(LONGITUDE, data.longitude)
                }

                override fun onDeleteClicked(data: Favorite) {
                    viewModel.delete(data.id)
                }
            })
        }

        binding.change.setOnClickListener {
            changeDistanceDialog()
        }

        binding.alarmButton.setOnClickListener {
            if (viewModel.address.value == "목적지") {
                Toast.makeText(this, "목적지를 입력하세요", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            viewModel.alarmCheck()
            MyApplication.prefs.setAddress(NAME, viewModel.address.value!!)
        }

        viewModel.alarmStatus.observe(this) {
            if (it) {
                startService(Intent(this, BackgroundLocationUpdateService::class.java))
            } else {
                stopService(Intent(this, BackgroundLocationUpdateService::class.java))
            }
        }
    }

    private fun changeDistanceDialog(){
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.alarm_distance_dialog)
        dialog.show()

        val check = dialog.findViewById<Button>(R.id.check)
        val cancel = dialog.findViewById<Button>(R.id.cancel)
        val radioGroup = dialog.findViewById<RadioGroup>(R.id.radioGroup)

        radioGroup.setOnCheckedChangeListener { _, p1 ->
            val anotherLayout = dialog.findViewById<LinearLayout>(R.id.another_layout)
            when (p1) {
                R.id.another -> anotherLayout.visibility = View.VISIBLE
                else -> anotherLayout.visibility = View.INVISIBLE
            }
        }

        check.setOnClickListener {
            try {
                when (radioGroup.checkedRadioButtonId) {
                    R.id.one -> {
                        MyApplication.prefs.setAlarmDistance(DISTANCE, 1000)
                    }

                    R.id.two -> {
                        MyApplication.prefs.setAlarmDistance(DISTANCE, 2000)
                    }

                    R.id.three -> {
                        MyApplication.prefs.setAlarmDistance(DISTANCE, 3000)
                    }

                    else -> {
                        val distance =
                            dialog.findViewById<EditText?>(R.id.another_text).text.toString()
                        MyApplication.prefs.setAlarmDistance(DISTANCE, distance.toInt() * 1000)
                    }
                }

                viewModel.refreshDistance()

                Toast.makeText(
                    this,
                    "목적지로부터 ${viewModel.distance.value}Km 이내 접근 시 알람이 울립니다.",
                    Toast.LENGTH_LONG
                ).show()

                dialog.cancel()
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "숫자를 입력해주세요.", Toast.LENGTH_LONG).show()
            }
        }

        cancel.setOnClickListener {
            dialog.cancel()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null) {
                viewModel.setAddress(data.getStringExtra(NAME))
            }
        }
    }

    private fun checkPermission() {
        val permissionArray =
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

        if (permissionArray.all
            { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        ) {
            requestPermissions(permissionArray, 1000)
        }
    }

    companion object {
        private const val NAME = "name"
    }
}
