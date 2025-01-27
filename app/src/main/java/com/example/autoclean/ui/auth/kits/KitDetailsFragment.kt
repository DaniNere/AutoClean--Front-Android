package com.example.autoclean.ui.auth.kits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.autoclean.data.model.ItemKitCleaning
import com.example.autoclean.databinding.FragmentKitDetailsBinding
import com.example.autoclean.ui.adapter.KitCleaningAdapter

class KitDetailsFragment : Fragment() {

    private var _binding: FragmentKitDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var itemAdapter: KitCleaningAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKitDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initRecycleItem(getItems())


    }



    private fun initRecycleItem(itemList: List<ItemKitCleaning>) {
        itemAdapter = KitCleaningAdapter(itemList)

        binding.rvItemKit.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvItemKit.setHasFixedSize(true)
        binding.rvItemKit.adapter = itemAdapter
    }

    private fun getItems(): List<ItemKitCleaning> {
        return listOf(
            ItemKitCleaning(
                id = "0",
                title = "Equipamentos",
                description = listOf("Reservatório de Água", "Mangueira",
                    "Lavador de Pressão", "Gerador de Energia",
                    "Extensão de Tomada", "Aspirador")
            ),
            ItemKitCleaning(
                id = "1",
                title = "Caixa de Itens",
                description = listOf("3 borrifadores", "Escovas",
                    "Panos", "Buchas", "Pinceis", "Protetor auricular",
                    "Pretinho(produto para pneus", "Silicone Spray",
                    "Cera Spray", "Caixinha com luva e bucha")
            ),
            ItemKitCleaning(
                id = "2",
                title = "Produtos de Limpeza",
                description = listOf("Lava a Seco",
                    "Limpador Estofados",
                    "Limpador de Superfícies")
            )
        )
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
