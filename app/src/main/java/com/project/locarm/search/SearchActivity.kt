package com.project.locarm.search

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.project.locarm.R
import com.project.locarm.common.GeoCoder
import com.project.locarm.common.MyApplication
import com.project.locarm.common.PreferenceUtil.Companion.LATITUDE
import com.project.locarm.common.PreferenceUtil.Companion.LONGITUDE
import com.project.locarm.data.AddressDTO
import com.project.locarm.databinding.ActivitySearchBinding
import com.project.locarm.main.MainActivity.Companion.NAME
import com.project.locarm.room.Favorite

class SearchActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var binding : ActivitySearchBinding
    private val viewModel : SearchViewModel by viewModels { SearchViewModel.Factory }

    data class Loc(
        val latitude:Double,
        val longitude:Double
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        /**
         * 목적지 검색
         */
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.keyword = query!!
                viewModel.searchAddress(query, 1)
                binding.addressSlide.animateOpen()
                //키보드 숨기기
                val keyboard : InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                keyboard.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })

        val adapter = AddressAdapter()
        viewModel.result.observe(this){
            viewModel.pageCheck()

            adapter.setAddress(it.result.juso)
            binding.addressList.adapter = adapter

            val currentPage = it.result.common.currentPage.toInt()
            binding.nextPage.setOnClickListener {
                viewModel.searchAddress(viewModel.keyword, currentPage+1)
            }

            binding.backPage.setOnClickListener {
                viewModel.searchAddress(viewModel.keyword, currentPage-1)
            }
        }

        /**
         * 주소 선택 시
         */
        adapter.setOnItemClickListener(object : AddressAdapter.OnItemClickListener {
            override fun onItemClicked(data: AddressDTO.Result.Juso) {
                val geo = GeoCoder.getXY(this@SearchActivity, data.jibunAddr)
                viewModel.selectAddress = data.name
                viewModel.location = Loc(geo.latitude, geo.longitude)

                viewModel.setData(data)
                binding.addressSlide.animateClose()
            }
        })

        /**
         * 등록
         */
        binding.button.setOnClickListener {
            if(!binding.addressSlide.isOpened){
                MyApplication.prefs.setLocation(LATITUDE, viewModel.location.latitude)
                MyApplication.prefs.setLocation(LONGITUDE, viewModel.location.longitude)

                if(viewModel.selectAddress == null){
                    val input = EditText(this)
                    AlertDialog.Builder(this)
                        .setView(input)
                        .setTitle("해당 좌표의 이름을 정해주세요")
                        .setPositiveButton("확인"){ _, _ ->
                            viewModel.selectAddress = input.text.toString()
                            favoriteAlertDialog()
                        }
                        .create()
                        .show()
                }
                else favoriteAlertDialog()
            }
        }

        /**
         * Naver Map
         */
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

        /** 지도에 직접 클릭하여 좌표 선택 시 **/
        naverMap.setOnMapClickListener { point, coord ->
            viewModel.location = Loc(coord.latitude, coord.longitude)
            marker.position = LatLng(coord.latitude, coord.longitude)
            marker.map = naverMap
            viewModel.selectAddress = null
        }

        /** 목적지 검색 시 해당 위치 지도 표시 **/
        viewModel.address.observe(this){
            val location = GeoCoder.getXY(this, it.jibunAddr)
            val cameraUpdate = CameraUpdate.scrollTo(LatLng(location.latitude, location.longitude))

            marker.position = LatLng(location.latitude, location.longitude)
            marker.map = naverMap

            naverMap.moveCamera(cameraUpdate)
        }
    }


    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if(binding.addressSlide.isOpened) binding.addressSlide.animateClose()
        else finish()
    }

    /**
     * 즐겨찾기 유무 확인 후 없으면 AlertDialog
     */
    private fun favoriteAlertDialog(){
        intent.apply {
            putExtra(NAME, viewModel.selectAddress)
            setResult(RESULT_OK, intent)
        }

        viewModel.getFavorite(viewModel.selectAddress!!).observe(this){
            if(it==null){
                AlertDialog.Builder(this)
                    .setTitle(viewModel.selectAddress!!)
                    .setMessage("즐겨찾기에 추가하시겠습니까?")
                    .setPositiveButton("예") { _, _ ->
                        viewModel.insertFavorite(
                            Favorite(
                                name = viewModel.selectAddress!!,
                                latitude = viewModel.location.latitude,
                                longitude = viewModel.location.longitude
                            ))

                        finish()
                    }
                    .setNegativeButton("아니오") { _, _ ->
                        finish()
                    }
                    .create()
                    .show()
            }
            else finish()
        }
    }
}
