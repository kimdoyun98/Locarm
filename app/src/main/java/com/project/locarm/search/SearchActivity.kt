package com.project.locarm.search

import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.project.locarm.ApiService
import com.project.locarm.R
import com.project.locarm.RetrofitManager
import com.project.locarm.common.GeoCoder
import com.project.locarm.common.MyApplication
import com.project.locarm.data.AddressDTO
import com.project.locarm.databinding.ActivitySearchBinding
import com.project.locarm.room.Favorite
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var binding : ActivitySearchBinding
    private lateinit var adapter: AddressAdapter
    private val retrofit2 = RetrofitManager.getRetrofitInstance().create(ApiService::class.java)
    private var address : String? = null
    private lateinit var viewModel:SearchViewModel
    private lateinit var locationSource: FusedLocationSource
    private lateinit var location : Loc

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = SearchViewModel(application)

        adapter = AddressAdapter()

        /**
         * 목적지 검색
         */
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                retrofit2.getAddress(query)
                    .enqueue(object : Callback<AddressDTO>{
                        override fun onResponse(call: Call<AddressDTO>, response: Response<AddressDTO>) {
                            Log.e("response" , response.body().toString())
                            adapter.setAddress(response.body()?.result?.juso)

                            binding.addressList.adapter = adapter

                            binding.addressSlide.animateOpen()
                        }

                        override fun onFailure(call: Call<AddressDTO>, t: Throwable) {
                            Log.e("onFailure", t.message.toString())
                        }

                    })
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                //사용 X
                return false
            }
        })

        /**
         * 주소 선택 시
         */
        adapter.setOnItemClickListener(object : AddressAdapter.OnItemClickListener {
            override fun onItemClicked(data: AddressDTO.Result.Juso) {
                val geo = GeoCoder.getXY(this@SearchActivity, data.jibunAddr)
                address = data.name
                location = Loc(geo.latitude, geo.longitude)

                viewModel.setData(data)
                binding.addressSlide.animateClose()
            }
        })

        /**
         * 등록
         */
        binding.button.setOnClickListener {
            intent.apply {
                putExtra("name", address)
                setResult(RESULT_OK, intent)
            }

            MyApplication.prefs.setLocation("latitude", location.latitude)
            MyApplication.prefs.setLocation("longitude", location.longitude)

            /** 지도 좌표 설정으로 선택 시 좌표 이름 설정 **/
            if(address == null){
                //TODO 좌표에 이름 설정
                val input = EditText(this)
                AlertDialog.Builder(this)
                    .setView(input)
                    .setTitle("해당 좌표의 이름을 정해주세요")
                    .setPositiveButton("확인"){ _, _ ->
                        address = input.text.toString()
                        favoriteAlertDialog()
                    }
                    .create()
                    .show()
            }
            else favoriteAlertDialog()
        }

        /**
         * Naver Map
         */
        val mapFragment = supportFragmentManager.findFragmentById(R.id.naverMap) as MapFragment?
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().add(R.id.naverMap, it).commit()
            }
        mapFragment.getMapAsync(this)
        locationSource = FusedLocationSource(this, 5000)
    }

    override fun onMapReady(naverMap: NaverMap) {
        val marker = Marker()

        naverMap.locationSource = locationSource

        /** 지도에 직접 클릭하여 좌표 선택 시 **/
        naverMap.setOnMapClickListener { point, coord ->
            location = Loc(coord.latitude, coord.longitude)
            marker.position = LatLng(coord.latitude, coord.longitude)
            marker.map = naverMap
            address = null
            Log.e("onMapReady", address.toString())
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


    override fun onBackPressed() {
        super.onBackPressed()
        if(binding.addressSlide.isOpened) binding.addressSlide.animateClose()
        else finish()
    }

    /**
     * 즐겨찾기 유무 확인 후 없으면 AlertDialog
     */
    private fun favoriteAlertDialog(){
        viewModel.getFavorite(address!!).observe(this){
            if(it==null){
                AlertDialog.Builder(this)
                    .setTitle(address!!)
                    .setMessage("즐겨찾기에 추가하시겠습니까?")
                    .setPositiveButton("예") { _, _ ->
                        viewModel.insertFavorite(
                            Favorite(
                                name = address!!,
                                latitude = location.latitude,
                                longitude = location.longitude
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

    data class Loc(
        val latitude:Double,
        val longitude:Double
        )
}