package com.example.autoclean.ui.payment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.autoclean.R
import com.example.autoclean.databinding.FragmentCardPaymentBinding
import com.example.autoclean.databinding.FragmentPixPaymentBinding


class PixPaymentFragment : Fragment() {

    private var _binding: FragmentPixPaymentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPixPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}