package com.oasis.bite
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.oasis.bite.databinding.ActivityMainBinding
import com.oasis.bite.databinding.FiltroPopupBinding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.google.gson.Gson
import com.oasis.bite.domain.models.User
import com.oasis.bite.presentation.ChangePasswordActivity
import com.oasis.bite.presentation.viewmodel.UsersViewModel
import com.oasis.bite.presentation.viewmodel.UsersViewModelFactory
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.google.android.material.chip.Chip
import com.oasis.bite.presentation.viewmodel.FiltroViewModel


class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    val filtroViewModel: FiltroViewModel by viewModels()


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

        val usuario = getUsuarioLogueado(this)

        // Obtener el DrawerLayout
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawerLayout)

        // Botón del menú (en tu custom_toolbar)
        val btnMenu = findViewById<View>(R.id.menuIcon)
        btnMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val btnCambiarContrasena = findViewById<TextView>(R.id.opcionCambiarContrasena)


        btnCambiarContrasena.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            intent.putExtra("email", usuario.email)
            startActivity(intent)
        }

        val nombreUsuario = intent.getStringExtra("username") ?: "Usuario"
        val userEmail = usuario.email
        Log.d("nombreUsuario", nombreUsuario.toString())
        findViewById<TextView>(R.id.saludoText)?.text = "¡Hola $nombreUsuario!"
        findViewById<TextView>(R.id.nombreUsuario)?.text = nombreUsuario

        val rolUsuario = intent.getStringExtra("rolUsuario")
        val opcionAutorizacion = findViewById<TextView>(R.id.opcionAutorizacion)
        val opcionMisCalculos = findViewById<TextView>(R.id.opcionMisCalculos)
        val wifiSwitch = findViewById<Switch>(R.id.switchWifi)
        if (rolUsuario == "ADMIN") {
            Log.d("EMAIL ADMIN", rolUsuario.toString())
            opcionAutorizacion.visibility = View.VISIBLE
        }

        if(rolUsuario == "GUEST"){
            opcionMisCalculos.visibility = View.GONE
            btnCambiarContrasena.visibility = View.GONE
            wifiSwitch.isEnabled = false
        }

        opcionAutorizacion.setOnClickListener {
            val intent = Intent(this, AutorizacionBoxesActivity::class.java)
            startActivity(intent)
        }

        opcionMisCalculos.setOnClickListener {
            val intent = Intent(this, MisCalculosActivity::class.java)
            startActivity(intent)
        }


        userViewModel.loadWifiPreference(userEmail)
        Log.d("EMAIL USUAERIO", userEmail)
        userViewModel.wifiPreference.observe(this) { isEnabled ->
            // Se usa setOnCheckedChangeListener(null) para evitar que se dispare
            // el listener cuando actualizamos el estado programáticamente.
            wifiSwitch.setOnCheckedChangeListener(null) // Desactiva temporalmente el listener
            wifiSwitch.isChecked = isEnabled // Actualiza el estado del Switch en la UI
            wifiSwitch.setOnCheckedChangeListener { _, checked ->
                // Este listener se dispara cuando el usuario CAMBIA el Switch
                Log.d("SettingsFragment", "Switch cambiado por el usuario a: $checked")
                userViewModel.toggleWifiPreference(userEmail.toString()) // Envía el cambio a la API
            }
        }

        // Observar el estado de la actualización para dar feedback al usuario
        userViewModel.preferenceUpdateStatus.observe(this) { isSuccess ->
            if (isSuccess) {
                // El estado del LiveData 'wifiPreference' ya se habrá actualizado en el ViewModel
                // por lo que el switch en la UI se actualizará automáticamente.
                Toast.makeText(this, "Preferencia guardada.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error al guardar preferencia.", Toast.LENGTH_SHORT).show()
                // Opcional: Revertir el estado visual del switch si la API falla
                // wifiSwitch.isChecked = !(wifiSwitch.isChecked)
            }
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
                R.id.navigation_notifications, R.id.navigation_dashboard, R.id.CalculadoraFragment-> {
                // Ocultamos frase y searchbar
                    findViewById<View>(R.id.toolbar).visibility = View.VISIBLE
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
                    if(rolUsuario != "GUEST"){
                    navController.navigate(R.id.navigation_dashboard)
                    true
                    } else{false}
                }
                R.id.navigation_notifications -> {
                    if(rolUsuario != "GUEST"){
                    navController.navigate(R.id.navigation_notifications)
                    true}else{false}
                }
                else -> false
            }
        }


        // FILTRO
        binding.btnAbrirPopup.setOnClickListener {
            // Inflamos el binding del layout del popup
            val popupBinding = FiltroPopupBinding.inflate(layoutInflater)
            val colorSeleccionado = ContextCompat.getColor(this, R.color.amarillo)
            val colorNormal = ContextCompat.getColor(this, android.R.color.transparent)
            val textoActivo = ContextCompat.getColor(this, android.R.color.black)
            val textoInactivo = ContextCompat.getColor(this, R.color.white)


            val dialog = AlertDialog.Builder(this)
                .setView(popupBinding.root)
                .setCancelable(true)
                .create()

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


            // --- AQUI CARGAMOS LOS FILTROS GUARDADOS PARA MOSTRARLOS ---
            filtroViewModel.filtros.value?.let { filtroGuardado ->
                // Ingredientes que incluye
                filtroGuardado.incluye.forEach { texto ->
                    val chip = Chip(this).apply {
                        this.text = texto
                        isCloseIconVisible = true
                        setOnCloseIconClickListener { popupBinding.chipGroup.removeView(this) }
                    }
                    popupBinding.chipGroup.addView(chip)
                }

                // Ingredientes que excluye
                filtroGuardado.excluye.forEach { texto ->
                    val chip = Chip(this).apply {
                        this.text = texto
                        isCloseIconVisible = true
                        setOnCloseIconClickListener { popupBinding.chipGroup2.removeView(this) }
                    }
                    popupBinding.chipGroup2.addView(chip)
                }

                // Cocinero (username), solo uno
                filtroGuardado.username?.let { usuario ->
                    val chip = Chip(this).apply {
                        this.text = usuario
                        isCloseIconVisible = true
                        setOnCloseIconClickListener { popupBinding.chipGroup3.removeView(this) }
                    }
                    popupBinding.chipGroup3.addView(chip)
                }

                // Estado botones orden
                if (filtroGuardado.direction == "desc") {  // si es "desc" entonces botón "Más reciente"
                    popupBinding.btnFiltro.isChecked = true
                    popupBinding.btnFiltroviejo.isChecked = false
                    popupBinding.btnFiltro.setBackgroundColor(colorSeleccionado)
                    popupBinding.btnFiltro.setTextColor(textoActivo)
                    popupBinding.btnFiltroviejo.setBackgroundColor(colorNormal)
                    popupBinding.btnFiltroviejo.setTextColor(textoInactivo)
                } else { // "asc" es botón "Más antiguo"
                    popupBinding.btnFiltroviejo.isChecked = true
                    popupBinding.btnFiltro.isChecked = false
                    popupBinding.btnFiltroviejo.setBackgroundColor(colorSeleccionado)
                    popupBinding.btnFiltroviejo.setTextColor(textoActivo)
                    popupBinding.btnFiltro.setBackgroundColor(colorNormal)
                    popupBinding.btnFiltro.setTextColor(textoInactivo)
                }
            }
            dialog.show()

            //cocineros
            popupBinding.tagInput3.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val texto = (v as? EditText)?.text?.toString()?.trim()
                    if (!texto.isNullOrEmpty()) {
                        if (popupBinding.chipGroup3.childCount == 0) {  // Solo permite uno
                            val chip = Chip(this).apply {
                                text = texto
                                isCloseIconVisible = true
                                setOnCloseIconClickListener {
                                    popupBinding.chipGroup3.removeView(this)
                                }
                            }
                            popupBinding.chipGroup3.addView(chip)
                            popupBinding.tagInput3.text?.clear()
                        } else {
                            Toast.makeText(this, "Solo se puede buscar un cocinero.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    true
                } else false
            }

            // Ingredientes que contiene
            popupBinding.tagInput.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val texto = (v as? EditText)?.text?.toString()?.trim()
                    if (!texto.isNullOrEmpty()) {
                        val chip = Chip(this).apply {
                            text = texto
                            isCloseIconVisible = true
                            setOnCloseIconClickListener {
                                popupBinding.chipGroup.removeView(this)
                            }
                        }
                        popupBinding.chipGroup.addView(chip)
                        popupBinding.tagInput.text?.clear()
                    }
                    true
                } else false
            }

            // Ingredientes que NO contiene
            popupBinding.tagInput2.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val texto = (v as? EditText)?.text?.toString()?.trim()
                    if (!texto.isNullOrEmpty()) {
                        val chip = Chip(this).apply {
                            text = texto
                            isCloseIconVisible = true
                            setOnCloseIconClickListener {
                                popupBinding.chipGroup2.removeView(this)
                            }
                        }
                        popupBinding.chipGroup2.addView(chip)
                        popupBinding.tagInput2.text?.clear()
                    }
                    true
                } else false
            }

            // Botones de orden
            popupBinding.btnFiltro.setOnClickListener {
                popupBinding.btnFiltro.isChecked = true
                popupBinding.btnFiltroviejo.isChecked = false

                popupBinding.btnFiltro.setBackgroundColor(colorSeleccionado)
                popupBinding.btnFiltroviejo.setBackgroundColor(colorNormal)
                popupBinding.btnFiltro.setTextColor(textoActivo)
                popupBinding.btnFiltroviejo.setTextColor(textoInactivo)
            }

            popupBinding.btnFiltroviejo.setOnClickListener {
                popupBinding.btnFiltro.isChecked = false
                popupBinding.btnFiltroviejo.isChecked = true

                popupBinding.btnFiltroviejo.setBackgroundColor(colorSeleccionado)
                popupBinding.btnFiltro.setBackgroundColor(colorNormal)

                popupBinding.btnFiltroviejo.setTextColor(textoActivo)
                popupBinding.btnFiltro.setTextColor(textoInactivo)
            }

            popupBinding.btnListo.setOnClickListener {
                val incluye = (0 until popupBinding.chipGroup.childCount).mapNotNull {
                    val chip = popupBinding.chipGroup.getChildAt(it) as? Chip
                    chip?.text?.toString()
                }

                val excluye = (0 until popupBinding.chipGroup2.childCount).mapNotNull {
                    val chip = popupBinding.chipGroup2.getChildAt(it) as? Chip
                    chip?.text?.toString()
                }

                val username = (popupBinding.chipGroup3.getChildAt(0) as? Chip)?.text?.toString()
                val ordenReciente = popupBinding.btnFiltro.isChecked
                val ordenAntiguo = popupBinding.btnFiltroviejo.isChecked

                val direction = when {
                    ordenReciente -> "desc"
                    ordenAntiguo -> "asc"
                    else -> "desc" // un default si ninguno está seleccionado
                }

                // Validar que haya al menos un filtro activo
                val hayFiltros = incluye.isNotEmpty() || excluye.isNotEmpty() || ordenReciente || ordenAntiguo || !username.isNullOrEmpty()

                if (hayFiltros) {
                    val filtro = FiltroViewModel.Filtro(incluye, excluye, username, "newest",direction)
                    filtroViewModel.filtros.value = filtro
                    Log.d("FiltroViewModel", "Seteando filtros: $filtro")
                    dialog.dismiss()
                    val navController = findNavController(R.id.nav_host_fragment_activity_main)
                    if (navController.currentDestination?.id != R.id.ResultadoBusqueda) {
                        navController.navigate(R.id.ResultadoBusqueda)}

                } else {
                    Toast.makeText(this, "Por favor, seleccioná al menos un filtro.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
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
    fun getUsuarioLogueado(context: Context): User {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString("usuario_logueado", null)
        return json.let { Gson().fromJson(it, User::class.java) }
    }


}

