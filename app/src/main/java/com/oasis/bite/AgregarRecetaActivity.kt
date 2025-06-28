package com.oasis.bite

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.oasis.bite.databinding.ActivityAgregarRecetaBinding
import com.oasis.bite.domain.models.Dificultad
import com.oasis.bite.domain.models.Ingrediente
import com.oasis.bite.domain.models.PasoReceta
import com.oasis.bite.presentation.adapters.MultimediaAdapter
import com.oasis.bite.presentation.viewmodel.RecetaViewModel

class AgregarRecetaActivity : AppCompatActivity() {
    private var _binding: ActivityAgregarRecetaBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecetaViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MultimediaAdapter
    private val archivos = mutableListOf<Uri>()
    private val listaIngredientes = mutableListOf<Ingrediente>()
    private val listaPasos = mutableListOf<PasoReceta>()
    private lateinit var pickMediaLauncher: ActivityResultLauncher<Intent> //Esto es una variable que lanza una intención (intent) y luego escucha la respuesta.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAgregarRecetaBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(RecetaViewModel::class.java)
        setContentView(binding.root)
        supportActionBar?.hide()

        val isEditando = intent.getBooleanExtra("isEditando", false)
        if (isEditando) {
            val receta = viewModel.cargarReceta(intent.getStringExtra("recetaId").toString())
            //aca va todas las instanciaciones
            //habria que agregar el cambio de nombre del boton de abajo y que si es editar se llama al put y sino al post
        } else {
            val botonContinuar = binding.continuarButton
            val usuario = intent.getStringExtra("usuario")
            val inputTitulo = binding.titulo
            val inputdescripcion = binding.descripcion
            val dropdown = binding.dropdown
            val inputporciones: EditText = findViewById(R.id.porciones)
            val inputtiempo: EditText = findViewById(R.id.tiempo)
            val opciones = Dificultad.values().map { it.label }
            val adapterDrop =
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opciones)
            dropdown.setAdapter(adapterDrop)
            pickMediaLauncher = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.data?.let { uri ->
                        archivos.add(uri)
                        adapter.notifyItemInserted(archivos.size - 1)
                    }
                }
            }
            recyclerView = binding.recyclerMultimedia
            recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            adapter = MultimediaAdapter(archivos, ::onAddClicked)
            recyclerView.adapter = adapter
            val layoutIngredientes = binding.layoutIngredientes
            val botonAgregarIngrediente = binding.btnAgregarIngrediente
            val inflater = LayoutInflater.from(this) // o requireContext()

            val unidades = listOf("g", "kg", "ml", "L", "cda", "cdta")
            botonAgregarIngrediente.setOnClickListener {
                val view =
                    inflater.inflate(R.layout.item_ingrediente_agregar, layoutIngredientes, false)
                val inputNombre = view.findViewById<EditText>(R.id.inputIngrediente)
                val inputCantidad = view.findViewById<EditText>(R.id.inputCantidad)
                val unidadDropdown = view.findViewById<AutoCompleteTextView>(R.id.inputUnidad)
                val adapter =
                    ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, unidades)
                unidadDropdown.setAdapter(adapter)

                layoutIngredientes.addView(view)
                val focusListener = View.OnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        val nombre = inputNombre.text.toString().trim()
                        val cantidadText = inputCantidad.text.toString().trim()
                        val unidad = unidadDropdown.text.toString().trim()

                        // Solo si los 3 campos están llenos y no se agregó antes
                        if (nombre.isNotEmpty() && cantidadText.isNotEmpty() && unidad.isNotEmpty()) {
                            val cantidad = cantidadText.toFloatOrNull() ?: 0f

                            // Verificá si ya fue agregado (por ejemplo, por nombre + cantidad + unidad)
                            val yaExiste = listaIngredientes.any {
                                it.nombre == nombre && it.cantidad == cantidad && it.unidad == unidad
                            }

                            if (!yaExiste) {
                                listaIngredientes.add(Ingrediente(nombre, cantidad, unidad))
                                Log.d("Ingredientes", "Agregado: $nombre - $cantidad $unidad")
                            }
                        }
                    }
                }

                inputNombre.onFocusChangeListener = focusListener
                inputCantidad.onFocusChangeListener = focusListener
                unidadDropdown.onFocusChangeListener = focusListener
            }
            val layoutPasos = binding.layoutPasos
            val botonAgregarPaso = binding.btnAgregarPasos
            val inflater2 = LayoutInflater.from(this) // o requireContext()
            var i = 0
            botonAgregarPaso.setOnClickListener {
                i = i+1
                val view =
                    inflater2.inflate(R.layout.item_paso_agregar, layoutPasos, false)
                val inputNombre = view.findViewById<EditText>(R.id.inputPaso)

                layoutPasos.addView(view)
                val focusListener = View.OnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        val nombre = inputNombre.text.toString().trim()
                        // Solo si los 3 campos están llenos y no se agregó antes
                        if (nombre.isNotEmpty() ) {
                                listaPasos.add(PasoReceta(i.toString(),nombre,null,null))
                                Log.d("Pasos", "Agregado: $nombre ")

                        }
                    }
                }

                inputNombre.onFocusChangeListener = focusListener
            }
            inputTitulo.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                   if( viewModel.verificarSiRecetaExiste(inputTitulo.text.toString(), usuario.toString())!= 0){
                        //aca iria el pop up preguntandole si quiere editar o reemplazar dialogo
                   }else{
                       inputdescripcion.isEnabled = true
                       inputporciones.isEnabled=true
                       inputtiempo.isEnabled = true
                       dropdown.isEnabled = true
                       botonAgregarPaso.isEnabled = true
                       botonAgregarIngrediente.isEnabled = true
                   }
                    //aca lo puedo hacer trayendo todas las recetas del usuario y hacer un map con los titulos y un contain
                }
            }

            botonContinuar.setOnClickListener {
                val titulo = inputTitulo.text.toString()
                val descripcion = inputdescripcion.text.toString()
                val imagen = "https://youizvpxaxfootagvdqp.supabase.co/storage/v1/object/public/imagenes//palta2.jpg"
                val porciones = inputporciones.text.toString()
                val tiempo = inputtiempo.text.toString()
                val dificultad = dropdown.text.toString()
                viewModel.agregarReceta(titulo, descripcion,tiempo,porciones,dificultad,imagen,
                    usuario.toString(), listaIngredientes , listaPasos)
            }


        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun onAddClicked() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/* video/*"
        pickMediaLauncher.launch(intent) // En vez de startActivityForResult
    }

}




