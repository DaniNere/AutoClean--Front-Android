package com.example.autoclean.ui.auth.kits

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.autoclean.R
import com.example.autoclean.databinding.FragmentValidationKitBinding


class ValidationKitFragment : Fragment() {
    private var _binding: FragmentValidationKitBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_validation_kit, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}