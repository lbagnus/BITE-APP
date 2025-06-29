package com.oasis.bite.presentation.ui.dashboard

import android.content.Context
import android.content.Intent
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
import com.oasis.bite.AgregarTituloRecetaActivty
import com.oasis.bite.R
import com.oasis.bite.databinding.FragmentDashboardBinding
import com.oasis.bite.domain.models.User
import com.oasis.bite.presentation.adapters.RecetaAdapter
import com.oasis.bite.presentation.ui.home.HomeViewModel
import com.oasis.bite.presentation.ui.home.HomeViewModelFactory

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var recetaAdapter: RecetaAdapter

    private var idsFavoritos: List<Int> = emptyList()

    private lateinit var noInternetLayoutDashboard: LinearLayout
    private lateinit var noInternetMessageDashboard: TextView
    private lateinit var btnRetryInternetDashboard: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val view = binding.root

        noInternetLayoutDashboard = binding.noInternetLayout
        noInternetMessageDashboard = binding.noInternetMessage
        btnRetryInternetDashboard = binding.btnRetryInternet

        val factory = HomeViewModelFactory(requireContext().applicationContext)
        homeViewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

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
            if (recetas != null && recetas.isNotEmpty()) {
                Log.d("DashboardFragment", "Mis Recetas recibidas: ${recetas.size}")
                recetaAdapter.actualizarRecetas(recetas)
                showContent() // Mostrar contenido si hay datos
            } else if (isInternetAvailable(requireContext())) {
                // Si la lista está vacía pero hay internet, podría ser que el usuario no tiene recetas.
                // Podrías mostrar un mensaje específico aquí como "No tienes recetas cargadas".
                Log.d("DashboardFragment", "Lista de recetas del usuario vacía, pero hay internet.")
                showContent() // Mostrar el UI para que vean el botón de agregar
                // Opcional: mostrar un TextView "Aún no tienes recetas. ¡Crea una!"
            } else {
                // No hay recetas Y no hay internet (el mensaje showNoInternetMessage ya se encargó de esto si se llamó primero)
                // Esta rama es para cuando el LiveData se actualiza pero no hay internet.
                Log.d("DashboardFragment", "No se recibieron recetas o la lista está vacía y no hay internet.")
                showNoInternetMessage() // Asegurarse de mostrar el mensaje de no internet
            }
        }

        val botonAgregar = binding.btnAgregarReceta
        botonAgregar.setOnClickListener {
                val intent = Intent(requireContext(), AgregarTituloRecetaActivty::class.java)
                intent.putExtra("usuarioEmail", usuario.email)
                intent.putExtra("usuarioUserName", usuario.username)
                startActivity(intent)

        }

        // Configurar el botón de reintentar
        btnRetryInternetDashboard.setOnClickListener {
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
            homeViewModel.cargarRecetasUsuario(usuario.username)
        } else {
            showNoInternetMessage() // Mostrar el mensaje de no internet
            Toast.makeText(requireContext(), "No hay conexión a internet.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showContent() {
        binding.recyclerRecetas.visibility = View.VISIBLE // El ScrollView que contiene todo el contenido
        noInternetLayoutDashboard.visibility = View.GONE // Ocultar el mensaje de no internet
    }

    private fun showNoInternetMessage() {
        binding.recyclerRecetas.visibility = View.GONE // Ocultar el contenido normal
        noInternetLayoutDashboard.visibility = View.VISIBLE // Mostrar el mensaje de no internet
    }

}