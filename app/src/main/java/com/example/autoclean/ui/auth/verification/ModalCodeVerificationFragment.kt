package com.example.autoclean.ui.auth.verification

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.autoclean.R
import com.example.autoclean.data.api.ApiClient
import com.example.autoclean.data.model.request.VerifyRequestDto
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ModalCodeVerificationFragment : BottomSheetDialogFragment() {
    private val verificationViewModel: VerificationViewModel by activityViewModels()
    private val LOG_TAG = "CodeVerificationModal"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.modal_code_verification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val closeButton: ImageView = view.findViewById(R.id.close)
        val resendCodeOption: LinearLayout = view.findViewById(R.id.resend_code_option)
        val resendCodeCheckBox: CheckBox = view.findViewById(R.id.resend_code_checkbox)
        val changeNumberOption: LinearLayout = view.findViewById(R.id.change_number_option)
        val changeNumberCheckBox: CheckBox = view.findViewById(R.id.change_number_checkbox)
        val continueButton: View = view.findViewById(R.id.continue_button)

        Log.d(LOG_TAG, "Modal opened")
        Log.d(LOG_TAG, "PhoneNumber in ViewModel: ${verificationViewModel.phoneNumber.value}")
        Log.d(LOG_TAG, "Role in ViewModel: ${verificationViewModel.role.value}")

        closeButton.setOnClickListener {
            Log.d(LOG_TAG, "Close button clicked")
            dismiss()
        }

        resendCodeOption.setOnClickListener {
            if (!resendCodeCheckBox.isChecked) {
                resendCodeCheckBox.isChecked = true
                changeNumberCheckBox.isChecked = false
            }
            Log.d(LOG_TAG, "Resend code option selected: ${resendCodeCheckBox.isChecked}")
        }

        changeNumberOption.setOnClickListener {
            if (!changeNumberCheckBox.isChecked) {
                changeNumberCheckBox.isChecked = true
                resendCodeCheckBox.isChecked = false
            }
            Log.d(LOG_TAG, "Change number option selected: ${changeNumberCheckBox.isChecked}")
        }

        // Listener no nível básico dos CheckBoxes apenas para garantir o comportamento uno
        resendCodeCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                changeNumberCheckBox.isChecked = false
            }
        }

        changeNumberCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                resendCodeCheckBox.isChecked = false
            }
        }

        continueButton.setOnClickListener {
            when {
                resendCodeCheckBox.isChecked -> {
                    Log.d(LOG_TAG, "Resend code selected, initiating resend process")
                    val fullPhoneNumber = verificationViewModel.phoneNumber.value ?: ""
                    if (fullPhoneNumber.isNotEmpty()) {
                        sendVerificationRequestToBackend(fullPhoneNumber)
                    } else {
                        Toast.makeText(context, "Número de telefone inválido.", Toast.LENGTH_SHORT).show()
                        Log.e(LOG_TAG, "Phone number is empty")
                    }
                }
                changeNumberCheckBox.isChecked -> {
                    Log.d(LOG_TAG, "Change number selected, dismissing modal and navigating")
                    dismiss()

                    val action = CodeVerificationFragmentDirections
                        .actionCodeVerificationFragmentToPhoneVerificationStartFragment(
                            role = verificationViewModel.role.value ?: "",
                            displayName = verificationViewModel.displayName.value ?: "",
                            email = verificationViewModel.email.value ?: "",
                            uid = verificationViewModel.uid.value ?: "",
                            photoUrl = verificationViewModel.photoUrl.value ?: "",
                            password = verificationViewModel.password.value ?: ""
                        )
                    parentFragment?.findNavController()?.navigate(action)
                }
                else -> {
                    Log.d(LOG_TAG, "No option selected")
                    Toast.makeText(context, "Por favor, selecione uma opção.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendVerificationRequestToBackend(fullPhoneNumber: String) {

        val fullPhoneNumber = fullPhoneNumber

        Log.d(LOG_TAG, "Enviando requisição para verificação do número de telefone: $fullPhoneNumber")
        val verifyRequest = VerifyRequestDto(phoneNumber = fullPhoneNumber)

        ApiClient.twilioService.sendPhoneVerification(verifyRequest)
            .enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d(LOG_TAG, "Solicitação de verificação enviada com sucesso.")
                        showToast("Código de verificação reenviado!")
                        dismiss() // Fecha o modal após o envio bem-sucedido
                    } else {
                        Log.e(LOG_TAG, "Erro na solicitação de verificação: ${response.code()}")
                        showToast("Erro no reenvio do código de verificação.")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e(LOG_TAG, "Falha de rede ou servidor: ", t)
                    showToast("Erro de rede ou servidor ao solicitar verificação.")
                }
            })
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}