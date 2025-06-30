package com.oasis.bite
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.oasis.bite.databinding.ActivityMainBinding
import com.oasis.bite.databinding.FiltroPopupBinding
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.oasis.bite.presentation.viewmodel.UsersViewModel
import com.oasis.bite.presentation.viewmodel.UsersViewModelFactory


class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        val factory = UsersViewModelFactory(applicationContext)
        val userViewModel = ViewModelProvider(this, factory).get(UsersViewModel::class.java)

        val cerrarSesionView = findViewById<TextView>(R.id.opcionCerrarSesion)
        cerrarSesionView.setOnClickListener {
            // Borrar preferencias guardadas
            val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()

            // Volver al login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            // Hacer logout de base local.
            userViewModel.logout()
        }

        // Obtener el DrawerLayout
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)

        // Botón del menú (en tu custom_toolbar)
        val btnMenu = findViewById<View>(R.id.menuIcon)
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val btnCambiarContrasena = findViewById<TextView>(R.id.opcionCambiarContrasena)

        btnCambiarContrasena.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
        }

        val nombreUsuario = intent.getStringExtra("nombreUsuario") ?: "Usuario"
        findViewById<TextView>(R.id.saludoText)?.text = "¡Hola $nombreUsuario!"
        findViewById<TextView>(R.id.nombreUsuario)?.text = nombreUsuario

        val rolUsuario = intent.getStringExtra("rolUsuario") ?: "USER"
        val opcionAutorizacion = findViewById<TextView>(R.id.opcionAutorizacion)

        if (rolUsuario == "ADMIN") {
            opcionAutorizacion.visibility = View.VISIBLE
        }

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.recetaFragment -> {
                    // Ocultamos barra, frase y searchbar
                    findViewById<View>(R.id.toolbar).visibility = View.GONE
                    findViewById<View>(R.id.searchbar).visibility = View.GONE
                    findViewById<View>(R.id.btnAbrirPopup).visibility = View.GONE
                }
                else -> {
                    // Los mostramos nuevamente en el resto de pantallas
                    findViewById<View>(R.id.toolbar).visibility = View.VISIBLE
                    findViewById<View>(R.id.searchbar).visibility = View.VISIBLE
                    findViewById<View>(R.id.btnAbrirPopup).visibility = View.VISIBLE
                }
            }
        }
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.popBackStack(R.id.navigation_home, false)
                    true
                }
                R.id.navigation_dashboard -> {
                    navController.navigate(R.id.navigation_dashboard)
                    true
                }
                R.id.navigation_notifications -> {
                    navController.navigate(R.id.navigation_notifications)
                    true
                }
                else -> false
            }
        }


        binding.btnAbrirPopup.setOnClickListener {
            // Inflamos el binding del layout del popup
            val popupBinding = FiltroPopupBinding.inflate(layoutInflater)

            val dialog = AlertDialog.Builder(this)
                .setView(popupBinding.root)
                .setCancelable(true)
                .create()

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()

            // Ejemplo: si tenés un botón "Listo" en el popup, podés cerrarlo así:
            popupBinding.root.findViewById<View>(R.id.btnListo)?.setOnClickListener {
                dialog.dismiss()
            }
        }

        val searchView = binding.searchbar.searchView // Acceso correcto a SearchView
        searchView.clearFocus()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchText ->
                    Log.d("Busqueda", "Se buscó: $searchText (por Submit)")
                    // Crea un Bundle para pasar el argumento
                    val bundle = Bundle().apply {
                        putString("query", searchText) // "query" debe coincidir con el nombre del argumento en nav_graph.xml
                    }
                    // Navega al fragmento ResultadoBusqueda con el argumento
                    navController.navigate(R.id.ResultadoBusqueda, bundle)
                }
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        // Agregando el FocusChangeListener que discutimos antes, por si se quiere activar al perder el foco
        searchView.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val currentQuery = searchView.query?.toString()
                if (!currentQuery.isNullOrBlank()) {
                    Log.d("Busqueda", "Se buscó: $currentQuery (por perder Focus)")
                    // Opcional: navegar al ResultadoBusqueda también al perder el foco si la consulta no está vacía
                    val bundle = Bundle().apply {
                        putString("query", currentQuery)
                    }
                    navController.navigate(R.id.ResultadoBusqueda, bundle)
                }
            }
        }


    }


    }

