package com.oasis.bite

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.oasis.bite.databinding.ActivityAutorizarObjetoBinding
import com.oasis.bite.databinding.ActivityMainBinding
import com.oasis.bite.domain.models.User
import com.oasis.bite.presentation.adapters.AutorizarComentarioAdapter
import com.oasis.bite.presentation.adapters.AutorizarRecetaAdapter
import com.oasis.bite.presentation.ui.home.HomeViewModel
import com.oasis.bite.presentation.ui.home.HomeViewModelFactory
import kotlin.collections.isNotEmpty

class AutorizarActivity :AppCompatActivity() {

    private lateinit var recetaAdapter: AutorizarRecetaAdapter
    private lateinit var comentarioAdapter : AutorizarComentarioAdapter
    private lateinit var binding: ActivityAutorizarObjetoBinding
    private lateinit var navController: NavController

    @SuppressLint("SoonBlockedPrivateApi", "WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = HomeViewModelFactory(applicationContext)
        val homeViewModel =  ViewModelProvider(this, factory).get(HomeViewModel::class.java)
        binding = ActivityAutorizarObjetoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        val isReceta = intent.getBooleanExtra("Receta", true)

        val botonCancelar = binding.btnVolver

        botonCancelar.setOnClickListener {
            finish()
        }
        recetaAdapter = AutorizarRecetaAdapter(
            emptyList(),
            { recetaSeleccionada ->
                val bundle = Bundle().apply {
                    putInt("recetaId", recetaSeleccionada.id)
                }
                navController.navigate(R.id.recetaFragment, bundle)
            }
        )

        comentarioAdapter = AutorizarComentarioAdapter(emptyList())

        // 3. Configurar recycler de recetas
        val recyclerRecetas = binding.includeRecetas.recyclerRecetas
        recyclerRecetas.layoutManager = LinearLayoutManager(this)
        recyclerRecetas.adapter = recetaAdapter
        recyclerRecetas.itemAnimator = null

        val recyclerComentarios = binding.includeComentarios.recyclerRecetas
        recyclerComentarios.layoutManager = LinearLayoutManager(this)
        recyclerComentarios.adapter = recetaAdapter
        recyclerComentarios.itemAnimator = null

        if (isReceta){
            recyclerRecetas.visibility = View.VISIBLE
        }else{
            recyclerComentarios.visibility = View.VISIBLE
        }

        homeViewModel.recetasLiveData.observe(this) { recetas ->
            if (recetas != null && recetas.isNotEmpty()) {
                Log.d("HomeFragment", "Recetas recibidas: ${recetas.size}")
                recetaAdapter.actualizarRecetas(recetas)
                showContent() // Mostrar contenido si hay datos
            } else {
                Log.d("HomeFragment", "No se recibieron recetas o la lista está vacía.")
                // No ocultar aquí, la lógica de no internet lo hará
            }
        }

        //aca falta el homeviewmodel de los comentarios

    }
    private fun showContent() {
        binding.includeRecetas.recyclerRecetas.visibility = View.VISIBLE // El ScrollView que contiene todo el contenido
        //noInternetLayout.visibility = View.GONE // Ocultar el mensaje de no internet
    }

    private fun showNoInternetMessage() {
        binding.includeRecetas.recyclerRecetas.visibility = View.GONE // Ocultar el contenido normal
        //noInternetLayout.visibility = View.VISIBLE // Mostrar el mensaje de no internet
    }
    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }


}