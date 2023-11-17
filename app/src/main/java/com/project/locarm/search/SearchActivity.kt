package com.project.locarm.search

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.project.locarm.ApiService
import com.project.locarm.RetrofitManager
import com.project.locarm.databinding.ActivitySearchBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.project.locarm.common.GeoCoder
import com.project.locarm.data.AddressDTO
import com.project.locarm.room.DataBase
import com.project.locarm.room.Favorite
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class SearchActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var binding : ActivitySearchBinding
    private lateinit var mapView : MapView
    private lateinit var adapter: AddressAdapter
    private val retrofit2 = RetrofitManager.getRetrofitInstance().create(ApiService::class.java)
    private lateinit var address : AddressDTO.Result.Juso
    private lateinit var viewModel:SearchViewModel

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
                address = data
                viewModel.setData(data)
                binding.addressSlide.animateClose()
            }
        })

        /**
         * 등록
         */
        binding.button.setOnClickListener {
            intent.apply {
                putExtra("name", address.name)
                putExtra("address", address.jibunAddr)
                setResult(RESULT_OK, intent)
            }

            /**
             * 즐겨찾기 유무 확인 후 없으면 AlertDialog
             */
            viewModel.getFavorite(address.name).observe(this){
                if(it==null){
                    AlertDialog.Builder(this)
                        .setTitle(address.name)
                        .setMessage("즐겨찾기에 추가하시겠습니까?")
                        .setPositiveButton("예") { _, _ ->
                            viewModel.insertFavorite(
                                Favorite(
                                    name = address.name,
                                    roadAddress = address.roadAddr,
                                    jibunAddress = address.jibunAddr
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

        /**
         * GoogleMap
         */
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this@SearchActivity)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        var marker = LatLng(37.5562, 126.9724)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 13f))

        viewModel.address.observe(this){
            val data = GeoCoder.getXY(this, it.jibunAddr)
            marker = LatLng(data.latitude, data.longitude)
            googleMap.addMarker(MarkerOptions().position(marker).title(it.name))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 13f))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(binding.addressSlide.isOpened) binding.addressSlide.animateClose()
        else finish()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }
    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }
}