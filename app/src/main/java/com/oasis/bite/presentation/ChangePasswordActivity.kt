package com.oasis.bite.presentation

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
import com.oasis.bite.R
import com.oasis.bite.ResetPasswordActivity
import com.oasis.bite.presentation.viewmodel.UsersViewModel
import com.oasis.bite.presentation.viewmodel.UsersViewModelFactory
import kotlinx.coroutines.launch

class ChangePasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = UsersViewModelFactory(applicationContext)
        val viewModel = ViewModelProvider(this, factory).get(UsersViewModel::class.java)
        setContentView(R.layout.activity_change_password)
        supportActionBar?.hide()

        val email = intent.getStringExtra("email")
        val oldpassword = findViewById<EditText>(R.id.passwordEditText0)
        val newpassword1 = findViewById<EditText>(R.id.passwordEditText)
        val newpassword2 = findViewById<EditText>(R.id.passwordEditText2)
        val submitButton = findViewById<Button>(R.id.continueButton)
        val botonCancelar = findViewById<TextView>(R.id.cancelText)

        botonCancelar.setOnClickListener {
            finish()
        }

        submitButton.setOnClickListener {
            val old = oldpassword.text.toString()
            val newpass1 = newpassword1.text.toString()
            val newpass2 = newpassword2.text.toString()

            if (newpass1.length < 6) {
                newpassword1.error = "Debe tener al menos 6 caracteres"
            } else if (newpass1 != newpass2) {
                newpassword2.error = "Las contraseñas no coinciden"
            } else {
                lifecycleScope.launch {
                    val response = viewModel.updatePassword(email.toString(),old, newpass1)
                    if (response.isSuccessful) {
                        Toast.makeText(this@ChangePasswordActivity, "Contraseña actualizada", Toast.LENGTH_SHORT).show()
                        showCustomNoInternetDialog()
                        // volver al login
                    } else {
                        Toast.makeText(this@ChangePasswordActivity, "Los datos son incorrectos", Toast.LENGTH_SHORT).show()
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