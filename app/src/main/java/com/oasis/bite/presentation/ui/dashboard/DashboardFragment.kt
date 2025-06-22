package com.oasis.bite.presentation.ui.dashboard

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.oasis.bite.R
import com.oasis.bite.databinding.FragmentDashboardBinding
import com.oasis.bite.domain.models.User
import com.oasis.bite.presentation.adapters.RecetaAdapter
import com.oasis.bite.presentation.ui.home.HomeViewModel
import kotlin.getValue

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()

    private lateinit var recetaAdapter: RecetaAdapter

    private var idsFavoritos: List<Int> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val usuario = getUsuarioLogueado(requireContext())
        recetaAdapter = RecetaAdapter(
            emptyList(),
            { recetaSeleccionada ->
                val bundle = Bundle().apply {
                    putInt("recetaId", recetaSeleccionada.id)
                }
                findNavController().navigate(R.id.recetaFragment, bundle)
            },
            { receta -> idsFavoritos.contains(receta.id) }, usuario, homeViewModel, true
        )

        val recyclerRecetas = binding.recyclerRecetas
        recyclerRecetas.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerRecetas.adapter = recetaAdapter
        recyclerRecetas.itemAnimator = null

        homeViewModel.recetasLiveData.observe(viewLifecycleOwner) { recetas ->
            if (recetas != null) {
                Log.d("Es favorito? home", idsFavoritos.toString())
                recetaAdapter.actualizarRecetas(recetas)
            }
        }

        homeViewModel.cargarRecetasUsuario(usuario.username)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    fun getUsuarioLogueado(context: Context): User {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString("usuario_logueado", null)
        return json.let { Gson().fromJson(it, User::class.java) }
    }
}