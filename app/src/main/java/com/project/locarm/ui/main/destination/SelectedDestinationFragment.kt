package com.project.locarm.ui.main.destination

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.project.locarm.common.MyApplication
import com.project.locarm.databinding.FragmentSelectedDestinationBinding
import com.project.locarm.location.RealTimeLocation
import com.project.locarm.ui.main.MainViewModel

class SelectedDestinationFragment : Fragment() {
    private var _binding: FragmentSelectedDestinationBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()
    private val realTimeLocation = RealTimeLocation(MyApplication.instance)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        setDistanceOfDestination()
    }

    private fun setDistanceOfDestination() {
        viewModel.destination.observe(viewLifecycleOwner) { destination ->
            if (destination != null) {
                realTimeLocation.currentLocation()?.addOnSuccessListener {
                    viewModel.setDistanceRemaining(
                        realTimeLocation.getDistance(
                            it.latitude,
                            it.longitude,
                            destination
                        )
                    )
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSelectedDestinationBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        realTimeLocation.onDestroy()
    }
}
