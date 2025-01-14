package com.example.autoclean.ui.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.autoclean.R
import com.example.autoclean.databinding.FragmentLoginBinding
import com.example.autoclean.databinding.FragmentProfileBinding
import com.google.android.material.button.MaterialButton


class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()
    }

    private fun initListeners() {

        val navigateToRegister: (View) -> Unit = {
            findNavController().navigate(R.id.action_profileFragment_to_registerFragment)
        }
        binding.btnClient.setOnClickListener(navigateToRegister)
        binding.btnProfissional.setOnClickListener(navigateToRegister)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}