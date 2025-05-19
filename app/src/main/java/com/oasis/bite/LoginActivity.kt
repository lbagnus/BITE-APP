package com.oasis.bite

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ocultar la ActionBar
        supportActionBar?.hide()

        setContentView(R.layout.activity_login)

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
