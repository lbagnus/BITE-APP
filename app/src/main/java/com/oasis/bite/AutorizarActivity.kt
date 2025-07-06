package com.oasis.bite

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.oasis.bite.databinding.ActivityAutorizarObjetoBinding

class AutorizarActivity :AppCompatActivity() {

    private lateinit var binding: ActivityAutorizarObjetoBinding
    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAutorizarObjetoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.auth_nav_host_fragment) as NavHostFragment
        if (navHostFragment == null) {
            Log.e("AutorizarActivity", "ERROR: NavHostFragment con ID R.id.auth_nav_host_fragment no encontrado o no es un NavHostFragment.")
            // Puedes mostrar un Toast al usuario o simplemente finalizar la actividad
            // Toast.makeText(this, "Error interno de la aplicación. Por favor, reinicie.", Toast.LENGTH_LONG).show()
            finish()
            return // Salir de onCreate para evitar NullPointerException
        }
        navController = navHostFragment.navController

        val isReceta = intent.getBooleanExtra("Receta",true)
        val objectTypeToDisplay = if (isReceta) "receta" else "comentario"
        val botonCancelar = binding.btnVolver

        botonCancelar.setOnClickListener {
            if (!navController.popBackStack()) {
                finish() // Si no hay destinos en el stack de navegación, finaliza la actividad
            }
        }
        val bundle = Bundle().apply {
            putString("objectType", objectTypeToDisplay)
        }
        navController.navigate(R.id.pendingObjectFragment, bundle)


    }

}