package com.oasis.bite.presentation.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.oasis.bite.R
import com.oasis.bite.databinding.ResultadosBusquedasBinding
import com.oasis.bite.domain.models.User
import com.oasis.bite.presentation.adapters.RecetaAdapter
import com.oasis.bite.presentation.ui.home.HomeViewModel
import kotlin.getValue

class ResultadoBusqueda : Fragment() {
    private var _binding: ResultadosBusquedasBinding? = null

    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()

    private lateinit var recetaAdapter: RecetaAdapter

    private var idsFavoritos: List<Int> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = ResultadosBusquedasBinding.inflate(inflater, container, false)

        // --- Inicializar adapters ---
        val usuario = getUsuarioLogueado(requireContext())
                    recetaAdapter = RecetaAdapter(
                         emptyList(),
                        { recetaSeleccionada ->
                            val bundle = Bundle().apply {
                                putInt("recetaId", recetaSeleccionada.id)
                            }
                            findNavController().navigate(R.id.recetaFragment, bundle)
                        },
                        { receta -> idsFavoritos?.contains(receta.id) == true }, usuario, homeViewModel, false // esta es la validación dinámica
                    )
        val recyclerRecetas = binding.recyclerRecetas
        recyclerRecetas.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerRecetas.adapter = recetaAdapter
        recyclerRecetas.itemAnimator = null

        homeViewModel.getRecetasFavoritos(usuario.email) { favoritos ->
            idsFavoritos = favoritos.map { receta -> receta.id }}

        homeViewModel.favoritoLiveData.observe(viewLifecycleOwner) { recetas ->
            Log.d("Favoritos", "Recetas recibidas: $recetas")
            if (recetas != null) {
                idsFavoritos = recetas.map { it.id }}}

        homeViewModel.recetasLiveData.observe(viewLifecycleOwner) { recetas ->
            Log.d("HomeFragment", "Recetas recibidas: $recetas")
            if (recetas != null) {
                recetaAdapter.actualizarRecetas(recetas)
            }
        }

        val categoriaNombre = arguments?.getString("categorianombre")

        categoriaNombre?.let {
            homeViewModel.cargarRecetasPorCategoria(it)
        }

        return binding.root
    }
    fun getUsuarioLogueado(context: Context): User {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString("usuario_logueado", null)
        return json.let { Gson().fromJson(it, User::class.java) }
    }
}