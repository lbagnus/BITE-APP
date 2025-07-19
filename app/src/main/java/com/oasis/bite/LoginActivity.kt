package com.oasis.bite

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.oasis.bite.presentation.viewmodel.UsersViewModel
import com.oasis.bite.MainActivity
import com.oasis.bite.presentation.viewmodel.UsersViewModelFactory
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ocultar la ActionBar
        supportActionBar?.hide()

        setContentView(R.layout.activity_login)

        // Usar Factory personalizado
        val factory = UsersViewModelFactory(applicationContext)
        val viewModel = ViewModelProvider(this, factory).get(UsersViewModel::class.java)
// --- Variables para controlar el estado de la sesión y la conexión ---
        var hasLocalSession = false // Variable para guardar el estado de la sesión local
        var isInternetConnected = isInternetAvailable(this) // Verificar internet al inicio

        // Observador para verificar si hay sesión local
        viewModel.localSessionExists.observe(this) { localSessionExists ->
            if (localSessionExists) {
                val intent = Intent(this, MainActivity::class.java)
                viewModel.usuarioLogueado.value?.let { user ->
                    intent.putExtra("username", user.username)
                    val gson = Gson()
                    intent.putExtra("usuario_json", gson.toJson(user))
                    intent.putExtra("rolUsuario", user.role.toString())
                    Log.d("rolusuario", user.role.toString())
                }
                startActivity(intent)
                finish() // Cierra LoginActivity después de redirigir
            } else {
                // No hay sesión local, AHORA verificamos internet para mostrar el diálogo o el formulario
                if (!isInternetConnected) {
                    // No hay sesión local Y no hay internet: NO puede proceder.
                    showCustomNoInternetDialog(canProceedOffline = false)
                } else {
                    // No hay sesión local, pero SÍ hay internet: Mostrar el formulario de login.
                    findViewById<View>(R.id.loginLayout).visibility = View.VISIBLE // Asegurarse que el layout de login es visible
                }
            }
        }
        // Verificar si ya hay sesión iniciada
        viewModel.inicializarSesion()

        val loginButton: Button = findViewById(R.id.loginButton)
        val emailEditText: EditText = findViewById(R.id.emailEditText)
        val passwordEditText: EditText = findViewById(R.id.passwordEditText)
        val registerButton: TextView = findViewById(R.id.registerText)
        val invitadoButton: TextView = findViewById(R.id.guestText)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            // Solo intentar login si hay internet, o si hay sesión local (aunque en este caso ya habríamos redirigido)
            if (isInternetConnected || hasLocalSession) { // hasLocalSession para el caso de reintentar si se pierde la conexión
                viewModel.login(email, password)
            } else {
                // Si no hay internet y no hay sesión local, no tiene sentido intentar login
                Toast.makeText(this, "No hay conexión a internet para iniciar sesión.", Toast.LENGTH_LONG).show()
            }
        }

        invitadoButton.setOnClickListener {
            if (isInternetConnected || hasLocalSession) { // hasLocalSession para el caso de reintentar si se pierde la conexión
                showGuest(true)
                viewModel.login("visit@gmail.com", "1111")
            }else{
                showGuestNoWifi(true)
            }
        }

        registerButton.setOnClickListener {
            showRegister(true)
        }

        viewModel.usuarioLogueado.observe(this) { usuario ->
            if (usuario != null) {
                Toast.makeText(this, "Bienvenido ${usuario.username}", Toast.LENGTH_SHORT).show()
                val gson = Gson()
                val usuarioJson = gson.toJson(usuario)
                val sharedPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                sharedPrefs.edit().putString("usuario_logueado", usuarioJson).apply()

                val nombreCompleto = "${usuario.firstName} ${usuario.lastName}"
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("emailUsuario", usuario.email)
                intent.putExtra("nombreUsuario", nombreCompleto)
                intent.putExtra("nombre", usuario.firstName + " " + usuario.lastName)
                intent.putExtra("rolUsuario", usuario.role.name)

                Log.d("EMAIL LOGIN", usuario.email)
                startActivity(intent)
                finish()

            }
        }

        viewModel.mensajeError.observe(this) { mensaje ->
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
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
    private fun showCustomNoInternetDialog(canProceedOffline: Boolean) {
        val inflater = LayoutInflater.from(this)
        val customView = inflater.inflate(R.layout.popup_no_internet, null)

        val messageTextView: TextView = customView.findViewById(R.id.popupMessage)
        val closeButton: Button = customView.findViewById(R.id.closeButton)

        messageTextView.text = if (canProceedOffline) {
            "No hay conexión a internet. Puedes usar la aplicación sin conexión."
        } else {
            "No hay conexión a internet. No puedes iniciar sesión sin conexión."
        }


        val dialog = AlertDialog.Builder(this)
            .setView(customView)
            .setCancelable(false)
            .create()

        closeButton.setOnClickListener {
            if (canProceedOffline) {
                // Si puede proceder offline, simplemente cierra el diálogo
                dialog.dismiss()
            } else {
                // Si no puede proceder offline, cierra la actividad
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showRegister(canProceedOffline: Boolean) {
        val inflater = LayoutInflater.from(this)
        val customView = inflater.inflate(R.layout.pop_up_registro, null)

        val closeButton: Button = customView.findViewById(R.id.closeButton)


        val dialog = AlertDialog.Builder(this)
            .setView(customView)
            .setCancelable(false)
            .create()

        closeButton.setOnClickListener {
            if (canProceedOffline) {
                // Si puede proceder offline, simplemente cierra el diálogo
                dialog.dismiss()
            } else {
                // Si no puede proceder offline, cierra la actividad
                finish()
            }
        }

        dialog.show()
    }

    private fun showGuestNoWifi(canProceedOffline: Boolean) {
        val inflater = LayoutInflater.from(this)
        val customView = inflater.inflate(R.layout.popup_guest_nowifi, null)

        val closeButton: Button = customView.findViewById(R.id.closeButton)


        val dialog = AlertDialog.Builder(this)
            .setView(customView)
            .setCancelable(false)
            .create()

        closeButton.setOnClickListener {
            if (canProceedOffline) {
                // Si puede proceder offline, simplemente cierra el diálogo
                dialog.dismiss()
            } else {
                // Si no puede proceder offline, cierra la actividad
                finish()
            }
        }

        dialog.show()
    }

    private fun showGuest(canProceedOffline: Boolean) {
        val inflater = LayoutInflater.from(this)
        val customView = inflater.inflate(R.layout.popup_ingresa_guest, null)

        val closeButton: Button = customView.findViewById(R.id.closeButton)


        val dialog = AlertDialog.Builder(this)
            .setView(customView)
            .setCancelable(false)
            .create()

        closeButton.setOnClickListener {
            if (canProceedOffline) {
                // Si puede proceder offline, simplemente cierra el diálogo
                dialog.dismiss()
            } else {
                // Si no puede proceder offline, cierra la actividad
                finish()
            }
        }

        dialog.show()
    }
}
