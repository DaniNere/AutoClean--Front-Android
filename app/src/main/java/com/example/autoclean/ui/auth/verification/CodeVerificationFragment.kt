package com.example.autoclean.ui.auth.verification

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.autoclean.R
import com.example.autoclean.databinding.FragmentCodeVerificationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider


class CodeVerificationFragment : Fragment() {
    private var _binding: FragmentCodeVerificationBinding? = null
    private val binding get() = _binding!!
    private lateinit var verificationId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCodeVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            verificationId = it.getString("verificationId") ?: ""
        }


        initListeners()
    }

    private fun initListeners() {
        binding.btnContinue.setOnClickListener {
            val code = getEnteredCode()

            if (code.length == 4) {
                verifyCode(code)
            } else {
                showToast("Please enter a 4-digit code.")
            }
        }
    }

    private fun getEnteredCode(): String {
        return binding.codAccess1.text.toString() +
                binding.codAccess2.text.toString() +
                binding.codAccess3.text.toString() +
                binding.codAccess4.text.toString()
    }

    private fun verifyCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    findNavController().navigate(R.id.action_codeVerificationFragment_to_profileProfissionalFragment)
                } else {
                    showToast("Invalid verification code.")
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}