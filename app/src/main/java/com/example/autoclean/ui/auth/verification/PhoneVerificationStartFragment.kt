package com.example.autoclean.ui.auth.verification

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.autoclean.R
import com.example.autoclean.data.api.ApiClient
import com.example.autoclean.data.model.request.VerifyRequestDto
import com.example.autoclean.databinding.FragmentPhoneVerificationStartBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.i18n.phonenumbers.PhoneNumberUtil

class PhoneVerificationStartFragment : Fragment() {
    private var _binding: FragmentPhoneVerificationStartBinding? = null
    private val binding get() = _binding!!
    private val TAG = "PhoneVerification"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhoneVerificationStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {
        binding.btnContinue.setOnClickListener {
            val countryCode = binding.ccp.selectedCountryCodeWithPlus ?: "+"
            val phoneNumber = binding.etPhoneNumber.text.toString()

            val fullPhoneNumber = "$countryCode$phoneNumber"
            if (phoneNumber.isNotBlank()) {
                if (isValidPhoneNumber(fullPhoneNumber)) {
                    Log.d(TAG, "Número de telefone completo: $fullPhoneNumber")
                    sendVerificationRequestToBackend(fullPhoneNumber)
                } else {
                    showToast("Por favor, insira um número de telefone válido.")
                }
            } else {
                showToast("Por favor, insira um número de telefone.")
            }
        }
    }

    private fun isValidPhoneNumber(fullPhoneNumber: String): Boolean {
        val phoneUtil = PhoneNumberUtil.getInstance()
        return try {
            val numberProto = phoneUtil.parse(fullPhoneNumber, null)
            phoneUtil.isValidNumber(numberProto)
        } catch (e: Exception) {
            Log.e(TAG, "Número inválido: ", e)
            false
        }
    }

    private fun sendVerificationRequestToBackend(fullPhoneNumber: String) {
        Log.d(TAG, "Enviando requisição para verificação do número de telefone.")
        val verifyRequest = VerifyRequestDto(phoneNumber = fullPhoneNumber)

        ApiClient.twilioService.sendPhoneVerification(verifyRequest)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "Solicitação de verificação enviada com sucesso.")
                        showToast("Código de verificação enviado!")

                        val action = PhoneVerificationStartFragmentDirections
                            .actionPhoneVerificationStartFragmentToCodeVerificationFragment(
                                fullPhoneNumber
                            )
                        findNavController().navigate(action)
                    } else {
                        Log.e(TAG, "Erro na solicitação de verificação: ${response.code()}")
                        showToast("Erro no envio do código de verificação.")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e(TAG, "Falha de rede ou servidor: ", t)
                    showToast("Erro de rede ou servidor ao solicitar verificação.")
                }
            })
    }

    private fun showToast(message: String) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}