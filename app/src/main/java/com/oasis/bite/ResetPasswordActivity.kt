package com.oasis.bite

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.oasis.bite.presentation.viewmodel.UsersViewModel
import kotlinx.coroutines.launch

class ResetPasswordActivity : AppCompatActivity() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            val viewModel = ViewModelProvider(this).get(UsersViewModel::class.java);
            setContentView(R.layout.activity_reset_password)
            supportActionBar?.hide()

            val email = intent.getStringExtra("email") ?: return
            val password1 = findViewById<EditText>(R.id.passwordEditText)
            val password2 = findViewById<EditText>(R.id.passwordEditText2)
            val submitButton = findViewById<Button>(R.id.continueButton)
            val botonCancelar = findViewById<TextView>(R.id.cancelText)

            botonCancelar.setOnClickListener {
                finish()
            }
            submitButton.setOnClickListener {
                val pass1 = password1.text.toString()
                val pass2 = password2.text.toString()

                if (pass1.length < 6) {
                    password1.error = "Debe tener al menos 6 caracteres"
                } else if (pass1 != pass2) {
                    password2.error = "Las contraseñas no coinciden"
                } else {
                    lifecycleScope.launch {
                        val response = viewModel.resetPassword(email, pass1)
                        if (response.isSuccessful) {
                            Toast.makeText(this@ResetPasswordActivity, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                            showCustomNoInternetDialog()
                            // volver al login
                        } else {
                            Toast.makeText(this@ResetPasswordActivity, "Error al actualizar", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    private fun showCustomNoInternetDialog() {
        val inflater = LayoutInflater.from(this)
        val customView = inflater.inflate(R.layout.popup_contrasenia_cambiada, null)

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

