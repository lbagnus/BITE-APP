package com.oasis.bite

import android.os.Bundle
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.oasis.bite.presentation.viewmodel.RecetaViewModel

class ComentarioActivity : AppCompatActivity() {
    //tiene q traer la info de la receta, el id, el titulo y por quien esta hecha
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comentario)
        val viewModel = ViewModelProvider(this).get(RecetaViewModel::class.java);
        supportActionBar?.hide()

        val ratingBar = findViewById<RatingBar>(R.id.ratingBar)
        val score = ratingBar.rating  // Esto te da el valor seleccionado
        val recetaAutor = findViewById<TextView>(R.id.recetaAutor)
        val botonCancelar = findViewById<TextView>(R.id.cancelText)
        val titulo = findViewById<EditText>(R.id.titulobox)
        val comentario = findViewById<EditText>(R.id.titulobox2)
        val botonAgregar = findViewById<TextView>(R.id.AgregarText)
        val email = intent.getStringExtra("email") ?: return
        val recetaId = intent.getStringExtra("recetaId") ?: return
        botonAgregar.isEnabled = false
        botonCancelar.setOnClickListener {
            finish()
        }
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            botonAgregar.isEnabled = rating >0
        }

        botonAgregar.setOnClickListener {
            viewModel.agregarComentario(email, recetaId, titulo.toString(),
                comentario.toString(), score)
        }

    }
}