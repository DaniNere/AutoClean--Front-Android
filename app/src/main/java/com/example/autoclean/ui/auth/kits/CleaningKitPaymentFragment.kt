package com.example.autoclean.ui.auth.kits

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.autoclean.R
import com.example.autoclean.databinding.FragmentCleaningKitPaymentBinding


class CleaningKitPaymentFragment : Fragment() {

    private var _binding: FragmentCleaningKitPaymentBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCleaningKitPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()
    }

    private fun initListeners(){

        binding.btnCreditCard.setOnClickListener{
            findNavController().navigate(R.id.action_cleaningKitPaymentFragment_to_cardPaymentFragment)
        }

        binding.btnPix.setOnClickListener{
            findNavController().navigate(R.id.action_cleaningKitPaymentFragment_to_pixPaymentFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}