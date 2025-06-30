package com.oasis.bite

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.oasis.bite.data.RetrofitInstance.apiService
import com.oasis.bite.presentation.viewmodel.UsersViewModel
import com.oasis.bite.presentation.viewmodel.UsersViewModelFactory
import kotlinx.coroutines.launch

class VerifyCodeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_code)

        val factory = UsersViewModelFactory(applicationContext)
        val viewModel = ViewModelProvider(this, factory).get(UsersViewModel::class.java)
        supportActionBar?.hide()
        if (!isInternetAvailable(this)) {
            showCustomNoInternetDialog()
        }
        val email = intent.getStringExtra("email") ?: return
        val continueButton = findViewById<Button>(R.id.continueButton)

        val digit1 = findViewById<EditText>(R.id.codeDigit1)
        val digit2 = findViewById<EditText>(R.id.codeDigit2)
        val digit3 = findViewById<EditText>(R.id.codeDigit3)
        val digit4 = findViewById<EditText>(R.id.codeDigit4)
        val digit5 = findViewById<EditText>(R.id.codeDigit5)
        val digit6 = findViewById<EditText>(R.id.codeDigit6)
        val botonCancelar = findViewById<TextView>(R.id.cancelText)

        botonCancelar.setOnClickListener {
            finish()
        }
        digit1.doOnTextChanged { text, _, _, _ ->
            if (text?.length == 1) digit2.requestFocus()
        }
        digit2.doOnTextChanged { text, _, _, _ ->
            if (text?.length == 1) digit3.requestFocus()
        }
        digit3.doOnTextChanged { text, _, _, _ ->
            if (text?.length == 1) digit4.requestFocus()
        }
        digit4.doOnTextChanged { text, _, _, _ ->
            if (text?.length == 1) digit5.requestFocus()
        }
        digit5.doOnTextChanged { text, _, _, _ ->
            if (text?.length == 1) digit6.requestFocus()
        }


        continueButton.setOnClickListener {
            val codigo = digit1.text.toString() + digit2.text.toString() + digit3.text.toString() + digit4.text.toString() + digit5.text.toString() + digit6.text.toString().trim()
            val errorText = findViewById<TextView>(R.id.errorText)
            if (codigo.isNotEmpty()) {
                lifecycleScope.launch {
                    try {
                        val response = viewModel.verify(email,codigo)
                        if (response.isSuccessful) {
                            startActivity(Intent(this@VerifyCodeActivity, ResetPasswordActivity::class.java)
                                .putExtra("email", email))
                            finish()
                        } else {
                            errorText.visibility = View.VISIBLE
                            errorText.text = "El correo no está registrado"
                        }
                    } catch (e: Exception) {
                        errorText.visibility = View.VISIBLE
                        errorText.text = "Error de red"
                    }
                }
            }else{
                Log.d("verificar codigo", codigo)
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