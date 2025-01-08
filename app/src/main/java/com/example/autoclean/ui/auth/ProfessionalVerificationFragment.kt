package com.example.autoclean.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.autoclean.R
import com.example.autoclean.databinding.FragmentProfessionalVerificationBinding

class ProfessionalVerificationFragment : Fragment() {
    private var _binding: FragmentProfessionalVerificationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfessionalVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()
        updateUserName("Carlos")
    }

    private fun initListeners() {
        TODO()
    }

    private fun updateUserName(userName: String) {
        binding.nameProfile.text = "Olá, $userName"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
