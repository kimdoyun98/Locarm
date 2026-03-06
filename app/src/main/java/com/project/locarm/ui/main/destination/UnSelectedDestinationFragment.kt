package com.project.locarm.ui.main.destination

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.project.locarm.databinding.FragmentUnSelectedDestinationBinding
import com.project.locarm.ui.main.MainActivity


class UnSelectedDestinationFragment : Fragment() {
    private var _binding: FragmentUnSelectedDestinationBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchDestinationButton.setOnClickListener {
            (activity as MainActivity).navigateToSearchDestination()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentUnSelectedDestinationBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
