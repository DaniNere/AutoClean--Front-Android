package com.example.autoclean.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.autoclean.R
import com.example.autoclean.databinding.FragmentProfileProfissionalBinding

class ProfileProfissionalFragment : Fragment() {

    private var _binding: FragmentProfileProfissionalBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileProfissionalBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setupClickableText()


        binding.checkboxTerms.setOnCheckedChangeListener { _, isChecked ->
            binding.btnContinue.isEnabled = isChecked
        }


        binding.btnContinue.setOnClickListener {
            Toast.makeText(
                requireContext(),
                "Informações enviadas com sucesso!",
                Toast.LENGTH_SHORT
            ).show()

            findNavController().navigate(R.id.action_profileProfissionalFragment_to_professionalVerificationFragment2)
        }
    }

    private fun setupClickableText() {
        val termsText =
            "Ao concordar, você aceita os Termos de Uso e a Política de Privacidade, que são obrigatórias para continuar utilizando nossos serviços."
        val spannableString = SpannableString(termsText)


        val termsClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openPdfOrWeb("https://www.seusite.com/termos_de_uso.pdf")
            }
        }


        val privacyClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                openPdfOrWeb("https://www.seusite.com/politica_de_privacidade.pdf")
            }
        }


        spannableString.setSpan(
            termsClickable,
            26,
            40,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        ) // Índices de "Termos de Uso"
        spannableString.setSpan(
            privacyClickable,
            43,
            63,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        ) // Índices de "Política de Privacidade"

        binding.textTerms.text = spannableString
        binding.textTerms.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun openPdfOrWeb(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Não foi possível abrir o documento",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
