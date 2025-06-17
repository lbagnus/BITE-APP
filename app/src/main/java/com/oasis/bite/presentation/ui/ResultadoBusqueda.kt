package com.oasis.bite.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.oasis.bite.R
import com.oasis.bite.databinding.ResultadosBusquedasBinding
import com.oasis.bite.presentation.adapters.RecetaAdapter
import com.oasis.bite.presentation.ui.home.HomeViewModel
import kotlin.getValue

class ResultadoBusqueda : Fragment() {
    private var _binding: ResultadosBusquedasBinding? = null

    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()

    private lateinit var recetaAdapter: RecetaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = ResultadosBusquedasBinding.inflate(inflater, container, false)

        // --- Inicializar adapters ---
        recetaAdapter = RecetaAdapter(emptyList()){ recetaSeleccionada ->
            val bundle = Bundle().apply {
                putInt("recetaId", recetaSeleccionada.id)
            }
            findNavController().navigate(R.id.recetaFragment, bundle)
        }
        homeViewModel.cargarRecetasPorCategoria(this.toString())

        return binding.root
    }
}