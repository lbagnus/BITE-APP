package com.oasis.bite.presentation.ui.home

import android.icu.util.ULocale
import com.oasis.bite.presentation.adapters.RecetaAdapter
import com.oasis.bite.presentation.adapters.CategoryAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.oasis.bite.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.oasis.bite.databinding.FragmentHomeBinding
import androidx.navigation.fragment.findNavController
import com.oasis.bite.domain.models.Category


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()

    private lateinit var recetaAdapter: RecetaAdapter
    private lateinit var categoriaAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // --- Inicializar adapters ---
        recetaAdapter = RecetaAdapter(emptyList()){ recetaSeleccionada ->
            val bundle = Bundle().apply {
                putInt("recetaId", recetaSeleccionada.id)
            }
            findNavController().navigate(R.id.recetaFragment, bundle)
        }
        categoriaAdapter = CategoryAdapter(emptyList()){ categoriaSeleccionada ->
            val bundle = Bundle().apply {
                putString("categorianombre", categoriaSeleccionada.nombre)
            }
            findNavController().navigate(R.id.ResultadoBusqueda, bundle)
        }

        // --- Configurar recycler de categorías (horizontal) ---
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
        binding.recyclerCategorias.adapter = categoriaAdapter


        // --- Configurar recycler de recetas (vertical) ---
        val recyclerRecetas = binding.includeRecetas.recyclerRecetas
        recyclerRecetas.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerRecetas.adapter = recetaAdapter
        recyclerRecetas.itemAnimator = null

        // --- Observar ViewModel ---
        homeViewModel.recetasLiveData.observe(viewLifecycleOwner) { recetas ->
            Log.d("HomeFragment", "Recetas recibidas: $recetas")
            if (recetas != null) {
                recetaAdapter.actualizarRecetas(recetas)
            }
        }

        homeViewModel.categoryLiveData.observe(viewLifecycleOwner) { categorias ->
            if (categorias != null) {
                categoriaAdapter.actualizarCategorias(categorias)
            }
        }

        // --- Llamar a la API ---
        homeViewModel.cargarRecetas()

        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

