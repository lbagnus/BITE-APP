package com.oasis.bite

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        supportActionBar?.hide()

        // Verificar conexión
        if (!isInternetAvailable(this)) {
            showCustomNoInternetDialog()
        }

        // Referencias
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val resetButton = findViewById<Button>(R.id.continueButton)

        resetButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                emailEditText.error = "El campo no puede estar vacío"
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.error = "Formato de correo inválido"
            } else {
                Toast.makeText(this, "Se envió un correo de recuperación a $email", Toast.LENGTH_LONG).show()
                // Lógica de envío real acá
            }
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null
    }

    private fun showCustomNoInternetDialog() {
        val inflater = LayoutInflater.from(this)
        val customView = inflater.inflate(R.layout.popup_no_internet, null)

        val closeButton: Button = customView.findViewById(R.id.closeButton)

        val dialog = AlertDialog.Builder(this)
            .setView(customView)
            .setCancelable(false)
            .create()

        closeButton.setOnClickListener {
            finish()
        }

        dialog.show()
    }
}
