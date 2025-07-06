package com.oasis.bite.presentation.ui

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
import com.oasis.bite.databinding.FragmentPendingObjectBinding
import com.oasis.bite.domain.models.User
import com.oasis.bite.presentation.adapters.AutorizarComentarioAdapter
import com.oasis.bite.presentation.adapters.AutorizarRecetaAdapter
import com.oasis.bite.presentation.ui.home.HomeViewModel
import com.oasis.bite.presentation.ui.home.HomeViewModelFactory

class PendingObjectFragment : Fragment() {

    private var _binding: FragmentPendingObjectBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var recetaAdapter: AutorizarRecetaAdapter
    private lateinit var comentarioAdapter: AutorizarComentarioAdapter

    // Vistas de UI
    private lateinit var recyclerRecetas: androidx.recyclerview.widget.RecyclerView
    private lateinit var recyclerComentarios: androidx.recyclerview.widget.RecyclerView
    private lateinit var emptyMessageTextView: TextView
    private lateinit var noInternetLayoutFragment: LinearLayout
    private lateinit var btnRetryInternetFragment: Button

    // Argumento para saber qué tipo de objeto mostrar
    private var objectType: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPendingObjectBinding.inflate(inflater, container, false)
        val view = binding.root

        // Obtener el argumento objectType
        objectType = arguments?.getString("objectType")
        Log.d("PendingObject, objeto", objectType.toString())

        // Inicializar vistas
        recyclerRecetas = binding.recyclerRecetasPending
        recyclerComentarios = binding.recyclerComentariosPending
        emptyMessageTextView = binding.emptyMessageTextView
        noInternetLayoutFragment = binding.noInternetLayoutFragment
        btnRetryInternetFragment = binding.btnRetryInternetFragment

        // Inicializar ViewModel
        val factory = HomeViewModelFactory(requireContext().applicationContext)
        homeViewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        // Obtener usuario logueado (necesario para adaptadores o ViewModel)
        val usuario = getUsuarioLogueado(requireContext())

        // Inicializar adaptadores
        recetaAdapter = AutorizarRecetaAdapter(
            emptyList(),
            { recetaSeleccionada ->
                val bundle = Bundle().apply { putInt("recetaId", recetaSeleccionada.id) }
                findNavController().navigate(R.id.action_pendingObjectFragment_to_recetaFragment, bundle)
            },
            homeViewModel // Pasa el ViewModel si el adaptador lo necesita
        )

        comentarioAdapter = AutorizarComentarioAdapter(
            emptyList(),
            homeViewModel // Pasa el ViewModel si el adaptador lo necesita
            // Asegúrate de tener callbacks para aprobar/rechazar si es necesario
            /*
            onApproveClick = { comentario -> homeViewModel.approveComment(comentario.id, usuario.email) },
            onRejectClick = { comentario -> homeViewModel.rejectComment(comentario.id, usuario.email) }
            */
        )

        // Configurar RecyclerViews (ambos)
        recyclerRecetas.layoutManager = LinearLayoutManager(requireContext())
        recyclerRecetas.adapter = recetaAdapter
        recyclerRecetas.itemAnimator = null

        recyclerComentarios.layoutManager = LinearLayoutManager(requireContext())
        recyclerComentarios.adapter = comentarioAdapter
        recyclerComentarios.itemAnimator = null

        // Observadores de LiveData
        homeViewModel.pendingReceta.observe(viewLifecycleOwner) { recetas ->
            if (objectType == "receta") { // Solo actualiza si estamos en modo "receta"
                if (recetas != null && recetas.isNotEmpty()) {
                    Log.d("pending", "llegue a la zona recetas")
                    recetaAdapter.actualizarRecetas(recetas)
                    showContent(ContentType.RECETAS)
                } else if (isInternetAvailable(requireContext())) {
                    recetaAdapter.actualizarRecetas(emptyList())
                    showEmptyState("No hay recetas pendientes de autorización.")
                } else {
                    showNoInternetMessage()
                }
            }
        }

        homeViewModel.pendingComments.observe(viewLifecycleOwner) { comments ->
            if (objectType == "comentario") { // Solo actualiza si estamos en modo "comentario"
                if (comments != null && comments.isNotEmpty()) {
                    comentarioAdapter.actualizarComentarios(comments)
                    showContent(ContentType.COMENTARIOS)
                } else if (isInternetAvailable(requireContext())) {
                    comentarioAdapter.actualizarComentarios(emptyList())
                    showEmptyState("No hay comentarios pendientes de autorización.")
                } else {
                    showNoInternetMessage()
                }
            }
        }

        // Listener para reintentar
        btnRetryInternetFragment.setOnClickListener {
            checkInternetAndLoadData()
        }

        // Llama a la función para cargar datos y actualizar UI
        checkInternetAndLoadData()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Funciones utilitarias (Mantener con 'context: Context' si son generales)
    private fun getUsuarioLogueado(context: Context): User {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString("usuario_logueado", null)
        return json?.let { Gson().fromJson(it, User::class.java) } ?: throw IllegalStateException("Usuario no logueado")
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    // Lógica para mostrar/ocultar los elementos de UI
    private fun checkInternetAndLoadData() {
        if (isInternetAvailable(requireContext())) {
            // Ocultar mensajes de error/vacío antes de cargar
            emptyMessageTextView.visibility = View.GONE
            noInternetLayoutFragment.visibility = View.GONE

            val usuario = getUsuarioLogueado(requireContext())
            if (objectType == "receta") {
                homeViewModel.loadPendingRecetas() // Carga tus recetas pendientes
            } else if (objectType == "comentario") {
                homeViewModel.loadPendingComments() // Carga tus comentarios pendientes
            }
        } else {
            showNoInternetMessage()
            Toast.makeText(requireContext(), "No hay conexión a internet.", Toast.LENGTH_SHORT).show()
        }
    }

    // Enum para el tipo de contenido (Ayuda a la claridad)
    private enum class ContentType {
        RECETAS, COMENTARIOS
    }

    private fun showContent(type: ContentType) {
        noInternetLayoutFragment.visibility = View.GONE
        emptyMessageTextView.visibility = View.GONE

        when (type) {
            ContentType.RECETAS -> {
                recyclerRecetas.visibility = View.VISIBLE
                recyclerComentarios.visibility = View.GONE
            }
            ContentType.COMENTARIOS -> {
                recyclerComentarios.visibility = View.VISIBLE
                recyclerRecetas.visibility = View.GONE
            }
        }
    }

    private fun showNoInternetMessage() {
        recyclerRecetas.visibility = View.GONE
        recyclerComentarios.visibility = View.GONE
        emptyMessageTextView.visibility = View.GONE // Ocultar mensaje de vacío
        noInternetLayoutFragment.visibility = View.VISIBLE // Mostrar mensaje de no internet
    }

    private fun showEmptyState(message: String) {
        recyclerRecetas.visibility = View.GONE
        recyclerComentarios.visibility = View.GONE
        noInternetLayoutFragment.visibility = View.GONE
        emptyMessageTextView.text = message
        emptyMessageTextView.visibility = View.VISIBLE
    }
}