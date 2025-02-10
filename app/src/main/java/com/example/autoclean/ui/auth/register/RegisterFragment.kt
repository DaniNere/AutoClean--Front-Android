package com.example.autoclean.ui.auth.register

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.autoclean.R
import com.example.autoclean.data.api.ApiClient
import com.example.autoclean.data.model.CreateAccountDto
import com.example.autoclean.databinding.FragmentRegisterBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    // Use navArgs para obter argumentos passados
    private val args: RegisterFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()
    }

    private fun initListeners() {
        binding.btnCreateAccount.setOnClickListener {
            registerUser()
        }

        binding.clickableText.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun registerUser() {
        val fullName = binding.editTextName.text.toString()
        val email = binding.editTextEmail.text.toString()
        val password = binding.editTextPassword.text.toString()


        val role = args.userRole

        if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
            Toast.makeText(context, "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show()
            return
        }

        val apiService = ApiClient.apiService
        val registrationData = CreateAccountDto(
            name = fullName,
            email = email,
            password = password,
            role = role
        )

        println(registrationData)


        apiService.register(registrationData).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Registro efetuado com sucesso!", Toast.LENGTH_SHORT).show()
                    navigateToPhoneVerification()
                } else {
                    Toast.makeText(context, "Falha no registro: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("RegisterFragment", "Erro na rede durante o registro", t)
                Toast.makeText(context, "Erro no registro: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToPhoneVerification() {
        findNavController().navigate(R.id.action_registerFragment_to_phoneVerificationStartFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}