package com.example.autoclean.ui.auth

import android.os.Bundle
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.autoclean.R
import com.example.autoclean.databinding.FragmentChooseKitBinding

class ChooseKitFragment : Fragment() {

    private var _binding: FragmentChooseKitBinding? = null
    private val binding get() = _binding!!
    private var selectedCard: CardView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChooseKitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    private fun initListeners() {

        val card1 = binding.root.findViewById<CardView>(R.id.card1)
        val card2 = binding.root.findViewById<CardView>(R.id.card2)

        val cards = listOf(card1, card2)

        cards.forEach { card ->
            card.setOnClickListener {
                selectedCard?.setBackgroundResource(R.drawable.border_unselected)
                card.setBackgroundResource(R.drawable.border_selected)
                selectedCard = card
            }
        }

        binding.btnContinue.setOnClickListener{
            if (selectedCard == card1){
                findNavController().navigate(R.id.action_cleaningkitmanagerFragment_to_validationKitFragment)
            } else if (selectedCard ==card2){
                findNavController().navigate(R.id.action_cleaningkitmanagerFragment_to_cleaningKitPaymentFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
