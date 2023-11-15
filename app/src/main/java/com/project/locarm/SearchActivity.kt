package com.project.locarm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.widget.SearchView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.project.locarm.databinding.ActivitySearchBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.project.locarm.GeoCoder

class SearchActivity : AppCompatActivity(), OnMapReadyCallback {
    lateinit var binding : ActivitySearchBinding
    private lateinit var mapView : MapView
    private lateinit var adapter: AddressAdapter
    private val retrofit2 = RetrofitManager.getRetrofitInstance().create(ApiService::class.java)
    private lateinit var address : AddressDTO.Result.Juso
    private val viewModel = MapViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AddressAdapter()
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
        adapter.setOnItemClickListener(object : AddressAdapter.OnItemClickListener{
            override fun onItemClicked(data: AddressDTO.Result.Juso) {
                address = data
                viewModel.setData(data)
                binding.addressSlide.animateClose()
            }

        })

        /**
         * GoogleMap
         */
        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this@SearchActivity)

        /**
         * 등록
         */
        binding.button.setOnClickListener {
            intent.apply {
                putExtra("name", address.name)
                putExtra("address", address.jibunAddr)
                setResult(RESULT_OK, intent)
            }
            finish()
        }
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