package com.oasis.bite

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.oasis.bite.databinding.ActivityAgregarRecetaBinding
import com.oasis.bite.databinding.ActivityAgregarTituloRecetaBinding
import com.oasis.bite.presentation.viewmodel.RecetaViewModel
import com.oasis.bite.presentation.viewmodel.RecetaViewModelFactory
import com.oasis.bite.presentation.viewmodel.UsersViewModel
import com.oasis.bite.presentation.viewmodel.UsersViewModelFactory

class AgregarTituloRecetaActivty: AppCompatActivity() {
    private var _binding: ActivityAgregarTituloRecetaBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecetaViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAgregarTituloRecetaBinding.inflate(layoutInflater)

        val factory = RecetaViewModelFactory(applicationContext)
        viewModel = ViewModelProvider(this, factory).get(RecetaViewModel::class.java)

        setContentView(binding.root)
        supportActionBar?.hide()

        val botonContinuar = binding.continuarButton
        val usuarioEmail = intent.getStringExtra("usuarioEmail")
        val usuarioUserName = intent.getStringExtra("usuarioUserName")
        val inputTitulo = binding.titulo

        botonContinuar.setOnClickListener setOnFocusChangeListener@{
            val titulo = inputTitulo.text.toString().trim()
            if (titulo.isEmpty()) {
                Toast.makeText(this, "Ingresá un título", Toast.LENGTH_SHORT).show()
                return@setOnFocusChangeListener
            }

            if (!isInternetAvailable(this)) {
                // 👉 Sin internet, ir directo a la siguiente actividad
                val intent = Intent(this, AgregarRecetaActivity::class.java).apply {
                    putExtra("tituloReceta", titulo)
                    putExtra("usuarioEmail", usuarioEmail)
                    putExtra("isEditando", false)
                    putExtra("reemplaza", false)
                    putExtra("idReceta", "0")
                }
                startActivity(intent)
                finish()
                return@setOnFocusChangeListener
            }

            // ✅ Con internet, verificar si la receta ya existe
            viewModel.verificarSiRecetaExiste(
                titulo,
                usuarioEmail.toString(),
            ) { idReceta ->
                if (idReceta != 0) {
                    showCustomNoInternetDialog { quiereEditar ->
                        val intent = Intent(this, AgregarRecetaActivity::class.java).apply {
                            putExtra("tituloReceta", titulo)
                            putExtra("usuarioEmail", usuarioEmail)
                            putExtra("isEditando", quiereEditar)
                            putExtra("idReceta", idReceta.toString())
                            putExtra("reemplaza", true)
                        }
                        startActivity(intent)
                        finish()
                    }
                } else {
                    val intent = Intent(this, AgregarRecetaActivity::class.java).apply {
                        putExtra("tituloReceta", titulo)
                        putExtra("usuarioEmail", usuarioEmail)
                        putExtra("isEditando", false)
                        putExtra("reemplaza", false)
                        putExtra("idReceta", idReceta.toString())
                    }
                    startActivity(intent)
                    finish()
                }
            }
        }

        binding.btnVolver.setOnClickListener {
                finish()
            }
        }
    
    private fun showCustomNoInternetDialog(onDecision: (Boolean) -> Unit) {
        val inflater = LayoutInflater.from(this)
        val customView = inflater.inflate(R.layout.popup_receta_existe, null)

        val messageTextView: TextView = customView.findViewById(R.id.popupMessage)
        val editarButton: Button = customView.findViewById(R.id.editarButton)
        val reemplazarButton: Button = customView.findViewById(R.id.reemplazarButton)
        val cerrar : ImageButton = customView.findViewById(R.id.btnClosePopup)
        val dialog = AlertDialog.Builder(this)
            .setView(customView)
            .setCancelable(false)
            .create()

        editarButton.setOnClickListener {
            dialog.dismiss()
            onDecision(true)  // true = EDITAR
        }

        reemplazarButton.setOnClickListener {
            dialog.dismiss()
            onDecision(false) // false = REEMPLAZAR
        }

        cerrar.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}




