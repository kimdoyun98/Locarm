package com.project.locarm.main

import android.Manifest
import android.app.ActivityManager
import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.project.locarm.R
import com.project.locarm.common.MyApplication
import com.project.locarm.common.PreferenceUtil.Companion.DISTANCE
import com.project.locarm.data.SelectDestination
import com.project.locarm.data.room.Favorite
import com.project.locarm.databinding.ActivityMainBinding
import com.project.locarm.location.BackgroundLocationUpdateService
import com.project.locarm.search.SearchActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }
    private val searchDestinationResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectDestination = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    result.data!!.getParcelableExtra(SELECT, SelectDestination::class.java)
                } else {
                    result.data!!.getParcelableExtra(SELECT)
                }

                viewModel.setDestination(selectDestination!!)
            }
        }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BackgroundLocationUpdateService.LocationServiceBind

            if (viewModel.destination.value == null) {
                viewModel.setDestination(binder.getDestination())
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        searchDestination()
        initFavorites()
        alarmDistanceChange()
        checkRunningService()
        alarmButtonClick()
    }

    private fun checkRunningService() {
        if (viewModel.serviceState.value == ServiceState.Idle) {
            viewModel.setServiceState(
                if (checkRunService()) {
                    val serviceIntent = Intent(this, BackgroundLocationUpdateService::class.java)
                    bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

                    getServiceState(true)
                } else {
                    getServiceState(false)
                }
            )
        }
    }

    private fun alarmButtonClick() {
        binding.alarmButton.setOnClickListener {
            checkPermission()

            if (viewModel.destination.value == null) {
                Toast.makeText(
                    this,
                    getString(R.string.mainActivity_input_destination_toast_message),
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

            when (val serviceState = viewModel.serviceState.value!!) {
                is ServiceState.Idle -> {
                    checkRunService()
                }

                is ServiceState.RunService -> {
                    serviceState.onClick()

                    viewModel.setServiceState(
                        getServiceState(false)
                    )
                }

                is ServiceState.StopService -> {
                    serviceState.onClick()

                    viewModel.setServiceState(
                        getServiceState(true)
                    )
                }
            }
        }
    }

    private fun getServiceState(run: Boolean): ServiceState {
        val serviceIntent = Intent(this, BackgroundLocationUpdateService::class.java)
        return if (run) {
            ServiceState.RunService(
                onClick = {
                    stopService(serviceIntent)
                    unbindService(serviceConnection)
                }
            )
        } else {
            ServiceState.StopService(
                onClick = {
                    serviceIntent.apply {
                        putExtra(SELECT, viewModel.destination.value)
                    }
                    startService(serviceIntent)
                    bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
                }
            )
        }
    }

    private fun checkRunService(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        manager.getRunningServices(Integer.MAX_VALUE).forEach {
            if (SERVICE_NAME == it.service.className) {
                return true
            }
        }
        return false
    }

    private fun searchDestination() {
        binding.searchDestination.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            searchDestinationResult.launch(intent)
        }
    }

    private fun alarmDistanceChange() {
        binding.change.setOnClickListener {
            changeDistanceDialog()
        }
    }

    private fun initFavorites() {
        val adapter = FavoritesAdapter()
        binding.favorites.adapter = adapter
        viewModel.favoriteList.observe(this) {
            adapter.setData(it)

            adapter.setOnItemClickListener(object : FavoritesAdapter.OnItemClickListener {
                override fun onItemClicked(data: Favorite) {
                    viewModel.setDestination(
                        SelectDestination(
                            data.name,
                            data.latitude,
                            data.longitude
                        )
                    )
                }

                override fun onDeleteClicked(data: Favorite) {
                    viewModel.delete(data.id)
                }
            })
        }
    }

    private fun changeDistanceDialog() {
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
                        MyApplication.prefs.setAlarmDistance(DISTANCE, ONE_KM)
                    }

                    R.id.two -> {
                        MyApplication.prefs.setAlarmDistance(DISTANCE, TWO_KM)
                    }

                    R.id.three -> {
                        MyApplication.prefs.setAlarmDistance(DISTANCE, THREE_KM)
                    }

                    else -> {
                        val distance =
                            dialog.findViewById<EditText?>(R.id.another_text).text.toString()
                        MyApplication.prefs.setAlarmDistance(DISTANCE, distance.toInt() * ONE_KM)
                    }
                }

                viewModel.refreshDistance()

                Toast.makeText(
                    this,
                    getString(
                        R.string.mainActivity_change_alarm_distance_toast_message,
                        viewModel.distance.value
                    ),
                    Toast.LENGTH_LONG
                ).show()

                dialog.cancel()
            } catch (e: NumberFormatException) {
                Toast.makeText(
                    this,
                    getString(R.string.mainActivity_input_number_toast_message),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        cancel.setOnClickListener {
            dialog.cancel()
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
        private const val SERVICE_NAME =
            "com.project.locarm.location.BackgroundLocationUpdateService"
        private const val ONE_KM = 1000
        private const val TWO_KM = 2000
        private const val THREE_KM = 3000

        const val NAME = "name"
        const val SELECT = "select"
    }
}
