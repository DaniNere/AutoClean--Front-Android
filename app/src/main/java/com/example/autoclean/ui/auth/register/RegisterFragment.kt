package com.example.autoclean.ui.auth.register

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
import androidx.navigation.fragment.navArgs
import com.example.autoclean.R

import com.example.autoclean.databinding.FragmentRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val args: RegisterFragmentArgs by navArgs()
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val openActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        handleActivityResult(result)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setupGoogleSignIn()
        initListeners()
    }

    private fun initListeners() {
        binding.btnCreateAccount.setOnClickListener { createAccount() }
        binding.clickableText.setOnClickListener { navigateToLogin() }
        binding.btnGoogle.setOnClickListener {
            Log.d("GoogleLogin", "Botão de login do Google clicado.")
            signIn()
        }
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
    }

    private fun createAccount() {
        val fullName = binding.editTextName.text.toString()
        val email = binding.editTextEmail.text.toString()
        val password = binding.editTextPassword.text.toString()
        val role = args.role
        navigateToPhoneVerification(fullName, email, password, role)
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
    }

    private fun navigateToPhoneVerification(fullName: String, email: String, password: String, role: String) {
        val action = RegisterFragmentDirections.actionRegisterFragmentToPhoneVerificationStartFragment(
            displayName = fullName, email = email, password = password, role = role, uid= "", photoUrl = ""
        )
        findNavController().navigate(action)
    }

    private fun signIn() {
        val intent = googleSignInClient.signInIntent
        openActivity.launch(intent)
    }

    private fun handleActivityResult(result: ActivityResult) {
        val resultCode = result.resultCode
        Log.d("GoogleLogin", "Resultado da Intent de login do Google: $resultCode")
        if (resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        } else {
            Log.e("GoogleLogin", "Falha ao receber 'RESULT_OK'. Código result: $resultCode")
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { idToken ->
                Log.d("GoogleLogin", "Token ID obtido: $idToken")
                loginWithGoogle(idToken)
            } ?: run {
                showToast("Token de autenticação não obtido.")
            }
        } catch (e: ApiException) {
            Log.e("GoogleLogin", "Falha no login: ${e.message}")
            showToast("Falha no login: ${e.message}")
        }
    }

    private fun loginWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(requireActivity()) { task ->
            if (task.isSuccessful) {
                Log.d("GoogleLogin", "Autenticação com Google efetuada com sucesso.")
                showToast("Autenticação Efetuada com o Google")
                handleSuccessfulLogin()
            } else {
                Log.e("GoogleLogin", "Erro de Autenticação com o Google")
                showToast("Erro de Autenticação com o Google")
            }
        }
    }

    private fun handleSuccessfulLogin(){
        val user = auth.currentUser
        val displayName = user?.displayName ?: ""
        val email = user?.email ?: ""
        val uid = user?.uid ?: ""
        val photoUrl = user?.photoUrl?.toString()
        val password = ""

        Log.d("LoginProcess", "Successful login with: displayName=$displayName, email=$email, uid=$uid, photoUrl=$photoUrl")
        if (isFirstLogin()) {
            val role = args.role
            Log.d("LoginProcess", "First login detected, navigating to profile with role: $role")
            navigateToProfile(displayName, email, password, role, uid, photoUrl)
        } else {
            navigateToHome()
        }
    }

    private fun navigateToProfile(
        displayName: String?,
        email: String?,
        password: String,
        role: String,
        uid: String?,
        photoUrl: String?
    ) {
        Log.d("Navigation", "Navigating to Profile with: displayName=$displayName, email=$email, password=$password, role=$role, uid=$uid, photoUrl=$photoUrl")
        val action = RegisterFragmentDirections.actionRegisterFragmentToPhoneVerificationStartFragment(
            displayName = displayName ?: "",
            email = email ?: "",
            password = password,
            role = role,
            uid = uid ?: "",
            photoUrl = photoUrl ?: ""
        )
        findNavController().navigate(action)
    }

    private fun isFirstLogin(): Boolean {
        val prefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isFirst = prefs.getBoolean("isFirstLogin", true)
        Log.d("GoogleLogin", "Verificando primeira execução: $isFirst")
        return isFirst
    }

    private fun navigateToHome() {
        findNavController().navigate(R.id.action_loginFragment_to_userHomeFragment)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}