package com.example.autoclean.ui.auth.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.autoclean.data.api.ApiClient
import com.example.autoclean.data.model.dto.CreateAccountDto
import com.example.autoclean.databinding.FragmentProfileProfissionalBinding
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import org.json.JSONException

class ProfileProfissionalFragment : Fragment() {

    private var _binding: FragmentProfileProfissionalBinding? = null
    private val binding get() = _binding!!
    private val TAG = "ProfileProfissional"

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
        _binding = FragmentProfileProfissionalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = ProfileProfissionalFragmentArgs.fromBundle(requireArguments())

        phoneNumber = args.phoneNumber
        role = args.role
        displayName = args.displayName
        email = args.email
        uid = args.uid
        photoUrl = args.photoUrl
        password = args.password

        with(binding) {
            editTextFullName.setText(displayName)
            editTextPhone.setText(phoneNumber)
            editTextFullName.isEnabled = false
            editTextPhone.isEnabled = false
        }

        setupDateMask()
        setupClickableText()

        binding.checkboxTerms.setOnCheckedChangeListener { _, isChecked ->
            binding.btnContinue.isEnabled = isChecked
        }

        binding.btnContinue.setOnClickListener {
            if (!binding.checkboxTerms.isChecked) {
                Toast.makeText(requireContext(), "Você deve aceitar os Termos de Uso.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val birthDate = binding.editTextBirthDate.text.toString()
            if (!isAgeValid(birthDate)) {
                Toast.makeText(requireContext(), "Você deve ter pelo menos 18 anos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cpfOrCnpj = binding.editTextCpfCnpj.text.toString()
            if (!isValidCpfOrCnpj(cpfOrCnpj)) {
                Toast.makeText(requireContext(), "CPF ou CNPJ inválido.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveUserProfile()
        }
    }

    private fun setupDateMask() {
        binding.editTextBirthDate.addTextChangedListener(object : TextWatcher {
            private var current = ""
            private val ddmmyyyy = "DDMMYYYY"

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    // Ignore recursive calls
                    if (s.toString() == current) return

                    // Remove other characters except digits
                    var clean = s.toString().replace("[^\\d]".toRegex(), "")

                    // If user deletes content back to empty, allow it
                    if (clean.isEmpty()) {
                        current = ""
                        return
                    }

                    // Format if there are enough characters for a date
                    if (clean.length <= 8) {
                        val cleanC = "${clean.padEnd(8, ' ')}${ddmmyyyy}".substring(0, 8)
                        clean = "${cleanC.substring(0, 2)}/${cleanC.substring(2, 4)}/${cleanC.substring(4, 8)}".trim()
                    } else {
                        // If there are more than 8 digits, truncate
                        clean = clean.substring(0, 8)
                        val cleanC = "${clean.padEnd(8, ' ')}${ddmmyyyy}".substring(0, 8)
                        clean = "${cleanC.substring(0, 2)}/${cleanC.substring(2, 4)}/${cleanC.substring(4, 8)}".trim()
                    }

                    current = clean
                    binding.editTextBirthDate.removeTextChangedListener(this) // Avoid recursive changes
                    binding.editTextBirthDate.setText(clean)
                    binding.editTextBirthDate.setSelection(clean.length.coerceAtMost(current.count()))
                    binding.editTextBirthDate.addTextChangedListener(this)
                }
            }
        })
    }

    private fun isAgeValid(birthDate: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val date = sdf.parse(birthDate) ?: return false
            val today = Calendar.getInstance()
            val birthCalendar = Calendar.getInstance().apply { time = date }
            var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            return age >= 18
        } catch (e: Exception) {
            false
        }
    }

    private fun isValidCpfOrCnpj(input: String): Boolean {
        // Remove todos os caracteres não numéricos para aceitar tanto CPF quanto CNPJ no formato puro
        val cleanInput = input.replace("[^\\d]".toRegex(), "")

        // Verifica se o CPF não formatado tem 11 dígitos
        if (cleanInput.length == 11) {
            return isCpfValid(cleanInput)
        }

        // Verifica se o CNPJ não formatado tem 14 dígitos
        if (cleanInput.length == 14) {
            return isCnpjValid(cleanInput)
        }

        return false
    }

    private fun isCpfValid(cpf: String): Boolean {
        // Verifica se todos os caracteres são iguais (ex.: 111.111.111-11)
        if (cpf.all { it == cpf[0] }) return false

        // Calcular os dígitos verificadores
        try {
            val numbers = cpf.substring(0, 9).map { it.toString().toInt() }
            val checkers = cpf.substring(9, 11).map { it.toString().toInt() }

            val firstChecker = calculateCpfChecker(numbers, 10)
            val secondChecker = calculateCpfChecker(numbers + firstChecker, 11)

            return checkers[0] == firstChecker && checkers[1] == secondChecker
        } catch (e: Exception) {
            return false
        }
    }

    private fun calculateCpfChecker(numbers: List<Int>, factor: Int): Int {
        val sum = numbers.mapIndexed { index, number -> number * (factor - index) }.sum()
        val result = 11 - (sum % 11)
        return if (result >= 10) 0 else result
    }

    private fun isCnpjValid(cnpj: String): Boolean {
        // Verifica se todos os caracteres são iguais (ex.: 11.111.111/1111-11)
        if (cnpj.all { it == cnpj[0] }) return false

        // Calcular os dígitos verificadores
        try {
            val numbers = cnpj.substring(0, 12).map { it.toString().toInt() }
            val checkers = cnpj.substring(12, 14).map { it.toString().toInt() }

            val firstChecker = calculateCnpjChecker(numbers)
            val secondChecker = calculateCnpjChecker(numbers + firstChecker)

            return checkers[0] == firstChecker && checkers[1] == secondChecker
        } catch (e: Exception) {
            return false
        }
    }

    private fun calculateCnpjChecker(numbers: List<Int>): Int {
        val weight1 = listOf(5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)
        val weight2 = listOf(6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2)

        val weight = if (numbers.size == 12) weight1 else weight2

        val sum = numbers.mapIndexed { index, number -> number * weight[index] }.sum()
        val result = sum % 11
        return if (result < 2) 0 else 11 - result
    }


    private fun setupClickableText() {
        val termsText = "Ao concordar, você aceita os Termos de Uso e a Política de Privacidade."
        val spannableString = SpannableString(termsText)

        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                openPdfOrWeb("https://www.seusite.com/termos_de_uso.pdf")
            }
        }, 26, 40, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                openPdfOrWeb("https://www.seusite.com/politica_de_privacidade.pdf")
            }
        }, 43, 63, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.textTerms.text = spannableString
        binding.textTerms.movementMethod = LinkMovementMethod.getInstance()

    }

    private fun saveUserProfile() {
        val cpfOrCnpj = binding.editTextCpfCnpj.text.toString()
        val birthDate = binding.editTextBirthDate.text.toString()

        val registrationData = CreateAccountDto(
            fullname = displayName.takeIf { it.isNotEmpty() } ?: binding.editTextFullName.text.toString(),
            email = email.takeIf { it.isNotEmpty() } ?: "",
            password = password.takeIf { it.isNotEmpty() } ?: "",
            role = role.takeIf { it.isNotEmpty() } ?: "default_role",
            uid = uid.takeIf { it.isNotEmpty() } ?: "",
            phone = phoneNumber.takeIf { it.isNotEmpty() } ?: binding.editTextPhone.text.toString(),
            photoUrl = photoUrl.takeIf { it.isNotEmpty() } ?: "",
            cpfOrCnpj = cpfOrCnpj,
            birthDate = birthDate
        )

        Log.d(TAG, "Attempting to send user data: $registrationData")

        ApiClient.apiService.register(registrationData).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "User data saved successfully via API!")
                    Toast.makeText(requireContext(), "Informações enviadas com sucesso!", Toast.LENGTH_SHORT).show()

                    val action = ProfileProfissionalFragmentDirections.actionProfileProfissionalFragmentToProfessionalVerificationFragment(
                        displayName = registrationData.fullname
                    )
                    findNavController().navigate(action)

                } else {
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null) {
                        Log.e(TAG, "Erro na resposta de salvamento: Código ${response.code()} - $errorBody")

                        val errorMessage = parseErrorMessage(errorBody)

                        if (errorMessage.isNotEmpty()) {
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Erro inesperado ao processar a requisição.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Erro ao processar a resposta do erro.", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e(TAG, "Erro ao tentar realizar a chamada de salvamento: ${t.message}")
                Toast.makeText(requireContext(), "Falha de rede: não foi possível salvar os dados.", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun parseErrorMessage(errorBodyString: String): String {
        return try {
            val jsonObject = JSONObject(errorBodyString)
            jsonObject.optString("message", "Erro não especificado.").also {
                Log.e(TAG, "Mensagem extraída: $it")
            }
        } catch (e: JSONException) {
            Log.e(TAG, "Erro ao analisar a mensagem de erro: ${e.message}")
            ""
        }
    }
    private fun openPdfOrWeb(url: String) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Não foi possível abrir o documento", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
