package com.example.autoclean.ui.auth.forgot

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.autoclean.R
import com.example.autoclean.databinding.FragmentPasswordChangeBinding
import com.example.autoclean.databinding.FragmentRecoverCodeBinding



class RecoverCodeFragment : Fragment() {

    private var _binding: FragmentRecoverCodeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentRecoverCodeBinding.inflate(inflater, container, false)
        return binding.root


    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()
    }

    private fun initListeners (){
        binding.btnContinue.setOnClickListener{
            findNavController().navigate(R.id.action_recoverCodeFragment_to_passwordChangeFragment)
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}