package com.oasis.bite

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.widget.LinearLayout
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.oasis.bite.databinding.ActivityMiscalculosBinding
import com.oasis.bite.presentation.adapters.MisCalculosRecetaAdapter
import com.oasis.bite.presentation.viewmodel.RecetaViewModel
import com.oasis.bite.presentation.viewmodel.RecetaViewModelFactory

class MisCalculosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMiscalculosBinding
    private lateinit var viewModel: RecetaViewModel
    private lateinit var recetaAdapter: MisCalculosRecetaAdapter // Usa el nuevo adaptador

    // Vistas para el mensaje de vacío y no internet (si aplica)
    private lateinit var emptyMessageTextView: TextView
    private lateinit var noInternetLayout: LinearLayout // Si lo incluyes en tu XML


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMiscalculosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()


        // Inicializar ViewModel
        val factory = RecetaViewModelFactory(applicationContext)
        viewModel = ViewModelProvider(this, factory).get(RecetaViewModel::class.java)

        // Inicializar vistas
        val botonVolver = binding.btnVolver
        val recyclerRecetas = binding.recyclerRecetas // Tu RecyclerView en el XML
        emptyMessageTextView = binding.emptyMessageTextView // El TextView para el mensaje de vacío
        // noInternetLayout = binding.noInternetLayoutMisCalculos // Si lo añades en tu XML

        botonVolver.setOnClickListener {
            finish()
        }

        // Inicializar Adaptador
        recetaAdapter = MisCalculosRecetaAdapter(
            emptyList(),
            onDeleteClick = { recetaToDelete ->
                viewModel.deleteLocalAdjustedReceta(recetaToDelete)
            }, onItemClick = { recetaSeleccionada ->
                val intent = android.content.Intent(this, RecetaLocalDetailActivity::class.java).apply {
                    putExtra("localRecetaId", recetaSeleccionada.localId) // Pasa el ID LOCAL
                    // No necesitas flags especiales como CLEAR_TOP si solo vas de A a B y esperas volver
                }
                startActivity(intent)
                // No llames finish() aquí a menos que quieras que MisCalculosActivity se cierre.
                // Si la mantienes abierta, al presionar 'atrás' en RecetaLocalDetailActivity, volverás a MisCalculosActivity.
            },
            context = this
        )

        recyclerRecetas.layoutManager = LinearLayoutManager(this)
        recyclerRecetas.adapter = recetaAdapter
        recyclerRecetas.itemAnimator = null // Opcional, mejora rendimiento

        // Observar las recetas ajustadas del ViewModel
        viewModel.localAdjustedRecetas.observe(this) { recetas ->
            if (recetas != null && recetas.isNotEmpty()) {
                recetaAdapter.actualizarRecetas(recetas)
                showContent(true) // Hay recetas, mostrar el RecyclerView
            } else {
                recetaAdapter.actualizarRecetas(emptyList()) // Asegurarse de que el adaptador esté vacío
                showContent(false) // No hay recetas, mostrar el mensaje de vacío
            }
        }

        // Observar mensajes del ViewModel para feedback al usuario (éxito/fallo al guardar/eliminar)
        viewModel.messageForUser.observe(this) { message ->
            if (message != null && message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }

        // --- Lógica de Carga Inicial ---
        // Verificar conexión y cargar datos (incluso sin internet)
        checkAndLoadData() // Llama a esta función para iniciar
    }

    // Función para manejar la visibilidad del contenido y mensajes
    private fun showContent(hasData: Boolean) {
        if (hasData) {
            binding.recyclerRecetas.visibility = View.VISIBLE
            binding.emptyMessageTextView.visibility = View.GONE
        } else {
            binding.recyclerRecetas.visibility = View.GONE
            binding.emptyMessageTextView.visibility = View.VISIBLE
        }
        // noInternetLayout.visibility = View.GONE // Ocultar overlay de no internet si lo tienes
    }

    // Función para verificar internet (solo si necesitas lógica de no internet aquí)
    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    // Lógica para cargar los datos al inicio
    private fun checkAndLoadData() {
        // En "Mis Cálculos", quieres mostrar las recetas guardadas localmente SIEMPRE,
        // incluso sin internet. La lógica de no internet del overlay solo es informativa.
        viewModel.loadLocalAdjustedRecetas()

        if (!isInternetAvailable(this)) {
            // Si tienes un layout de no internet a nivel de actividad, muéstralo aquí.
            // noInternetLayout.visibility = View.VISIBLE
            Toast.makeText(this, "Sin conexión a internet. Verás solo las recetas guardadas localmente.", Toast.LENGTH_LONG).show()
        }
        // else { noInternetLayout.visibility = View.GONE }
    }
}