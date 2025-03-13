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
import com.example.autoclean.data.model.response.UserResponse
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

        Log.d("Login", "Token de acesso recebido: $accessToken")
        Log.d("Login", "Token de atualização recebido: $refreshToken")
        Log.d("Login", "Usuário logado: ${user.fullname}")

        val sharedPrefs = requireContext().getSharedPreferences("AuthPrefs", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            putString("accessToken", accessToken)
            putString("refreshToken", refreshToken)
            apply()
        }

        Toast.makeText(
            requireContext(),
            "Bem-vindo(a), ${user.fullname}!",
            Toast.LENGTH_SHORT
        ).show()

        val action = LoginFragmentDirections.actionLoginFragmentToUserHomeFragment(
            displayName = user.fullname,
            email = user.email,
            uid = user.id.toString(),
            photoUrl = user.profilePicture ?: ""
        )
        findNavController().navigate(action)
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

                    val user = auth.currentUser
                    val displayName = user?.displayName
                    val email = user?.email
                    val uid = user?.uid
                    val photoUrl = user?.photoUrl?.toString()

                    // Verificar se o usuário já está registrado no banco de dados
                    verifyUserInDatabase(uid, displayName, email, photoUrl)
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

    private fun verifyUserInDatabase(
        uid: String?,
        displayName: String?,
        email: String?,
        photoUrl: String?
    ) {
        Log.d("DatabaseVerification", "Iniciando verificação do usuário no banco de dados.")

        if (uid == null) {
            Log.e("DatabaseVerification", "UID é nulo. Não é possível continuar a verificação.")
            return
        }

        // Logar o valor do UID antes de fazer a chamada da API
        Log.d("DatabaseVerification", "Verificando UID: $uid")

        ApiClient.apiService.checkUserExists(uid).enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val userExists = response.body()?.exists ?: false
                    Log.d("DatabaseVerification", "Requisição sucedida. Usuário existe: $userExists")

                    if (userExists) {
                        Log.d("GoogleLogin", "Usuário já registrado.")

                        navigateToHome(displayName, email, uid, photoUrl)
                    }  else {
                        // Novo usuário
                        Log.d("GoogleLogin", "Novo usuário, navegando para profile.")
                        navigateToProfile(displayName, email, uid, photoUrl, true)
                    }
                } else {
                    Log.e("DatabaseVerification", "Requisição falhou com o código: ${response.code()}, mensagem: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("GoogleLogin", "Falha na verificação do banco de dados: ${t.message}")
                Toast.makeText(
                    requireContext(),
                    "Erro na verificação do usuário.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

// Métodos separados para navegação

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

    private fun navigateToHome(
        displayName: String?,
        email: String?,
        uid: String?,
        photoUrl: String?,
    ) {
        val action = LoginFragmentDirections.actionLoginFragmentToUserHomeFragment(
            displayName = displayName ?: "",
            email = email ?: "",
            uid = uid ?: "",
            photoUrl = photoUrl ?: ""
        )

        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}