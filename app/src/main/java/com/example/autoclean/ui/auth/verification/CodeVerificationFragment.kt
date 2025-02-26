package com.example.autoclean.ui.auth.verification

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.autoclean.R
import com.example.autoclean.data.api.ApiClient
import com.example.autoclean.data.model.model.CreateAccountDto
import com.example.autoclean.data.model.request.VerifyCodeTwilio
import com.example.autoclean.databinding.FragmentCodeVerificationBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CodeVerificationFragment : Fragment() {

    private var _binding: FragmentCodeVerificationBinding? = null
    private val binding get() = _binding!!

    private val args: CodeVerificationFragmentArgs by navArgs()

    private lateinit var phoneNumber: String
    private lateinit var role: String
    private lateinit var displayName: String
    private lateinit var email: String
    private lateinit var uid: String
    private lateinit var photoUrl: String
    private lateinit var password: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCodeVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("CodeVerificationFragment", "onViewCreated: Fragment view created")

        phoneNumber = args.phoneNumber
        role = args.role
        displayName = args.displayName
        email = args.email
        uid = args.uid
        photoUrl = args.photoUrl
        password = args.password

        Log.d("CodeVerificationFragment", "onViewCreated: Args initialized - phoneNumber: $phoneNumber, role: $role, displayName: $displayName, email: $email")

        initListeners()
    }

    // Este trecho é só pra não gastar os créditos de envio de SMS
    private fun initListeners() {
        binding.btnContinue.setOnClickListener {
            Log.d("CodeVerificationFragment", "Continue button clicked")

            if (role == "pro") {
                Log.d("CodeVerificationFragment", "Navigating as professional role")

                val action = CodeVerificationFragmentDirections
                    .actionCodeVerificationFragmentToProfileProfissionalFragment(
                        phoneNumber = phoneNumber,
                        role = role,
                        displayName = displayName,
                        email = email,
                        uid = uid,
                        photoUrl = photoUrl,
                        password = password
                    )
                findNavController().navigate(action)
            } else {
                Log.d("CodeVerificationFragment", "Role is not 'pro', staying in current fragment")
                saveUserData()
            }
        }
    }

    /*private fun initListeners(phoneNumber: String) {
        binding.btnContinue.setOnClickListener {
            val code = getEnteredCode()
            if (code.length == 4) {
                verifyCode(phoneNumber, code)
            } else {
                showToast("Por favor, insira um código de 4 dígitos.")
            }
        }
    }*/

    private fun getEnteredCode(): String {
        return binding.codAccess1.text.toString() +
                binding.codAccess2.text.toString() +
                binding.codAccess3.text.toString() +
                binding.codAccess4.text.toString()
    }

    private fun verifyCode(phoneNumber: String, code: String) {
        Log.d("CodeVerificationFragment", "verifyCode: Verifying code for phoneNumber: $phoneNumber with code: $code")

        val verificationRequest = VerifyCodeTwilio(phoneNumber, code)

        ApiClient.twilioService.validateVerificationCode(verificationRequest)
            .enqueue(object : Callback<Void> {

                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d("CodeVerificationFragment", "Code verified successfully")
                        showToast("Código de verificação validado com sucesso!")
                        if (role == "user") {
                            saveUserData()
                        }
                        findNavController().navigate(R.id.action_codeVerificationFragment_to_profileProfissionalFragment)
                    } else {
                        Log.e("CodeVerificationFragment", "Failed verification with code: ${response.code()}")
                        showToast("Código de verificação inválido.")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e("CodeVerificationFragment", "Error verifying code: ${t.message}")
                    showToast("Erro ao validar o código: ${t.message}")
                }
            })
    }

    private fun saveUserData() {
        val registrationData = CreateAccountDto(
            fullname = displayName,
            email = email,
            password = "",
            role = role,
            uid = uid,
            phone = phoneNumber,
            photoUrl = photoUrl
        )

        Log.d("SaveUserData", "Sending user data to save: $registrationData")

        ApiClient.apiService.register(registrationData).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("SaveUserData", "User data saved successfully!")
                } else {
                    Log.e("SaveUserData", "Error saving user data: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("SaveUserData", "Error during save call: ${t.message}")
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}