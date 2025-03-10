package com.example.autoclean.ui.auth.verification

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.autoclean.databinding.FragmentCodeVerificationBinding
import com.example.autoclean.R
import com.example.autoclean.data.api.ApiClient
import com.example.autoclean.data.model.dto.CreateAccountDto
import com.example.autoclean.data.model.request.VerifyCodeTwilio
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CodeVerificationFragment : Fragment() {

    private var _binding: FragmentCodeVerificationBinding? = null
    private val binding get()= _binding!!

    private val args: CodeVerificationFragmentArgs by navArgs()


    private val verificationViewModel: VerificationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCodeVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("CodeVerificationFragment", "onViewCreated: Fragment view created")


        verificationViewModel.updatePhoneNumber(args.phoneNumber)
        verificationViewModel.updateRole(args.role)
        verificationViewModel.updateDisplayName(args.displayName)
        verificationViewModel.updateEmail(args.email)
        verificationViewModel.updateUid(args.uid)
        verificationViewModel.updatePhotoUrl(args.photoUrl)
        verificationViewModel.updatePassword(args.password)


        verificationViewModel.phoneNumber.observe(viewLifecycleOwner, { displayName ->
            Log.d("CodeVerificationFragment", "Observando mudança no número de telefone: $displayName")
        })

        binding.noCode.setOnClickListener {
            val modalFragment = ModalCodeVerificationFragment ()
            modalFragment.show(parentFragmentManager, modalFragment.tag)
        }

        initListeners()
    }

    private fun initListeners() {
        binding.btnContinue.setOnClickListener {
            Log.d("CodeVerificationFragment", "Continue button clicked")

            val role = verificationViewModel.role.value
            if (role == "pro") {
                Log.d("CodeVerificationFragment", "Navigating as professional role")

                val action = CodeVerificationFragmentDirections
                    .actionCodeVerificationFragmentToProfileProfissionalFragment(
                        phoneNumber = verificationViewModel.phoneNumber.value!!,
                        role = verificationViewModel.role.value!!,
                        displayName = verificationViewModel.displayName.value!!,
                        email = verificationViewModel.email.value!!,
                        uid = verificationViewModel.uid.value!!,
                        photoUrl = verificationViewModel.photoUrl.value!!,
                        password = verificationViewModel.password.value!!
                    )
                findNavController().navigate(action)
            } else {
                Log.d("CodeVerificationFragment", "Role is not 'pro', staying in current fragment")
                saveUserData()
            }
        }
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
                        if (verificationViewModel.role.value == "user") {
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
            fullname = verificationViewModel.displayName.value ?: "",
            email = verificationViewModel.email.value ?: "",
            password = "",
            role = verificationViewModel.role.value ?: "",
            firebaseUid =  verificationViewModel.uid.value ?: "",
            phone = verificationViewModel.phoneNumber.value ?: "",
            photoUrl = verificationViewModel.photoUrl.value ?: ""
        )

        Log.d("SaveUserData", "Sending user data to save: $registrationData")

        ApiClient.apiService.register(registrationData).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("SaveUserData", "User data saved successfully!")
                    Toast.makeText(
                        requireContext(),
                        "Dados do usuário salvos com sucesso!",
                        Toast.LENGTH_SHORT
                    ).show()
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