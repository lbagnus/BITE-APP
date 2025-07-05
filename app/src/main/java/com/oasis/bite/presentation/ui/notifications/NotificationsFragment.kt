package com.oasis.bite.presentation.ui.notifications

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.oasis.bite.R
import com.oasis.bite.databinding.FragmentNotificationsBinding
import com.oasis.bite.domain.models.User
import com.oasis.bite.presentation.adapters.RecetaAdapter
import com.oasis.bite.presentation.ui.home.HomeViewModel
import com.oasis.bite.presentation.ui.home.HomeViewModelFactory


class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var recetaAdapter: RecetaAdapter

    private var idsFavoritos: List<Int> = emptyList()
    private lateinit var noInternetLayoutFavorites: LinearLayout
    private lateinit var noInternetMessageFavorites: TextView
    private lateinit var btnRetryInternetFavorites: Button


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val view= binding.root

        noInternetLayoutFavorites = binding.noInternetLayout
        noInternetMessageFavorites = binding.noInternetMessage
        btnRetryInternetFavorites = binding.btnRetryInternet

        val factory = HomeViewModelFactory(requireContext().applicationContext)
        homeViewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)
        val contextForAdapter = requireContext()

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
            { receta -> idsFavoritos.contains(receta.id) }, usuario, homeViewModel, false, contextForAdapter
        )

        val recyclerRecetas = binding.recyclerRecetas
        recyclerRecetas.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recyclerRecetas.adapter = recetaAdapter
        recyclerRecetas.itemAnimator = null

        homeViewModel.favoritoLiveData.observe(viewLifecycleOwner) { recetas ->
            if (recetas != null && recetas.isNotEmpty()) {
                Log.d("NotificationsFragment", "Recetas favoritas recibidas: ${recetas.size}")
                idsFavoritos = recetas.map { it.id }
                recetaAdapter.actualizarRecetas(recetas)
                binding.fraseCarencia.visibility = View.GONE // Ocultar si hay favoritos
                showContent() // Mostrar contenido si hay datos
            } else if (isInternetAvailable(requireContext())) {
                // Si la lista está vacía pero hay internet, el usuario realmente no tiene favoritos.
                // Mostrar el mensaje de carencia.
                Log.d("NotificationsFragment", "No hay recetas favoritas, pero hay internet.")
                recetaAdapter.actualizarRecetas(emptyList()) // Asegurarse de que el adapter esté vacío
                binding.fraseCarencia.visibility = View.VISIBLE // Mostrar mensaje de carencia
                showContent() // Mostrar el UI normal con el mensaje de carencia
            } else {
                // No hay recetas Y no hay internet (el mensaje showNoInternetMessage ya se encargó de esto si se llamó primero)
                // Esta rama es para cuando el LiveData se actualiza pero no hay internet.
                Log.d("NotificationsFragment", "No se recibieron favoritos o la lista está vacía y no hay internet.")
                binding.fraseCarencia.visibility = View.GONE // Ocultar carencia si no hay internet
                showNoInternetMessage() // Asegurarse de mostrar el mensaje de no internet
            }
        }

        // Configurar el botón de reintentar
        btnRetryInternetFavorites.setOnClickListener {
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
            val usuario = getUsuarioLogueado(requireContext())
            homeViewModel.getRecetasFavoritos(usuario.email){ favoritos ->
                idsFavoritos = favoritos.map { receta -> receta.id }} // Cargar favoritos
        } else {
            showNoInternetMessage() // Mostrar el mensaje de no internet
            Toast.makeText(requireContext(), "No hay conexión a internet.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showContent() {
        binding.recyclerRecetas.visibility = View.VISIBLE // El ScrollView que contiene todo el contenido
        noInternetLayoutFavorites.visibility = View.GONE // Ocultar el mensaje de no internet
    }

    private fun showNoInternetMessage() {
        binding.recyclerRecetas.visibility = View.GONE // Ocultar el contenido normal
        noInternetLayoutFavorites.visibility = View.VISIBLE // Mostrar el mensaje de no internet
    }
}