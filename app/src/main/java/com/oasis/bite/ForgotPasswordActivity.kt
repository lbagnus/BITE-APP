package com.oasis.bite

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.oasis.bite.presentation.viewmodel.UsersViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.oasis.bite.presentation.viewmodel.UsersViewModelFactory


class ForgotPasswordActivity : AppCompatActivity() {

    @SuppressLint("SoonBlockedPrivateApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val factory = UsersViewModelFactory(applicationContext)
        val viewModel = ViewModelProvider(this, factory).get(UsersViewModel::class.java)

        supportActionBar?.hide()

        // Verificar conexión
        if (!isInternetAvailable(this)) {
            showCustomNoInternetDialog()
        }

        // Referencias
        val emailEditText = findViewById<TextInputEditText>(R.id.emailEditText)
        val resetButton = findViewById<Button>(R.id.continueButton)
        val emailInputLayout = findViewById<TextInputLayout>(R.id.emailInputLayout)
        emailInputLayout.setEndIconTintList(
            ContextCompat.getColorStateList(this, R.color.brown)
        )

        val botonCancelar = findViewById<TextView>(R.id.cancelText)

        botonCancelar.setOnClickListener {
            finish()
        }
        resetButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val errorText = findViewById<TextView>(R.id.errorText)

            if (email.isEmpty()) {
                emailInputLayout.boxStrokeColor = Color.RED
                errorText.visibility = View.VISIBLE
                errorText.text = "El campo no puede estar vacío"

            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInputLayout.boxStrokeColor = Color.RED
                errorText.visibility = View.VISIBLE
                errorText.text = "Formato de correo inválido"

            } else {
                // Llamar al backend para validar si existe
                lifecycleScope.launch {
                    val errorText = findViewById<TextView>(R.id.errorText)
                        val response = withContext(Dispatchers.IO) {
                            viewModel.sendResetCode(email)
                        }

                        if (response.isSuccessful) {
                            startActivity(
                                Intent(this@ForgotPasswordActivity, VerifyCodeActivity::class.java)
                                    .putExtra("email", email)
                            )
                            finish()

                        } else {
                            emailInputLayout.boxStrokeColor = Color.RED
                            errorText.visibility = View.VISIBLE
                            errorText.text = "El correo no está registrado"

                        }

                }

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
