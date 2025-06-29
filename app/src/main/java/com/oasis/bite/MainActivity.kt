package com.oasis.bite
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.oasis.bite.databinding.ActivityMainBinding
import com.oasis.bite.databinding.FiltroPopupBinding
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding


class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()


        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.recetaFragment -> {
                    // Ocultamos barra, frase y searchbar
                    findViewById<View>(R.id.toolbar).visibility = View.GONE
                    findViewById<View>(R.id.fraseDestacada1).visibility = View.GONE
                    findViewById<View>(R.id.searchbar).visibility = View.GONE
                    findViewById<View>(R.id.btnAbrirPopup).visibility = View.GONE
                }
                else -> {
                    // Los mostramos nuevamente en el resto de pantallas
                    findViewById<View>(R.id.toolbar).visibility = View.VISIBLE
                    findViewById<View>(R.id.fraseDestacada1).visibility = View.VISIBLE
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

        val textoHtml = "Hoy es un <font color='#FFD700'>hermoso día</font>, para una <font color='#FFD700'>deliciosa comida</font>"
        binding.fraseDestacada1.text = Html.fromHtml(textoHtml, Html.FROM_HTML_MODE_LEGACY)

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
        val searchView = binding.searchbar.searchView
        searchView.clearFocus()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Acá se ejecuta cuando el usuario presiona "Enter" o el ícono de buscar
                query?.let {
                    // Podés usar el texto ingresado para buscar, filtrar, etc.
                    Log.d("Busqueda", "Se buscó: $query")
                }
                // Evita que se borre el texto
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Opcional: para mostrar resultados mientras se escribe
                return false
            }
        })


    }


    }

