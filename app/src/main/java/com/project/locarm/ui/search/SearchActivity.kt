package com.project.locarm.ui.search

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.project.locarm.R
import com.project.locarm.data.model.SelectDestination
import com.project.locarm.databinding.ActivitySearchBinding
import com.project.locarm.databinding.DestinationTitleInputLayoutBinding
import com.project.locarm.databinding.SearchResultLayoutBinding
import com.project.locarm.location.GeoCoder
import com.project.locarm.ui.main.MainActivity.Companion.SELECT
import com.project.locarm.ui.search.adapter.PagingAdapter
import com.project.locarm.ui.search.util.SelectDestinationState
import com.project.locarm.ui.view.LocarmSnackBar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModels { SearchViewModel.Factory }
    private val adapter = PagingAdapter()
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private val fusedLocationSource = FusedLocationSource(this, 5000)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        initToolbarNavigationButton()
        initBottomSheetDialog()
        searchDestination()
        selectAddress()
        selectDestinationButton()
        initNaverMap()
    }

    private fun searchDestination() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.searchAddress(query!!).collectLatest(adapter::submitData)
                    }
                }

                bottomSheetDialog.show()
                hideKeyBoard()
                binding.searchView.clearFocus()

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }

    private fun selectAddress() {
        adapter.setOnItemClickListener { juso ->
            if (juso == null) return@setOnItemClickListener

            val geo = GeoCoder.getXY(this@SearchActivity, juso.jibunAddr)
            viewModel.selectSearchResult(juso, geo)

            bottomSheetDialog.dismiss()
        }
    }

    private fun initNaverMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.naverMap) as MapFragment?
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().add(R.id.naverMap, it).commit()
            }
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(naverMap: NaverMap) {
        val marker = Marker()

        naverMap.locationSource = fusedLocationSource

        // 지도에 직접 클릭하여 좌표 선택 시
        naverMap.setOnMapClickListener { point, coord ->
            viewModel.selectDestinationOnMap(latitude = coord.latitude, longitude = coord.longitude)

            marker.position = LatLng(coord.latitude, coord.longitude)
            marker.map = naverMap
        }

        // 목적지 검색 시 해당 위치 지도 표시
        lifecycleScope.launch {
            viewModel.selectDestinationState.collect { state ->
                if (state is SelectDestinationState.SelectSearchResult) {
                    val latitude = state.result.latitude
                    val longitude = state.result.longitude
                    val cameraUpdate = CameraUpdate.scrollTo(LatLng(latitude, longitude))

                    marker.position = LatLng(latitude, longitude)
                    marker.map = naverMap

                    naverMap.moveCamera(cameraUpdate)
                }
            }
        }
    }

    private fun selectDestinationButton() {
        binding.selectDestinationBt.setOnClickListener {
            when (val state = viewModel.selectDestinationState.value) {
                is SelectDestinationState.Idle -> Unit

                is SelectDestinationState.SelectOnMap -> {
                    val dialogBinding =
                        DestinationTitleInputLayoutBinding.inflate(LayoutInflater.from(this))

                    val dialog = AlertDialog.Builder(this)
                        .setView(dialogBinding.root)
                        .create()

                    dialogBinding.editButton.setOnClickListener {
                        notifyFavorite(
                            SelectDestination(
                                name = dialogBinding.destinationEditText.text.toString(),
                                latitude = state.location.latitude,
                                longitude = state.location.longitude
                            )
                        )

                        dialog.dismiss()
                    }

                    dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                    dialog.show()
                }

                is SelectDestinationState.SelectSearchResult -> {
                    notifyFavorite(state.result)
                }
            }
        }
    }

    private fun notifyFavorite(
        selectDestination: SelectDestination
    ) {
        intent.apply {
            putExtra(SELECT, selectDestination)
            setResult(RESULT_OK, intent)
        }

        if (viewModel.getFavorite(selectDestination.name) != null) {
            finish()
            return
        }

        showSnackBar(selectDestination)
    }

    private fun showSnackBar(selectDestination: SelectDestination) {
        val snackBar =
            LocarmSnackBar.make(
                this,
                getString(R.string.searchActivity_dialog_add_favorites_message),
                LocarmSnackBar.SHORT
            )

        snackBar
            .setAction(getString(R.string.searchActivity_dialog_post_button)) {
                viewModel.insertFavorite(selectDestination)
                finish()
            }
            .setNegativeAction(getString(R.string.searchActivity_dialog_negative_button)) {
                finish()
            }
            .setDisMissAction { finish() }
            .show()
    }

    private fun initToolbarNavigationButton() {
        binding.searchToolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun initBottomSheetDialog() {
        bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetTheme)

        val bsBinding = SearchResultLayoutBinding.inflate(LayoutInflater.from(this))
        bsBinding.searchAddressRv.adapter = adapter

        bottomSheetDialog.setContentView(bsBinding.root)
    }

    private fun hideKeyBoard() {
        val keyboard: InputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        keyboard.hideSoftInputFromWindow(
            currentFocus?.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (bottomSheetDialog.isShowing) bottomSheetDialog.dismiss()
        else finish()
    }
}
