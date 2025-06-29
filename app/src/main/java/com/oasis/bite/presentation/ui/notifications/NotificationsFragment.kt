package com.oasis.bite.presentation.ui.notifications

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.oasis.bite.R
import com.oasis.bite.databinding.FragmentNotificationsBinding
import com.oasis.bite.domain.models.User
import com.oasis.bite.presentation.adapters.RecetaAdapter
import com.oasis.bite.presentation.ui.home.HomeViewModel
import com.oasis.bite.presentation.ui.home.HomeViewModelFactory
import kotlin.getValue

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var recetaAdapter: RecetaAdapter

    private var idsFavoritos: List<Int> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        val factory = HomeViewModelFactory(requireContext().applicationContext)
        homeViewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

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
            { receta -> idsFavoritos.contains(receta.id) }, usuario, homeViewModel, false
        )

        val recyclerRecetas = binding.recyclerRecetas
        recyclerRecetas.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerRecetas.adapter = recetaAdapter
        recyclerRecetas.itemAnimator = null

        homeViewModel.favoritoLiveData.observe(viewLifecycleOwner) { recetas ->
            Log.d("Favoritos", "Recetas recibidas: $recetas")
            if (recetas != null) {
                idsFavoritos = recetas.map { it.id }
                recetaAdapter.actualizarRecetas(recetas)
            }
            if (idsFavoritos.isEmpty()){
                binding.fraseCarencia.visibility = View.VISIBLE}
        }


            homeViewModel.getRecetasFavoritos(usuario.email) { favoritos ->
                idsFavoritos = favoritos.map { receta -> receta.id }
                // Recargamos la lista con la info de favoritos
                recetaAdapter.actualizarRecetas(homeViewModel.recetasLiveData.value ?: emptyList())
            }




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