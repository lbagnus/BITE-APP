package com.oasis.bite.presentation.ui.home

import com.oasis.bite.presentation.adapters.RecetaAdapter
import com.oasis.bite.presentation.adapters.CategoryAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.oasis.bite.Category
import com.oasis.bite.R
import com.oasis.bite.Receta
import com.oasis.bite.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // --- Configurar RecyclerView de categorías (horizontal) ---
        val listaCategorias = listOf(
            Category("Pastas", R.drawable.ic_pasta),
            Category("Ensaladas", R.drawable.ic_salad),
            Category("Postres", R.drawable.ic_postre),
            Category("Pizza", R.drawable.ic_pizza),
            Category("Sano", R.drawable.ic_sano),
            Category("Carnes", R.drawable.ic_carne),
            Category("Veggie", R.drawable.ic_veggie),
            Category("Entre Pan", R.drawable.ic_entrepan)
        )

        binding.recyclerCategorias.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerCategorias.adapter = CategoryAdapter(listaCategorias)

        // --- Configurar RecyclerView de recetas (horizontal) ---
        val listaRecetas = listOf(
            Receta("Pasta a la carbonara", R.drawable.ic_carbonara, "★ 4.3 (100 Reviews)", "25 min", "Fácil", "Julia Roberts"),
            Receta("Sopa Thai", R.drawable.ic_carbonara, "★ 4.7 (80 Reviews)", "35 min", "Media", "Gordon Ramsay"),
            Receta("Ensalada César", R.drawable.ic_carbonara, "★ 4.5 (60 Reviews)", "15 min", "Fácil", "Jamie Oliver"),

        )

        val sectionView = binding.includeRecetas
        val recyclerRecetas = sectionView.recyclerRecetas
        recyclerRecetas.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerRecetas.adapter = RecetaAdapter(listaRecetas)
        recyclerRecetas.itemAnimator = null
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
