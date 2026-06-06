package com.project.locarm.ui.main

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import com.project.locarm.R
import com.project.locarm.common.activityLifecycleScope
import com.project.locarm.common.appContainer
import com.project.locarm.common.permission.LocarmPermission
import com.project.locarm.data.model.SelectDestination
import com.project.locarm.databinding.ActivityMainBinding
import com.project.locarm.di.PermissionFactory
import com.project.locarm.location.BackgroundLocationUpdateService
import com.project.locarm.location.LocationSettings
import com.project.locarm.location.util.LocationState
import com.project.locarm.ui.favorite.FavoriteActivity
import com.project.locarm.ui.main.MainViewModel.Companion.LOCATION_DISABLED
import com.project.locarm.ui.main.MainViewModel.Companion.LOCATION_PERMISSION_DENIED
import com.project.locarm.ui.main.MainViewModel.Companion.SERVICE_READY
import com.project.locarm.ui.main.destination.SelectedDestinationFragment
import com.project.locarm.ui.main.destination.UnSelectedDestinationFragment
import com.project.locarm.ui.search.SearchActivity
import com.project.locarm.ui.view.TopStackingNotification

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val locationObserver by lazy { applicationContext.appContainer.locationObserver }
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.factory(
            applicationContext.appContainer.preference,
            applicationContext.appContainer.locationRepository,
            locationObserver,
            applicationContext.appContainer.realTimeLocation,
            appContainer.adsRepository,
        )
    }

    //private val serviceIntent = Intent(this, BackgroundLocationUpdateService::class.java)
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
    private val locarmPermission by lazy {
        PermissionFactory.createLocarmPermission(
            this,
            viewModel::isGrantedLocationPermission,
        )
    }

    private val adManager by lazy { appContainer.interstitialAdManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        windowInset()

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        adManager.load(this)

        initFavoriteContent()
        locationPermissionState()
        searchDestination()
        destinationFragment()
        checkRunningService()
        trackingButtonClickAction()
        unknownDestinationNotification()
    }

    private fun initFavoriteContent() {
        binding.favoriteLayout.root.setOnClickListener {
            val intent = Intent(this, FavoriteActivity::class.java)
            searchDestinationResult.launch(intent)
        }
    }

    private fun locationPermissionState() {
        activityLifecycleScope(Lifecycle.State.CREATED) {
            viewModel.locationPermissionState.collect { state ->
                when (state) {
                    LocationState.PermissionDenied -> {
                        locarmPermission.requestAllPermission()
                    }

                    LocationState.LocationDisabled -> {
                        LocationSettings.checkLocationSettings(this@MainActivity)
                    }

                    else -> locarmPermission
                }
            }
        }
    }

    private fun destinationFragment() {
        val unSelectedDestinationFragment = UnSelectedDestinationFragment()
        val selectedDestinationFragment = SelectedDestinationFragment()

        activityLifecycleScope {
            viewModel.destination.collect {
                val transaction = supportFragmentManager.beginTransaction()
                when (it) {
                    null -> {
                        transaction.replace(
                            R.id.destination_fragment,
                            unSelectedDestinationFragment
                        )
                    }

                    else -> {
                        transaction.replace(R.id.destination_fragment, selectedDestinationFragment)
                    }
                }
                transaction.commit()
            }
        }
    }

    private fun checkRunningService() {
        if (viewModel.serviceState.value == ServiceState.Idle) {
            viewModel.setServiceState(
                if (checkRunService()) {
                    val serviceIntent = Intent(this, BackgroundLocationUpdateService::class.java)
                    bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)

                    ServiceState.RunService
                } else {
                    ServiceState.StopService
                }
            )
        }
    }

    private fun unknownDestinationNotification() {
        activityLifecycleScope {
            viewModel.unknownDestination.collect {
                showTopNotification(getString(R.string.mainActivity_input_destination_toast_message))
            }
        }
    }

    private fun trackingButtonClickAction() {
        activityLifecycleScope {
            viewModel.trackingButtonClick.collect {
                when (it) {
                    LOCATION_PERMISSION_DENIED -> {
                        if (LocarmPermission.checkTiramisuVersionHigher() && !locarmPermission.checkNotificationPermission()) {
                            locarmPermission.requestAllPermission()
                        } else {
                            locarmPermission.requestLocationPermission()
                        }
                    }

                    LOCATION_DISABLED -> {
                        LocationSettings.checkLocationSettings(this@MainActivity)
                    }

                    SERVICE_READY -> {
                        runServiceAction()
                    }
                }
            }
        }
    }

    private fun runServiceAction() {
        val serviceIntent = Intent(this, BackgroundLocationUpdateService::class.java)
        when (viewModel.serviceState.value) {
            is ServiceState.Idle -> Unit

            is ServiceState.RunService -> {
                stopTrackingService(serviceIntent)

                viewModel.setServiceState(ServiceState.StopService)
            }

            is ServiceState.StopService -> {
                if (LocarmPermission.checkTiramisuVersionHigher() && !locarmPermission.checkNotificationPermission()) {
                    locarmPermission.requestNotificationPermission()
                }

                activityLifecycleScope {
                    if (viewModel.shouldShowAd()) {
                        viewModel.updateAdsLastTime()

                        adManager.show(this@MainActivity) {
                            runTrackingService(serviceIntent)
                            viewModel.setServiceState(ServiceState.RunService)
                        }
                    } else {
                        runTrackingService(serviceIntent)
                        viewModel.setServiceState(ServiceState.RunService)
                    }
                }
            }
        }
    }

    private fun runTrackingService(serviceIntent: Intent) {
        serviceIntent.apply {
            putExtra(SELECT, viewModel.destination.value)
            putExtra(DISTANCE_REMAINING, viewModel.getDistanceRemainingInteger())
        }
        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)
    }

    private fun stopTrackingService(serviceIntent: Intent) {
        stopService(serviceIntent)
        unbindService(serviceConnection)
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

    private fun windowInset() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = true

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    companion object {
        private const val SERVICE_NAME =
            "com.project.locarm.location.BackgroundLocationUpdateService"
        const val SELECT = "select"
        const val DISTANCE_REMAINING = "DistanceRemaining"
    }
}
