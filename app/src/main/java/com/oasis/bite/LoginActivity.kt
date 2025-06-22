package com.oasis.bite

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.oasis.bite.presentation.viewmodel.UsersViewModel
import com.oasis.bite.MainActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ocultar la ActionBar
        supportActionBar?.hide()

        setContentView(R.layout.activity_login)

        val viewModel = ViewModelProvider(this).get(UsersViewModel::class.java);
        val loginButton: Button = findViewById(R.id.loginButton)
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            viewModel.login(email, password)
        }

        viewModel.usuarioLogueado.observe(this) { usuario ->
            if (usuario != null) {
                Toast.makeText(this, "Bienvenido ${usuario.username}", Toast.LENGTH_SHORT).show()
                val gson = Gson()
                val usuarioJson = gson.toJson(usuario)
                val sharedPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().putString("usuario_logueado", usuarioJson).apply()


                // Ir al MainActivity
                val intent = Intent(this, MainActivity::class.java)
                Log.d("usuarioLogueado??", usuarioJson)
                intent.putExtra("username", usuario.username) // opcional
                startActivity(intent)
                finish() // Cierra la LoginActivity para que no se pueda volver con el botón atrás

            }
        }

        viewModel.mensajeError.observe(this) { mensaje ->
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        }


        // Verificar si hay conexión a internet al iniciar la actividad
        if (!isInternetAvailable(this)) {
            showCustomNoInternetDialog()
        }

        // Vincular el TextView de "¿Olvidaste tu contraseña?" y agregarle comportamiento de link
        val forgotPasswordText: TextView = findViewById(R.id.forgotPasswordText)
        forgotPasswordText.paintFlags = forgotPasswordText.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        forgotPasswordText.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    // Función para verificar la conexión a Internet
    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    // Mostrar el popup personalizado si no hay internet
    private fun showCustomNoInternetDialog() {
        val inflater = LayoutInflater.from(this)
        val customView = inflater.inflate(R.layout.popup_no_internet, null)

        // Acceder a los elementos del layout personalizado
        val messageTextView: TextView = customView.findViewById(R.id.popupMessage)
        val closeButton: Button = customView.findViewById(R.id.closeButton)

        // Crear el AlertDialog
        val dialog = AlertDialog.Builder(this)
            .setView(customView)
            .setCancelable(false)  // No permitir que se cierre tocando fuera del popup
            .create()

        // Acción del botón "Cerrar"
        closeButton.setOnClickListener {
            finish()  // Cierra la actividad si el usuario decide salir
        }

        // Mostrar el popup
        dialog.show()
    }
}
