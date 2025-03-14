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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.autoclean.R
import com.example.autoclean.data.api.ApiClient
import com.example.autoclean.data.model.dto.LoginDto
import com.example.autoclean.data.model.response.LoginResponse
import com.example.autoclean.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

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

        binding.btnEntrar.setOnClickListener {
            val email = binding.editTextEmail.text.toString()
            val password = binding.editTextPassword.text.toString()
            performLogin(email, password)
        }

    }

    private fun performLogin(email: String, password: String) {
        Log.d("Login", "Tentando fazer login com email: $email")

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.login(LoginDto(email, password))
                Log.d("Login", "Resposta recebida: ${response.raw()}")

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    Log.d(
                        "Login",
                        "Login bem-sucedido para usuário: ${loginResponse.user.fullname}"
                    )
                    handleSuccessfulLogin(loginResponse)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(
                        "Login",
                        "Erro no login: Código: ${response.code()}, Mensagem: ${response.message()}, Corpo do erro: $errorBody"
                    )
                    Toast.makeText(
                        requireContext(),
                        "Falha no login: ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("Login", "Exceção durante o login: ${e.message}", e)
                Toast.makeText(requireContext(), "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleSuccessfulLogin(loginResponse: LoginResponse) {
        val accessToken = loginResponse.accessToken
        val refreshToken = loginResponse.refreshToken
        val user = loginResponse.user
        val role = loginResponse.user.role

        Log.d("Login", "Token de acesso recebido: $accessToken")
        Log.d("Login", "Token de atualização recebido: $refreshToken")
        Log.d("Login", "Role: ${user.role}")

        val sharedPrefs = requireContext().getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putString("accessToken", accessToken)
            putString("refreshToken", refreshToken)
            putString("userRole", role)
            apply()
        }

        Toast.makeText(
            requireContext(),
            "Bem-vindo(a), ${user.fullname}!",
            Toast.LENGTH_SHORT
        ).show()

        when (role) {
            "user" -> {
                val action = LoginFragmentDirections.actionLoginFragmentToUserHomeFragment(
                    skipRegistration = false,
                    displayName = user.fullname,
                    email = user.email,
                    uid = user.id.toString(),
                    photoUrl = user.profilePicture ?: "",

                    )
                findNavController().navigate(action)

            }


            "pro" -> {
                val action = LoginFragmentDirections.actionLoginFragmentToProHomeFragment(
                    skipRegistration = false,
                    displayName = user.fullname,
                    email = user.email,
                    uid = user.id.toString(),
                    photoUrl = user.profilePicture ?: "",
                )
                findNavController().navigate(action)
            }

            else -> {
                Toast.makeText(requireContext(), "Role não reconhecido!", Toast.LENGTH_SHORT).show()
            }
        }
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
            val action = LoginFragmentDirections.actionLoginFragmentToProfileFragment(
                skipRegistration = false,
                displayName = "",
                email = "",
                uid = "",
                photoUrl = ""
            )
            findNavController().navigate(action)
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

                val displayName = account.displayName
                val email = account.email
                val uid = account.id
                val photoUrl = account.photoUrl?.toString()

            } else {
                Log.d("GoogleLogin", "Token ID não foi retornado.")
                Toast.makeText(
                    requireContext(),
                    "Token de autenticação não obtido.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: ApiException) {
            Log.e("GoogleLogin", "Falha no login: ${e.message}")
            Toast.makeText(requireContext(), "Falha no login: ${e.message}", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun loginWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d("GoogleLogin", "Autenticação com Google efetuada com sucesso.")
                    Toast.makeText(
                        requireContext(),
                        "Autenticação Efetuada com o Google",
                        Toast.LENGTH_SHORT
                    ).show()

                    val user = auth.currentUser
                    val displayName = user?.displayName
                    val email = user?.email
                    val uid = user?.uid
                    val photoUrl = user?.photoUrl?.toString()

                    if (isFirstLogin()) {
                        navigateToProfile(displayName, email, uid, photoUrl, true)
                    } else {
                        navigateToHome()
                    }
                } else {
                    Log.e("GoogleLogin", "Erro de Autenticação com o Google")
                    Toast.makeText(
                        requireContext(),
                        "Erro de Autenticação com o Google",
                        Toast.LENGTH_SHORT
                    ).show()
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

    private fun navigateToProfile(
        displayName: String?,
        email: String?,
        uid: String?,
        photoUrl: String?,
        skipRegistration: Boolean
    ) {
        val action = LoginFragmentDirections.actionLoginFragmentToProfileFragment(
            skipRegistration = skipRegistration,
            displayName = displayName ?: "",
            email = email ?: "",
            uid = uid ?: "",
            photoUrl = photoUrl ?: ""
        )
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
        findNavController().navigate(R.id.action_loginFragment_to_userHomeFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}