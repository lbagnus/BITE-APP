package com.oasis.bite

import android.view.LayoutInflater
import com.oasis.bite.databinding.ActivityRecetaLocalDetailBinding
import com.oasis.bite.presentation.viewmodel.RecetaViewModel
import com.oasis.bite.presentation.viewmodel.RecetaViewModelFactory

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson // Para deserializar JSON de Ingredientes y Pasos
import com.google.gson.reflect.TypeToken // Para deserializar Listas con Gson
import com.oasis.bite.presentation.adapters.MediaAdapter

class RecetaLocalDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecetaLocalDetailBinding
    private lateinit var viewModel: RecetaViewModel // Reutilizamos RecetaViewModel

    // Vistas para los detalles de la receta
    private lateinit var recetaTitulo: TextView
    private lateinit var recetaAutor: TextView
    private lateinit var recetaDescripcion: TextView
    private lateinit var porciones: TextView
    private lateinit var tiempo: TextView
    private lateinit var dificultad: TextView
    private lateinit var imagenRecetaPrincipal: ImageView
    private lateinit var layoutIngredientes: LinearLayout // Contenedor para ingredientes
    private lateinit var layoutPasos: LinearLayout // Contenedor para pasos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecetaLocalDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        // Inicializar ViewModel
        val factory = RecetaViewModelFactory(applicationContext)
        viewModel = ViewModelProvider(this, factory).get(RecetaViewModel::class.java)

        // Inicializar vistas del layout
        val botonVolver = binding.btnVolver
        recetaTitulo = binding.recetaTitulo
        recetaAutor = binding.recetaAutor
        recetaDescripcion = binding.recetaDescripcion
        porciones = binding.porciones
        tiempo = binding.tiempo
        dificultad = binding.dificultad
        imagenRecetaPrincipal = binding.imagenRecetaPrincipal
        layoutIngredientes = binding.layoutIngredientes
        layoutPasos = binding.layoutPasos


        botonVolver.setOnClickListener {
            finish() // Cierra esta Activity y vuelve a MisCalculosActivity
        }

        // Obtener el ID LOCAL de la receta desde la Intent
        val localRecetaId = intent.getIntExtra("localRecetaId", -1)
        Log.d("RecetaLocalDetail", "ID Local recibido: $localRecetaId")

        if (localRecetaId != -1) {
            viewModel.cargarRecetaLocal(localRecetaId) // Llama al método del ViewModel
        } else {
            Toast.makeText(this, "Error: ID de receta local no proporcionado.", Toast.LENGTH_LONG).show()
            finish() // Cierra la actividad si no hay ID
            return
        }

        // Observar la receta cargada del ViewModel
        viewModel.receta.observe(this) { receta ->
            receta?.let { loadedReceta ->
                Log.d("RecetaLocalDetail", "Receta cargada en UI: ${loadedReceta.nombre}")
                // Asignar datos a las vistas
                recetaTitulo.text = loadedReceta.nombre
                recetaAutor.text = "Por ${loadedReceta.username}"
                recetaDescripcion.text = loadedReceta.descripcion
                porciones.text = "${loadedReceta.porciones} porciones"
                tiempo.text = loadedReceta.tiempo
                dificultad.text = loadedReceta.dificultad.label // Asumiendo que Dificultad tiene un 'label'

                loadedReceta.imagen?.let { imageUrl ->
                    Glide.with(this)
                        .load(imageUrl)
                        .into(imagenRecetaPrincipal)
                    imagenRecetaPrincipal.visibility = View.VISIBLE
                } ?: run {
                    imagenRecetaPrincipal.visibility = View.GONE
                }

                // Inflar ingredientes dinámicamente
                layoutIngredientes.removeAllViews()
                loadedReceta.ingredientes.forEach { ingrediente ->
                    val ingredienteView = LayoutInflater.from(this).inflate(R.layout.item_ingrediente, layoutIngredientes, false)
                    val nombre = ingredienteView.findViewById<TextView>(R.id.txtNombreIngrediente)
                    val cantidad = ingredienteView.findViewById<TextView>(R.id.txtCantidad)
                    val unidad = ingredienteView.findViewById<TextView>(R.id.txtUnidad)

                    nombre.text = ingrediente.nombre
                    cantidad.text = ingrediente.cantidad.toString()
                    unidad.text = ingrediente.unidad
                    layoutIngredientes.addView(ingredienteView)
                }

                // Inflar pasos dinámicamente
                layoutPasos.removeAllViews()
                loadedReceta.pasos.forEachIndexed { index, paso ->
                    val pasoView = LayoutInflater.from(this).inflate(R.layout.item_paso_receta, layoutPasos, false)
                    val textoPaso = pasoView.findViewById<TextView>(R.id.txtPaso)

                    textoPaso.text = " ${paso.numeroDePaso} ${paso.contenido}"

                    val recyclerMediaPaso = pasoView.findViewById<RecyclerView>(R.id.recyclerMediaPaso)

                    val listaMultimediaPaso = paso.imagenesPasos.orEmpty()
                    if (listaMultimediaPaso.isNotEmpty()) {
                        val adapter = MediaAdapter(listaMultimediaPaso)
                        recyclerMediaPaso.adapter = adapter
                        recyclerMediaPaso.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                        recyclerMediaPaso.visibility = View.VISIBLE
                    } else {
                        recyclerMediaPaso.visibility = View.GONE
                    }
                    layoutPasos.addView(pasoView)
                }

            } ?: run {
                Toast.makeText(this, "Error al cargar los detalles de la receta.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}