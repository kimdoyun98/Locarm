package com.project.locarm.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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
import com.project.locarm.common.GeoCoder
import com.project.locarm.data.Loc
import com.project.locarm.data.SelectDestination
import com.project.locarm.data.room.Favorite
import com.project.locarm.databinding.ActivitySearchBinding
import com.project.locarm.databinding.SearchResultLayoutBinding
import com.project.locarm.main.MainActivity.Companion.SELECT
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivitySearchBinding
    private val viewModel: SearchViewModel by viewModels { SearchViewModel.Factory }
    private val adapter = PagingAdapter()
    private lateinit var bottomSheetDialog: BottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initBottomSheetDialog()
        searchDestination()
        selectAddress()
        selectDestination()
        initNaverMap()

    }

    private fun searchDestination() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.keyword = query!!
                lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        viewModel.searchAddress(query).collectLatest(adapter::submitData)
                    }
                }

                bottomSheetDialog.show()

                hideKeyBoard()

                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }

    private fun hideKeyBoard() {
        val keyboard: InputMethodManager =
            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        keyboard.hideSoftInputFromWindow(
            currentFocus?.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
    }

    private fun initBottomSheetDialog() {
        bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetTheme)

        val bsBinding = SearchResultLayoutBinding.inflate(LayoutInflater.from(this))
        bsBinding.searchAddressRv.adapter = adapter

        bottomSheetDialog.setContentView(bsBinding.root)
    }

    private fun selectAddress() {
        adapter.setOnItemClickListener { juso ->
            if (juso == null) return@setOnItemClickListener

            val geo = GeoCoder.getXY(this@SearchActivity, juso.jibunAddr)
            viewModel.location = Loc(geo.latitude, geo.longitude)

            viewModel.selectDestination = SelectDestination(
                name = juso.name,
                latitude = geo.latitude,
                longitude = geo.longitude
            )

            viewModel.setAddressJuso(juso)

            bottomSheetDialog.dismiss()
        }
    }

    private fun selectDestination() {
        binding.selectDestinationBt.setOnClickListener {
            if (viewModel.selectDestination == null) {
                val input = EditText(this)
                AlertDialog.Builder(this)
                    .setView(input)
                    .setTitle(getString(R.string.searchActivity_dialog_input_destination_name))
                    .setPositiveButton(getString(R.string.searchActivity_dialog_done)) { _, _ ->
                        viewModel.selectDestination = SelectDestination(
                            name = input.text.toString(),
                            latitude = viewModel.location.latitude,
                            longitude = viewModel.location.longitude
                        )

                        favoriteAlertDialog()
                    }
                    .create()
                    .show()
            } else favoriteAlertDialog()
        }
    }

    private fun initNaverMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.naverMap) as MapFragment?
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().add(R.id.naverMap, it).commit()
            }
        mapFragment.getMapAsync(this)
        viewModel.locationSource = FusedLocationSource(this, 5000)
    }

    override fun onMapReady(naverMap: NaverMap) {
        val marker = Marker()

        naverMap.locationSource = viewModel.locationSource

        // 지도에 직접 클릭하여 좌표 선택 시
        naverMap.setOnMapClickListener { point, coord ->
            viewModel.location = Loc(coord.latitude, coord.longitude)
            marker.position = LatLng(coord.latitude, coord.longitude)
            marker.map = naverMap

            viewModel.selectDestination = null
        }

        // 목적지 검색 시 해당 위치 지도 표시
        viewModel.address.observe(this) {
            val location = GeoCoder.getXY(this, it.jibunAddr)
            val cameraUpdate = CameraUpdate.scrollTo(LatLng(location.latitude, location.longitude))

            marker.position = LatLng(location.latitude, location.longitude)
            marker.map = naverMap

            naverMap.moveCamera(cameraUpdate)
        }
    }


    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (bottomSheetDialog.isShowing) bottomSheetDialog.dismiss()
        else finish()
    }

    private fun favoriteAlertDialog() {
        val selectDestination = viewModel.selectDestination!!

        intent.apply {
            putExtra(SELECT, selectDestination)
            setResult(RESULT_OK, intent)
        }

        viewModel.getFavorite(selectDestination.name).observe(this) {
            if (it != null) finish()

            AlertDialog.Builder(this)
                .setTitle(selectDestination.name)
                .setMessage(getString(R.string.searchActivity_dialog_add_favorites_message))
                .setPositiveButton(getString(R.string.searchActivity_dialog_post_button)) { _, _ ->
                    viewModel.insertFavorite(
                        Favorite(
                            name = selectDestination.name,
                            latitude = selectDestination.latitude,
                            longitude = selectDestination.longitude
                        )
                    )

                    finish()
                }
                .setNegativeButton(getString(R.string.searchActivity_dialog_negative_button)) { _, _ ->
                    finish()
                }
                .create()
                .show()
        }
    }
}
