package com.oasis.bite.presentation.ui.home

import android.content.Context
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
import androidx.lifecycle.ViewModelProvider
import com.oasis.bite.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.oasis.bite.databinding.FragmentHomeBinding
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.oasis.bite.data.toReceta
import com.oasis.bite.domain.models.Category
import com.oasis.bite.domain.models.User


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    val factory = HomeViewModelFactory(requireContext().applicationContext)
    val homeViewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

    private lateinit var categoriaAdapter: CategoryAdapter
    private lateinit var recetaAdapter: RecetaAdapter

    private var idsFavoritos: List<Int> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // 1. Obtener usuario logueado
        val usuario = getUsuarioLogueado(requireContext())

        // 2. Inicializar adapters (aunque sea vacío al principio)
        recetaAdapter = RecetaAdapter(
            emptyList(),
            { recetaSeleccionada ->
                val bundle = Bundle().apply {
                    putInt("recetaId", recetaSeleccionada.id)
                }
                findNavController().navigate(R.id.recetaFragment, bundle)
            },
            { receta -> idsFavoritos.contains(receta.id)}, usuario, homeViewModel, false
        )

        // 3. Configurar recycler de recetas
        val recyclerRecetas = binding.includeRecetas.recyclerRecetas
        recyclerRecetas.layoutManager = LinearLayoutManager(requireContext())
        recyclerRecetas.adapter = recetaAdapter
        recyclerRecetas.itemAnimator = null

        // 4. Configurar recycler de categorías
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

        categoriaAdapter = CategoryAdapter(listaCategorias) { categoriaSeleccionada ->
            val bundle = Bundle().apply {
                putString("categorianombre", categoriaSeleccionada.nombre)
            }
            findNavController().navigate(R.id.ResultadoBusqueda, bundle)
        }

        binding.recyclerCategorias.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerCategorias.adapter = categoriaAdapter

        homeViewModel.getRecetasFavoritos(usuario.email) { favoritos ->
            idsFavoritos = favoritos.map { receta -> receta.id }}

        homeViewModel.favoritoLiveData.observe(viewLifecycleOwner) { recetas ->
            Log.d("Favoritos", "Recetas recibidas: $recetas")
            if (recetas != null) {
                idsFavoritos = recetas.map { it.id }}}

        // 5. Observar cambios en las recetas y favoritos
        homeViewModel.recetasLiveData.observe(viewLifecycleOwner) { recetas ->
            if (recetas != null) {
                Log.d("Es favorito? home", idsFavoritos.toString())
                recetaAdapter.actualizarRecetas(recetas)
            }
        }

        homeViewModel.categoryLiveData.observe(viewLifecycleOwner) { categorias ->
            if (categorias != null) {
                categoriaAdapter.actualizarCategorias(categorias)
            }
        }

        // 6. Obtenemos favoritos si hay usuario


        // 7. Cargar recetas al iniciar
        homeViewModel.cargarRecetas()

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



