package com.oasis.bite

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.oasis.bite.databinding.ActivityComentarioBinding
import com.oasis.bite.databinding.FragmentRecetaBinding
import com.oasis.bite.presentation.viewmodel.RecetaViewModel
import com.oasis.bite.presentation.viewmodel.RecetaViewModelFactory

class ComentarioActivity : AppCompatActivity() {
    //tiene q traer la info de la receta, el id, el titulo y por quien esta hecha
    private var _binding: ActivityComentarioBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecetaViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comentario)
        Log.d("ComentarioActivity333", "ENTRÓ EN onCreate")

        _binding = ActivityComentarioBinding.inflate(layoutInflater)
        val factory = RecetaViewModelFactory(applicationContext)
        viewModel = ViewModelProvider(this, factory).get(RecetaViewModel::class.java)
        setContentView(binding.root)
        supportActionBar?.hide()

        val ratingBar = binding.ratingBar
        var score = 0  // Esto te da el valor seleccionado
        val recetaAutor = intent.getStringExtra("recetaAutor") ?: return
        val email = intent.getStringExtra("usuarioEmail") ?: return
        val recetaId = intent.getIntExtra("recetaId", -1)
        if (recetaId == -1) return
        val recetaTitulo = intent.getStringExtra("tituloReceta") ?: return
        val titulo: EditText = findViewById(R.id.titulobox)
        val comentario: EditText = findViewById(R.id.titulobox2)
        val botonAgregar = binding.AgregarText
        val botonCancelar = binding.cancelText
        botonAgregar.isEnabled = false
        Log.d("comentarioActivity222", "recetaID: $recetaId")
        binding.recetaAutor.text = recetaAutor
        binding.recetaTitulo.text = recetaTitulo
        botonCancelar.setOnClickListener {
            finish()
        }

        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            botonAgregar.isEnabled = rating > 0
        }

        botonAgregar.setOnClickListener {
            score = ratingBar.rating.toInt()
            val newtitulo = titulo.text.toString()
            val newcomentario = comentario.text.toString()
            viewModel.agregarComentario(email,  newtitulo, newcomentario, score, recetaId,)
            showCustomNoInternetDialog()

        }

    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
    private fun showCustomNoInternetDialog() {
        val inflater = LayoutInflater.from(this)
        val customView = inflater.inflate(R.layout.popup_comentario, null)

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