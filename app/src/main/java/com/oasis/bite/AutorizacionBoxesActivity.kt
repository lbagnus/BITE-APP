package com.oasis.bite

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AutorizacionBoxesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_autorizacion_boxes)

        supportActionBar?.hide()

        val autorizarReceta = findViewById<TextView>(R.id.opcionreceta)
        val autorizarComentario = findViewById<TextView>(R.id.opcioncomentario)
        val cancelar = findViewById<ImageButton>(R.id.cancelar)

        cancelar.setOnClickListener {
            finish()
        }
        autorizarReceta.setOnClickListener {
            val intent = Intent(this, AutorizarActivity::class.java)
            intent.putExtra("Receta", true)
            startActivity(intent)
        }
        autorizarComentario.setOnClickListener {
            val intent = Intent(this, AutorizarActivity::class.java)
            intent.putExtra("Receta", false)
            startActivity(intent)
        }

    }
}