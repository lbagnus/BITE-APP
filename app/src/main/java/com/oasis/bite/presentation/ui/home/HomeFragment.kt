package com.oasis.bite.presentation.ui.home

import android.content.Context
import android.net.ConnectivityManager
import android.icu.util.ULocale
import android.net.NetworkInfo
import com.oasis.bite.presentation.adapters.RecetaAdapter
import com.oasis.bite.presentation.adapters.CategoryAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.oasis.bite.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.oasis.bite.databinding.FragmentHomeBinding
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.oasis.bite.domain.models.Category
import com.oasis.bite.domain.models.User


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var categoriaAdapter: CategoryAdapter
    private lateinit var recetaAdapter: RecetaAdapter

    private var idsFavoritos: List<Int> = emptyList()

    private lateinit var noInternetLayout: LinearLayout
    private lateinit var noInternetMessage: TextView
    private lateinit var btnRetryInternet: Button
    val contextForAdapter = requireContext()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val factory = HomeViewModelFactory(requireContext().applicationContext)
        homeViewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)
        val view = binding.root

        // Inicializar vistas de no internet
        noInternetLayout = binding.noInternetLayout
        noInternetMessage = binding.noInternetMessage
        btnRetryInternet = binding.btnRetryInternet

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
            { receta -> idsFavoritos.contains(receta.id)}, usuario, homeViewModel, false, contextForAdapter
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

        // 5. Observar cambios en las recetas y favoritos
        homeViewModel.recetasLiveData.observe(viewLifecycleOwner) { recetas ->
            if (recetas != null && recetas.isNotEmpty()) {
                Log.d("HomeFragment", "Recetas recibidas: ${recetas.size}")
                recetaAdapter.actualizarRecetas(recetas)
                showContent() // Mostrar contenido si hay datos
            } else {
                Log.d("HomeFragment", "No se recibieron recetas o la lista está vacía.")
                // No ocultar aquí, la lógica de no internet lo hará
            }
        }

        homeViewModel.categoryLiveData.observe(viewLifecycleOwner) { categorias ->
            if (categorias != null && categorias.isNotEmpty()) {
                categoriaAdapter.actualizarCategorias(categorias)
            } else {
                Log.d("HomeFragment", "No se recibieron categorías o la lista está vacía.")
            }
        }

        homeViewModel.favoritoLiveData.observe(viewLifecycleOwner) { recetas ->
            if (recetas != null) {
                idsFavoritos = recetas.map { receta -> receta.id }
                // Forzar una actualización de recetas para que el estado de favoritos se refleje
                // Esto es importante si el favorito se cargó después de las recetas principales.
                homeViewModel.recetasLiveData.value?.let { currentRecetas ->
                    recetaAdapter.actualizarRecetas(currentRecetas)
                }
                Log.d("HomeFragment", "Favoritos recibidos: $idsFavoritos")
            }
        }


        // Configurar el botón de reintentar
        btnRetryInternet.setOnClickListener {
            checkInternetAndLoadData() // Reintentar cuando se hace clic en el botón
        }

        // Llama a la función que maneja la lógica de carga y visibilidad al inicio
        checkInternetAndLoadData()

        return view
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
    // Función para verificar la conexión a Internet (la misma que ya tenías)
    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun checkInternetAndLoadData() {
        if (isInternetAvailable(requireContext())) {
            showContent() // Mostrar el contenido normal
            // Cargar datos de la API
            val usuario = getUsuarioLogueado(requireContext())
            homeViewModel.getRecetasFavoritos(usuario.email){ favoritos ->
                idsFavoritos = favoritos.map { receta -> receta.id }} // Cargar favoritos primero
            homeViewModel.cargarRecetas() // Luego cargar recetas
            // Y categorías
        } else {
            showNoInternetMessage() // Mostrar el mensaje de no internet
            Toast.makeText(requireContext(), "No hay conexión a internet.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showContent() {
        binding.includeRecetas.recyclerRecetas.visibility = View.VISIBLE // El ScrollView que contiene todo el contenido
        noInternetLayout.visibility = View.GONE // Ocultar el mensaje de no internet
    }

    private fun showNoInternetMessage() {
        binding.includeRecetas.recyclerRecetas.visibility = View.GONE // Ocultar el contenido normal
        noInternetLayout.visibility = View.VISIBLE // Mostrar el mensaje de no internet
    }
}