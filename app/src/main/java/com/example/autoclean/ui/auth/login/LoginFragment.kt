package com.example.autoclean.ui.auth.login

import android.app.Activity.RESULT_OK
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.example.autoclean.R
import com.example.autoclean.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        setupGoogleSignIn()

        resetFirstLoginFlag()

        logSharedPreferences()


    initListeners()
}

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
    }

    private fun initListeners() {
        binding.clickableText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_profileFragment)
        }

        binding.clickableText2.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_recoverAccountFragment)
        }

        binding.btnGoogle.setOnClickListener {
            Log.d("GoogleLogin", "Botão de login do Google clicado.")
            signIn()
        }
    }

    private fun signIn() {
        val intent = googleSignInClient.signInIntent
        openActivity.launch(intent)
    }

    private var openActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        val resultCode = result.resultCode
        Log.d("GoogleLogin", "Resultado da Intent de login do Google: $resultCode")

        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            handleSignInResult(task)
            Log.d("GoogleLogin", "Activity Result processada.")
        } else {
            Log.e("GoogleLogin", "Falha ao receber 'RESULT_OK'. Código result: $resultCode")
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken

            if (idToken != null) {
                Log.d("GoogleLogin", "Token ID obtido: $idToken")
                loginWithGoogle(idToken)
            } else {
                Log.d("GoogleLogin", "Token ID não foi retornado.")
                Toast.makeText(requireContext(), "Token de autenticação não obtido.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ApiException) {
            Log.e("GoogleLogin", "Falha no login: ${e.message}")
            Toast.makeText(requireContext(), "Falha no login: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d("GoogleLogin", "Autenticação com Google efetuada com sucesso.")
                    Toast.makeText(requireContext(), "Autenticação Efetuada com o Google", Toast.LENGTH_SHORT).show()

                    if (isFirstLogin()) {
                        navigateToProfile(true)
                    } else {
                        navigateToHome()
                    }
                } else {
                    Log.e("GoogleLogin", "Erro de Autenticação com o Google")
                    Toast.makeText(requireContext(), "Erro de Autenticação com o Google", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun isFirstLogin(): Boolean {
        val prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isFirst = prefs.getBoolean("isFirstLogin", true)
        Log.d("GoogleLogin", "Verificando primeira execução: $isFirst")
        return isFirst
    }

    private fun setFirstLogin(isFirst: Boolean) {
        val prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putBoolean("isFirstLogin", isFirst)
            apply()
        }
    }

    private fun navigateToProfile(skipRegistration: Boolean) {
        Log.d("GoogleLogin", "Navegando para Profile. Skip Registration: $skipRegistration")
        val action = LoginFragmentDirections
            .actionLoginFragmentToProfileFragment(skipRegistration)
        findNavController().navigate(action)
    }

    private fun logSharedPreferences() {
        val prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val allEntries = prefs.all
        for ((key, value) in allEntries) {
            Log.d("GoogleLogin", "Chave: $key, Valor: $value")
        }
    }

    private fun resetFirstLoginFlag() {
        val prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            clear()
            Log.d("GoogleLogin", "SharedPreferences resetado.")
            apply()
        }
    }

    private fun navigateToHome() {
        // Coloque o código necessário para navegar para a tela principal ou inicial
        findNavController().navigate(R.id.action_loginFragment_to_userHomeFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}