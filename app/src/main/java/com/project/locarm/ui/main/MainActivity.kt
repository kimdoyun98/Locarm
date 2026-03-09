package com.project.locarm.ui.main

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.project.locarm.R
import com.project.locarm.common.permission.LocarmPermission
import com.project.locarm.data.model.SelectDestination
import com.project.locarm.databinding.ActivityMainBinding
import com.project.locarm.location.BackgroundLocationUpdateService
import com.project.locarm.ui.favorite.FavoriteActivity
import com.project.locarm.ui.main.destination.SelectedDestinationFragment
import com.project.locarm.ui.main.destination.UnSelectedDestinationFragment
import com.project.locarm.ui.search.SearchActivity
import com.project.locarm.ui.view.TopStackingNotification
import kotlinx.coroutines.launch

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

        override fun onServiceDisconnected(name: ComponentName?) {}
    }
    private val locarmPermission = LocarmPermission(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.favoriteLayout.root.setOnClickListener {
            val intent = Intent(this, FavoriteActivity::class.java)
            searchDestinationResult.launch(intent)
        }

        locarmPermission.requestAllPermission()
        searchDestination()
        destinationFragment()
        checkRunningService()
        trackingButtonClick()
        trackingButtonClickAction()
        destinationNearByAlarm()
    }

    private fun destinationNearByAlarm() {
        lifecycleScope.launch {
            viewModel.destinationNearbyAlarm.collect {
                TopStackingNotification.make(
                    this@MainActivity,
                    getString(R.string.backgroundLocationUpdate_destination_nearby)
                ).show()
            }
        }
    }

    private fun destinationFragment() {
        val unSelectedDestinationFragment = UnSelectedDestinationFragment()
        val selectedDestinationFragment = SelectedDestinationFragment()

        viewModel.destination.observe(this) {
            val transaction = supportFragmentManager.beginTransaction()
            when (it) {
                null -> {
                    transaction.replace(R.id.destination_fragment, unSelectedDestinationFragment)
                }

                else -> {
                    transaction.replace(R.id.destination_fragment, selectedDestinationFragment)
                }
            }
            transaction.commit()
        }
    }

    private fun checkRunningService() {
        if (viewModel.serviceState.value == ServiceState.Idle) {
            viewModel.setServiceState(
                if (checkRunService()) {
                    val serviceIntent = Intent(this, BackgroundLocationUpdateService::class.java)
                    bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)

                    getServiceState(true)
                } else {
                    getServiceState(false)
                }
            )
        }
    }

    private fun trackingButtonClick() {
        binding.alarmButton.setOnClickListener {
            if (locarmPermission.checkLocationPermission()) {
                viewModel.onClickTrackingButton()
            } else {
                locarmPermission.requestAllPermission()
            }
        }
    }

    private fun trackingButtonClickAction() {
        lifecycleScope.launch {
            viewModel.trackingButtonClick.collect {
                if (viewModel.destination.value == null) {
                    showTopNotification(getString(R.string.mainActivity_input_destination_toast_message))

                    return@collect
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
                        locarmPermission.requestAllPermission()

                        serviceState.onClick()

                        viewModel.setServiceState(
                            getServiceState(true)
                        )
                    }
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
                        putExtra(DISTANCE_REMAINING, viewModel.getDistanceRemainingInteger())
                    }
                    startService(serviceIntent)
                    bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)
                }
            )
        }
    }

    private fun checkRunService(): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        manager.getRunningServices(Integer.MAX_VALUE).forEach {
            if (SERVICE_NAME == it.service.className) {
                return true
            }
        }
        return false
    }

    private fun searchDestination() {
        binding.searchDestination.setOnClickListener {
            navigateToSearchDestination()
        }
    }

    fun navigateToSearchDestination() {
        val intent = Intent(this, SearchActivity::class.java)
        searchDestinationResult.launch(intent)
    }

    private fun showTopNotification(message: String) {
        TopStackingNotification.make(this, message).show()
    }

    companion object {
        private const val SERVICE_NAME =
            "com.project.locarm.location.BackgroundLocationUpdateService"
        const val SELECT = "select"
        const val DISTANCE_REMAINING = "DistanceRemaining"
    }
}
