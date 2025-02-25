package com.example.autoclean.ui.auth.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.autoclean.R
import com.example.autoclean.databinding.FragmentProfileBinding


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

        val args = ProfileFragmentArgs.fromBundle(requireArguments())
        val displayName = args.displayName
        val email = args.email
        val uid = args.uid
        val photoUrl = args.photoUrl
        val skipRegistration = args.skipRegistration


        if (skipRegistration) {
            binding.btnClient.setOnClickListener {
                navigateToPhoneVerification("user", displayName, email, uid, photoUrl)
            }
            binding.btnProfissional.setOnClickListener {
                navigateToPhoneVerification("pro", displayName, email, uid, photoUrl)
            }
        } else {
            initListeners()
        }
    }

    private fun initListeners() {
        binding.btnClient.setOnClickListener {
            navigateToRegister("user")
        }
        binding.btnProfissional.setOnClickListener {
            navigateToRegister("pro")
        }
    }

    private fun navigateToPhoneVerification(
        role: String,
        displayName: String,
        email: String,
        uid: String,
        photoUrl: String?
    ) {
        val safePhotoUrl = photoUrl ?: " "
        val action =
            ProfileFragmentDirections.actionProfileFragmentToPhoneVerificationStartFragment(
                role = role,
                displayName = displayName,
                email = email,
                uid = uid,
                password = "",
                photoUrl = safePhotoUrl
            )
        findNavController().navigate(action)
    }

    private fun navigateToRegister(role: String) {
        val action = ProfileFragmentDirections.actionProfileFragmentToRegisterFragment(role)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}