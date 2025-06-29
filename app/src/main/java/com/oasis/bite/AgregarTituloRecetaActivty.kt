package com.oasis.bite

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
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

class AgregarTituloRecetaActivty: AppCompatActivity() {
    private var _binding: ActivityAgregarTituloRecetaBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecetaViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAgregarTituloRecetaBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(RecetaViewModel::class.java)
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
            viewModel.verificarSiRecetaExiste(
                inputTitulo.text.toString(),
                usuarioUserName.toString()
            ) { idReceta ->
                if (idReceta != 0) {
                    showCustomNoInternetDialog{ quiereEditar ->
                        val intent = Intent(this, AgregarRecetaActivity::class.java).apply {
                            putExtra("tituloReceta", titulo)
                            putExtra("usuarioEmail", usuarioEmail)
                            putExtra("isEditando", quiereEditar)
                            putExtra("idReceta", idReceta.toString())
                            putExtra("reemplaza", true)// true o false según decisión
                        }
                        startActivity(intent)
                        finish()
                    }
                } else {
                    val intent = Intent(this, AgregarRecetaActivity::class.java)
                    intent.putExtra("tituloReceta", titulo)
                    intent.putExtra("usuarioEmail", usuarioEmail)
                    intent.putExtra("isEditando", false)
                    intent.putExtra("reemplaza", false)
                    intent.putExtra("idReceta", idReceta.toString())
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

        dialog.show()
    }
}



