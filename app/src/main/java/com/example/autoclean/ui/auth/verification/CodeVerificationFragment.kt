package com.example.autoclean.ui.auth.verification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.autoclean.R
import com.example.autoclean.data.api.ApiClient
import com.example.autoclean.data.model.request.VerifyCodeTwilio
import com.example.autoclean.databinding.FragmentCodeVerificationBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CodeVerificationFragment : Fragment() {

    private var _binding: FragmentCodeVerificationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCodeVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: CodeVerificationFragmentArgs by navArgs()
        val phoneNumber = args.phoneNumber

        initListeners(phoneNumber)
    }

    private fun initListeners(phoneNumber: String) {
        binding.btnContinue.setOnClickListener {
            val code = getEnteredCode()
            if (code.length == 4) {
                verifyCode(phoneNumber, code)
            } else {
                showToast("Por favor, insira um código de 4 dígitos.")
            }
        }
    }

    private fun getEnteredCode(): String {
        return binding.codAccess1.text.toString() +
                binding.codAccess2.text.toString() +
                binding.codAccess3.text.toString() +
                binding.codAccess4.text.toString()
    }

    private fun verifyCode(phoneNumber: String, code: String) {
        val verificationRequest = VerifyCodeTwilio(phoneNumber, code)

        ApiClient.twilioService.validateVerificationCode(verificationRequest)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        findNavController().navigate(R.id.action_codeVerificationFragment_to_profileProfissionalFragment)
                        showToast("Código de verificação validado com sucesso!")
                    } else {
                        showToast("Código de verificação inválido.")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    showToast("Erro ao validar o código: ${t.message}")
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