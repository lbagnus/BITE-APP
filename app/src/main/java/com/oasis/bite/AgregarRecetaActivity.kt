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
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.oasis.bite.data.model.PasoRecetaRequest
import com.oasis.bite.databinding.ActivityAgregarRecetaBinding
import com.oasis.bite.databinding.ItemPasoAgregarBinding
import com.oasis.bite.domain.models.Dificultad
import com.oasis.bite.domain.models.Ingrediente
import com.oasis.bite.domain.models.MediaItem
import com.oasis.bite.domain.models.MediaType
import com.oasis.bite.domain.models.PasoReceta
import com.oasis.bite.presentation.adapters.MultimediaAdapter
import com.oasis.bite.presentation.viewmodel.RecetaViewModel

class AgregarRecetaActivity : AppCompatActivity() {
    private var _binding: ActivityAgregarRecetaBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecetaViewModel

    private val listaIngredientes = mutableListOf<Ingrediente>()
    private val listaPasos = mutableListOf<PasoRecetaRequest>() // Aquí guardaremos los objetos PasoReceta

    private lateinit var recetaImagenPrincipalUrl: String  // URL para la imagen principal de la receta
    private val recetaImagenesUrlsList = mutableListOf<String>()
    // --- Variables para el manejo de imágenes de pasos ---
    private var currentPasoIndexForImage: Int = -1 // Usaremos esto para saber qué paso está subiendo una imagen
    private lateinit var currentImageViewForImage: ImageView // La ImageView del paso actual que se está subiendo

    // Un único ActivityResultLauncher para todas las selecciones de imagen (receta y pasos)
    private lateinit var globalPickMediaLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAgregarRecetaBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(RecetaViewModel::class.java)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.btnVolver.setOnClickListener {
            finish()
        }

        // --- Configuración del globalPickMediaLauncher ---
        globalPickMediaLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    // Decide si esta imagen es para la receta principal o para un paso
                    if (currentPasoIndexForImage == -1) { // Es para la receta principal
                        binding.imagenSeleccionada.apply {
                            setImageURI(uri)
                            visibility = View.VISIBLE
                        }
                        viewModel.subirImagen(this, uri) { url ->
                            if (url != null) {
                                recetaImagenPrincipalUrl = url
                                if (!recetaImagenesUrlsList.contains(url)) { // Si quieres permitir múltiples imágenes para la receta en el array
                                    recetaImagenesUrlsList.add(url)
                                }
                                Log.d("UPLOAD_RECETA", "Imagen principal subida: $url")
                            } else {
                                Toast.makeText(this, "Error al subir la imagen principal", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else { // Es para un paso
                        currentImageViewForImage.apply { // Usamos la referencia guardada del ImageView
                            setImageURI(uri)
                            visibility = View.VISIBLE
                        }
                        viewModel.subirImagen(this, uri) { url ->
                            if (url != null) {
                                val pasoToUpdateIndex = listaPasos.indexOfFirst {
                                    it.numeroDePaso == currentPasoIndexForImage.toString()
                                }
                                if (pasoToUpdateIndex != -1) {
                                    val currentPaso = listaPasos[pasoToUpdateIndex]
                                    // Asigna la URL al campo singular 'archivoFoto'
                                    val updatedPasoWithArchivoFoto = currentPaso.copy(archivoFoto = url)

                                    // Agrega la URL a la lista 'imagenesPasos' (List<String>)
                                    val updatedImagenesPasosUrls = (currentPaso.imagenesPasos?.toMutableList() ?: mutableListOf()).apply {
                                        if (!contains(url)) {
                                            add(url)
                                        }
                                    }
                                    val finalUpdatedPaso = updatedPasoWithArchivoFoto.copy(imagenesPasos = updatedImagenesPasosUrls)

                                    listaPasos[pasoToUpdateIndex] = finalUpdatedPaso
                                    Log.d("UPLOAD_PASO", "Imagen para el paso ${currentPasoIndexForImage} actualizada: $url")
                                }
                            }  else {
                                Toast.makeText(this, "Error al subir la imagen para el paso", Toast.LENGTH_SHORT).show()
                            }
                            currentPasoIndexForImage = -1
                        }
                    }
                }
            }
        }
        // --- Fin configuración globalPickMediaLauncher ---


        val isEditando = intent.getBooleanExtra("isEditando", false)
        if (isEditando) {
            val receta = viewModel.cargarReceta(intent.getStringExtra("recetaId").toString())
            // ... lógica de edición ...
        } else {
            val botonContinuar = binding.continuarButton
            val usuarioEmail = intent.getStringExtra("usuarioEmail")
            val inputTitulo = binding.titulo
            binding.titulo.setText(intent.getStringExtra("tituloReceta"))
            binding.titulo.isEnabled = false
            val inputdescripcion = binding.descripcion
            val dropdown = binding.dropdown
            val inputporciones: EditText = findViewById(R.id.porciones)
            val dropdownCategoria = binding.dropdownCategoria
            val inputtiempo: EditText = findViewById(R.id.tiempo)
            val opciones = Dificultad.values().map { it.label }
            val categorias = listOf("Pastas","Ensaladas","Postres","Pizza","Sano","Carnes","Veggie","Entre Pan")

            val adapterDrop =
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, opciones)
            dropdown.setAdapter(adapterDrop)

            val adapterDropCategoria =
                ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categorias)
            dropdownCategoria.setAdapter(adapterDropCategoria)

            binding.btnSubirImagen.setOnClickListener {
                currentPasoIndexForImage = -1 // Indica que la siguiente imagen es para la receta principal
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "image/*"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                globalPickMediaLauncher.launch(Intent.createChooser(intent, "Seleccioná una imagen para la receta"))
            }

            val layoutIngredientes = binding.layoutIngredientes
            val botonAgregarIngrediente = binding.btnAgregarIngrediente
            val inflater = LayoutInflater.from(this)

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

                        if (nombre.isNotEmpty() && cantidadText.isNotEmpty() && unidad.isNotEmpty()) {
                            val cantidad = cantidadText.toFloatOrNull() ?: 0f

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
            val inflater2 = LayoutInflater.from(this)
            var pasoIndexCounter = 0 // Contador para asignar un ID único a cada paso

            botonAgregarPaso.setOnClickListener {
                pasoIndexCounter++
                val currentPasoTempId = pasoIndexCounter // ID único para este paso específico

                val view = inflater2.inflate(R.layout.item_paso_agregar, layoutPasos, false)
                val itemPasoBinding = ItemPasoAgregarBinding.bind(view)
                val inputNombrePaso = itemPasoBinding.inputPaso
                val btnAgregarImagenPaso = itemPasoBinding.agregarImagenPaso
                val imagenSeleccionadaPaso = itemPasoBinding.imagenSeleccionada

                // Inicializar un objeto PasoReceta temporalmente y agregarlo a la lista
                // Se actualizará después con la URL de la imagen y la descripción final
                val nuevoPaso = PasoRecetaRequest(
                    numeroDePaso = currentPasoTempId.toString(),
                    contenido = "", // Inicialmente vacío
                    archivoFoto = null,
                    imagenesPasos = mutableListOf()
                )
                listaPasos.add(nuevoPaso)

                // 2. Configurar el OnClickListener para el botón de imagen de este paso
                btnAgregarImagenPaso.setOnClickListener {
                    currentPasoIndexForImage = currentPasoTempId // Indicar qué paso espera la imagen
                    currentImageViewForImage = imagenSeleccionadaPaso // Guardar la referencia al ImageView de este paso

                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "image/*"
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    globalPickMediaLauncher.launch(Intent.createChooser(intent, "Seleccioná una imagen para el paso ${currentPasoTempId}"))
                }

                layoutPasos.addView(view)

                // 3. Modificar el FocusListener para capturar el texto del paso
                val focusListenerPaso = View.OnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        val descripcionPaso = inputNombrePaso.text.toString().trim()
                        // Buscar el paso correcto por su ID temporal y actualizar la descripción
                        val pasoToUpdateIndex = listaPasos.indexOfFirst {
                            it.numeroDePaso == currentPasoTempId.toString()
                        }
                        if (pasoToUpdateIndex != -1) {
                            val currentPaso = listaPasos[pasoToUpdateIndex]
                            if (currentPaso.contenido != descripcionPaso) { // Solo actualizar si la descripción cambió
                                listaPasos[pasoToUpdateIndex] = currentPaso.copy(contenido = descripcionPaso)
                                Log.d("Pasos", "Descripción actualizada para el paso $currentPasoTempId: $descripcionPaso")
                            }
                        }
                    }
                }
                inputNombrePaso.onFocusChangeListener = focusListenerPaso
            }


            botonContinuar.setOnClickListener {
                val titulo = inputTitulo.text.toString()
                val descripcion = inputdescripcion.text.toString()
                // La URL de la imagen principal singular para el campo 'imagen'
                val imagenPrincipalReceta = recetaImagenPrincipalUrl
                // La lista de MediaItems para el campo 'imagenes'
                val imagenesRecetaParaEnvio: List<String> = if (recetaImagenesUrlsList.isNotEmpty()) {
                    recetaImagenesUrlsList.toList()
                } else {
                    emptyList()
                }
                val porciones = inputporciones.text.toString()
                val tiempo = inputtiempo.text.toString()
                val dificultad = dropdown.text.toString()
                val categoria = dropdownCategoria.text.toString()

                // Opcional: Una última pasada para asegurarse de que todos los datos estén en listaPasos
                // Esto es importante si el usuario escribe el paso pero no pierde el foco antes de "Continuar"
                // y para asegurarse que las imágenes estén asociadas si se subieron antes de la descripción
                for (i in 0 until layoutPasos.childCount) {
                    val view = layoutPasos.getChildAt(i)
                    val inputNombrePaso = view.findViewById<EditText>(R.id.inputPaso)
                    // Asume que el orden del child en layoutPasos es el mismo que el pasoIndexCounter
                    // Es crucial que el 'orden' en PasoReceta sea el mismo que el pasoIndexCounter
                    // para que la búsqueda sea precisa.
                    val pasoTempId = (view.tag as? Int) ?: (i + 1) // Si guardaste el tag, úsalo, sino i+1

                    val descripcionPaso = inputNombrePaso.text.toString().trim()

                    // Buscar el paso existente en listaPasos por su ID temporal
                    val existingPasoIndex = listaPasos.indexOfFirst { it.numeroDePaso == pasoTempId.toString() }

                    if (existingPasoIndex != -1) {
                        val currentPasoInList = listaPasos[existingPasoIndex]
                        // Actualizar solo si la descripción cambió o la imagen es nula y ya se subió
                        if (currentPasoInList.contenido != descripcionPaso) {
                            listaPasos[existingPasoIndex] = currentPasoInList.copy(contenido = descripcionPaso)
                        }
                        // La URL de la imagen se actualiza directamente en el launcher,
                        // por lo que no es necesario un manejo adicional aquí a menos que quieras validar.
                    } else if (descripcionPaso.isNotEmpty()) {
                        // Este caso es menos probable si agregamos el paso a listaPasos al crear la vista.
                        // Solo si el paso no se agregó previamente a listaPasos (ej. si el usuario solo escribe texto sin clickear el botón de imagen)
                        listaPasos.add(
                            PasoRecetaRequest(
                                numeroDePaso = pasoTempId.toString(),
                                contenido = descripcionPaso,
                                archivoFoto = null,
                                imagenesPasos = null
                            )
                        )
                    }
                }

                // Filtrar pasos que puedan haberse agregado sin contenido si se interactuó con el botón de imagen pero no con el texto
                val pasosValidos = listaPasos.filter { it.contenido.isNotEmpty() || it.archivoFoto != null }

                if (intent.getBooleanExtra("reemplaza", false)) {
                    val idRecetaAEliminar = intent.getStringExtra("idReceta")
                    Log.d("eliminar receta", idRecetaAEliminar.toString())
                    // Use '?.let' to only execute the block if idRecetaAEliminar is NOT null
                    idRecetaAEliminar?.let { id ->
                        viewModel.eliminarReceta(id) // 'id' here is guaranteed to be a non-null String
                    } ?: run {
                        // Optional: Handle the case where idRecetaAEliminar is null, if necessary
                        Log.e("AgregarRecetaActivity", "Error: idReceta es nulo al intentar eliminar.")
                        Toast.makeText(this, "Error al eliminar la receta: ID no encontrado.", Toast.LENGTH_SHORT).show()
                    }
                }
                viewModel.agregarReceta(titulo, descripcion, tiempo, porciones, dificultad, imagenPrincipalReceta, imagenesRecetaParaEnvio,
                    usuarioEmail.toString(), listaIngredientes, pasosValidos, categoria)
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}






