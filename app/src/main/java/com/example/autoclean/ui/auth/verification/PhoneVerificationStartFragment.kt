package com.example.autoclean.ui.auth.verification

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.autoclean.R
import com.example.autoclean.databinding.FragmentPhoneVerificationStartBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class PhoneVerificationStartFragment : Fragment() {
    private var _binding: FragmentPhoneVerificationStartBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhoneVerificationStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        setupCallbacks()

        initListeners()
    }

    private fun initListeners() {
        binding.btnContinue.setOnClickListener {
            val phoneNumber = binding.etPhoneNumber.text.toString()
            if (phoneNumber.isNotBlank()) {
                sendVerificationCode(phoneNumber)
            } else {
                Toast.makeText(requireContext(), "Please enter a valid phone number.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupCallbacks() {
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                Log.d("PhoneAuth", "onVerificationCompleted:${credential}")
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w("PhoneAuth", "onVerificationFailed", e)
                Toast.makeText(requireContext(), "Verification failed.", Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                val action = PhoneVerificationStartFragmentDirections
                    .actionPhoneVerificationStartFragmentToCodeVerificationFragment(verificationId)
                findNavController().navigate(action)
            }
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
