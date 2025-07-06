package com.oasis.bite

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.oasis.bite.data.model.PasoRecetaRequest
import com.oasis.bite.databinding.ActivityAgregarRecetaBinding
import com.oasis.bite.databinding.ItemPasoAgregarBinding
import com.oasis.bite.domain.models.Dificultad
import com.oasis.bite.domain.models.Ingrediente
import com.oasis.bite.domain.models.PasoReceta
import com.oasis.bite.presentation.viewmodel.RecetaViewModel
import com.oasis.bite.presentation.viewmodel.RecetaViewModelFactory
import java.io.File
import java.io.FileOutputStream

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
    var isInternetConnected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityAgregarRecetaBinding.inflate(layoutInflater)
        val factory = RecetaViewModelFactory(applicationContext)
        viewModel = ViewModelProvider(this, factory).get(RecetaViewModel::class.java)
        setContentView(binding.root)
        isInternetConnected = isInternetAvailable(this)
        supportActionBar?.hide()

        binding.btnVolver.setOnClickListener {
            showMensajeAtras { accion ->
                when (accion) {
                    AccionDialogoAtras.VOLVER -> {
                        finish()
                    }
                    AccionDialogoAtras.PERMANECER -> {
                        // no hace nada
                    }
                }}
        }
        viewModel.recetaOperationStatus.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Operación de receta exitosa!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "La operación de receta falló. Inténtelo de nuevo.", Toast.LENGTH_LONG).show()
            }
            // Ahora, y solo ahora, finaliza la actividad
            finish()
        }

        // 3. Mejorar el manejo de imágenes en el ActivityResultLauncher
        globalPickMediaLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    Log.d("CALLBACK_IMAGEN", "Procesando imagen para paso: $currentPasoIndexForImage")

                    if (currentPasoIndexForImage == -1) {
                        Log.d("CALLBACK_IMAGEN", "→ Es imagen de receta principal")
                        // Imagen para la receta principal
                        manejarImagenRecetaPrincipal(uri)
                    } else {
                        Log.d("CALLBACK_IMAGEN", "→ Es imagen de paso $currentPasoIndexForImage")
                        // Imagen para un paso específico
                        manejarImagenPaso(uri, currentPasoIndexForImage)
                    }
                } ?: run {
                    Log.e("CALLBACK_IMAGEN", "✗ URI de imagen es nulo")
                }
            } else {
                Log.e("CALLBACK_IMAGEN", "✗ Result code no es OK: ${result.resultCode}")
            }
        }

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

            dropdownCategoria.setOnClickListener {
                dropdownCategoria.showDropDown()
            }
            dropdown.setOnClickListener {
                dropdown.showDropDown()
            }

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

            val unidades = listOf("g", "kg", "ml", "L", "cda", "cdta", "u")
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
                val currentPasoTempId = pasoIndexCounter

                val view = inflater2.inflate(R.layout.item_paso_agregar, layoutPasos, false)
                val itemPasoBinding = ItemPasoAgregarBinding.bind(view)
                val inputNombrePaso = itemPasoBinding.inputPaso
                val btnAgregarImagenPaso = itemPasoBinding.agregarImagenPaso
                val imagenSeleccionadaPaso = itemPasoBinding.imagenSeleccionada

                // IMPORTANTE: Asignar el tag a la vista
                view.tag = currentPasoTempId
                Log.d("CREAR_PASO", "Creando paso con ID: $currentPasoTempId")

                // Crear el paso con un ID único basado en el contador
                val nuevoPaso = PasoRecetaRequest(
                    numeroDePaso = currentPasoTempId.toString(),
                    contenido = "",
                    archivoFoto = null,
                    imagenesPasos = mutableListOf()
                )
                listaPasos.add(nuevoPaso)
                Log.d("CREAR_PASO", "Paso agregado a la lista. Total pasos: ${listaPasos.size}")

                // Configurar el OnClickListener para el botón de imagen
                btnAgregarImagenPaso.setOnClickListener {
                    currentPasoIndexForImage = currentPasoTempId // Usar el ID correcto
                    currentImageViewForImage = imagenSeleccionadaPaso
                    Log.d("SELECCIONAR_IMAGEN", "Seleccionando imagen para paso: $currentPasoIndexForImage")

                    val intent = Intent(Intent.ACTION_GET_CONTENT)
                    intent.type = "image/*"
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    globalPickMediaLauncher.launch(Intent.createChooser(intent, "Seleccioná una imagen para el paso ${currentPasoTempId}"))
                }

                layoutPasos.addView(view)

                // FocusListener para capturar el texto del paso
                val focusListenerPaso = View.OnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        val descripcionPaso = inputNombrePaso.text.toString().trim()
                        Log.d("TEXTO_PASO", "Perdiendo foco en paso $currentPasoTempId: '$descripcionPaso'")
                        actualizarDescripcionPaso(currentPasoTempId, descripcionPaso)
                    }
                }
                inputNombrePaso.onFocusChangeListener = focusListenerPaso
            }
        val isEditando = intent.getBooleanExtra("isEditando", false)
        if (isEditando) {
            viewModel.cargarReceta(intent.getStringExtra("idReceta").toString())
            viewModel.receta.observe(this) { receta ->
                receta?.let {
                    binding.descripcion.setText(receta.descripcion)
                    binding.porciones.setText(receta.porciones.toString())
                    binding.tiempo.setText(receta.tiempo)
                    binding.dropdownCategoria.setText(receta.categoria, false)
                    binding.dropdown.setText(receta.dificultad.label.toString(), false)
                    val imageUrlToLoad = receta.imagen
                        ?: receta.imagenes?.firstOrNull()?.url // Intenta obtener la primera URL de la lista si la principal es null
                    imageUrlToLoad?.let { url ->
                        Glide.with(this) // Usa 'this' (la Activity) como contexto para Glide
                            .load(url) // `.load()` espera una URL (String)
                            .into(binding.imagenSeleccionada) // Tu ImageView principal de la receta
                        binding.imagenSeleccionada.visibility = View.VISIBLE // Asegura que se muestre
                    }
                    recetaImagenPrincipalUrl = imageUrlToLoad?: "Hola"
                    recetaImagenesUrlsList.add(recetaImagenPrincipalUrl)

                    // --- Cargar Ingredientes (si estás editando) ---
                    // Tendrías que iterar sobre loadedReceta.ingredientes y añadirlos dinámicamente
                    // a layoutIngredientes, similar a como agregas nuevos ingredientes.
                    // Esto es más complejo y requeriría adaptar tu lógica de agregar ingredientes.
                    // Por ejemplo:

                    listaIngredientes.clear() // Limpiar la lista actual
                    layoutIngredientes.removeAllViews() // Eliminar vistas existentes
                    receta.ingredientes.forEach { ingrediente ->
                        val view = LayoutInflater.from(this).inflate(R.layout.item_ingrediente_agregar, layoutIngredientes, false)
                        val inputNombre = view.findViewById<EditText>(R.id.inputIngrediente)
                        val inputCantidad = view.findViewById<EditText>(R.id.inputCantidad)
                        val unidadDropdown = view.findViewById<AutoCompleteTextView>(R.id.inputUnidad)

                        inputNombre.setText(ingrediente.nombre)
                        inputCantidad.setText(ingrediente.cantidad.toString())
                        unidadDropdown.setText(ingrediente.unidad, false) // `false` para no mostrar el dropdown al establecer
                        // Añadir OnFocusChangeListener o TextWatcher para actualizar listaIngredientes si el usuario edita
                        listaIngredientes.add(ingrediente) // Añadir al modelo interno
                        layoutIngredientes.addView(view)
                    }


                    // --- Cargar Pasos (si estás editando) ---
                    // Similar a ingredientes, iterar sobre loadedReceta.pasos
                    // y añadir las vistas dinámicamente.
                    // Y para las imágenes de los pasos, también usar Glide.

                    listaPasos.clear()
                    layoutPasos.removeAllViews()
                    // Asegúrate de resetear pasoIndexCounter
                    var currentLoadedPasoIndex = 0
                    receta.pasos.forEach { paso ->
                        currentLoadedPasoIndex++
                        val view = LayoutInflater.from(this).inflate(R.layout.item_paso_agregar, layoutPasos, false)
                        val itemPasoBinding = ItemPasoAgregarBinding.bind(view)
                        itemPasoBinding.inputPaso.setText(paso.contenido)
                        // Cargar imagen del paso si existe
                        paso.archivoFoto?.let { pasoImageUrl ->
                            Log.d("lleguepasorecetaaa", "$pasoImageUrl")
                            Glide.with(this)
                                .load(pasoImageUrl)
                                .into(itemPasoBinding.imagenSeleccionada) // Asegúrate que el ID sea correcto en item_paso_agregar
                            itemPasoBinding.imagenSeleccionada.visibility = View.VISIBLE
                        }
                        // También puedes iterar sobre paso.imagenesPasos si un paso tiene múltiples imágenes
                        // Agrega el paso al modelo interno
                        listaPasos.add(PasoRecetaRequest(
                            numeroDePaso = paso.numeroDePaso,
                            contenido = paso.contenido,
                            archivoFoto = paso.archivoFoto,
                            imagenesPasos = paso.imagenesPasos?.map { it.url } // O conviértelas si es necesario
                        ))
                        layoutPasos.addView(view)
                    }
                    // Asegúrate de que pasoIndexCounter refleje el último índice cargado
                    // pasoIndexCounter = currentLoadedPasoIndex

                    Log.d("editar", "llegue $receta")
                }
            }


        }

            // 7. Mejorar la validación final en el botón continuar
            botonContinuar.setOnClickListener {
                Log.d("BOTON_CONTINUAR", "=== INICIANDO VALIDACIÓN FINAL ===")

                // ... código existente para obtener datos básicos ...
                val titulo = inputTitulo.text.toString()
                val descripcion = inputdescripcion.text.toString()
                val imagenPrincipalReceta = recetaImagenPrincipalUrl
                val imagenesRecetaParaEnvio: List<String> = if (recetaImagenesUrlsList.isNotEmpty()) {
                    recetaImagenesUrlsList.toList()
                } else {
                    emptyList()
                }
                val porciones = inputporciones.text.toString()
                val tiempo = inputtiempo.text.toString()
                val dificultad = dropdown.text.toString()
                val categoria = dropdownCategoria.text.toString()

                Log.d("BOTON_CONTINUAR", "Estado inicial - Total pasos en lista: ${listaPasos.size}")
                listaPasos.forEachIndexed { index, paso ->
                    Log.d("BOTON_CONTINUAR", "  Paso $index: ID=${paso.numeroDePaso}, contenido='${paso.contenido}', imagen='${paso.archivoFoto}'")
                }

                Log.d("BOTON_CONTINUAR", "Validando vistas en layoutPasos: ${layoutPasos.childCount} vistas")

                // Validación final mejorada
                for (i in 0 until layoutPasos.childCount) {
                    val view = layoutPasos.getChildAt(i)
                    val inputNombrePaso = view.findViewById<EditText>(R.id.inputPaso)
                    val pasoTempId = view.tag as? Int

                    Log.d("BOTON_CONTINUAR", "Vista $i: tag=$pasoTempId")

                    if (pasoTempId == null) {
                        Log.e("BOTON_CONTINUAR", "✗ Vista $i no tiene tag asignado")
                        continue
                    }

                    val descripcionPaso = inputNombrePaso.text.toString().trim()
                    Log.d("BOTON_CONTINUAR", "Vista $i (paso $pasoTempId): texto='$descripcionPaso'")

                    if (descripcionPaso.isNotEmpty()) {
                        actualizarDescripcionPaso(pasoTempId, descripcionPaso)
                    }
                }

                Log.d("BOTON_CONTINUAR", "Después de validación - Total pasos en lista: ${listaPasos.size}")
                listaPasos.forEachIndexed { index, paso ->
                    Log.d("BOTON_CONTINUAR", "  Paso $index: ID=${paso.numeroDePaso}, contenido='${paso.contenido}', imagen='${paso.archivoFoto}'")
                }

                // Filtrar pasos válidos
                val pasosValidos = listaPasos.filter {
                    it.contenido.isNotEmpty() || !it.archivoFoto.isNullOrEmpty()
                }

                Log.d("BOTON_CONTINUAR", "Pasos válidos después del filtro: ${pasosValidos.size}")

                // Renumerar los pasos válidos secuencialmente
                val pasosFinales = pasosValidos.mapIndexed { index, paso ->
                    paso.copy(numeroDePaso = (index + 1).toString())
                }

                Log.d("BOTON_CONTINUAR", "=== PASOS FINALES ===")
                Log.d("BOTON_CONTINUAR", "Total pasos válidos: ${pasosFinales.size}")
                pasosFinales.forEach { paso ->
                    Log.d("BOTON_CONTINUAR", "  ✓ Paso ${paso.numeroDePaso}:")
                    Log.d("BOTON_CONTINUAR", "    - Contenido: '${paso.contenido}'")
                    Log.d("BOTON_CONTINUAR", "    - Imagen: '${paso.archivoFoto}'")
                    Log.d("BOTON_CONTINUAR", "    - Imágenes lista: ${paso.imagenesPasos}")
                }



                if (isInternetConnected) {
                    Log.d("BOTON_CONTINUAR", "Guardando receta ONLINE")
                    if(isEditando){
                        viewModel.editarReceta(intent.getStringExtra("idReceta").toString(),
                            titulo,
                            descripcion,
                            tiempo,
                            porciones,
                            dificultad,
                            imagenPrincipalReceta,
                            imagenesRecetaParaEnvio,
                            usuarioEmail.toString(),
                            listaIngredientes,
                            pasosFinales,
                            categoria
                        )
                    }else {
                        if (intent.getBooleanExtra("reemplaza", false)) {
                            val idRecetaAEliminar = intent.getStringExtra("idReceta")
                            Log.d("BOTON_CONTINUAR", "Eliminando receta anterior: $idRecetaAEliminar")
                            idRecetaAEliminar?.let { id ->
                                viewModel.eliminarReceta(id)
                            } ?: run {
                                Log.e("BOTON_CONTINUAR", "Error: idReceta es nulo al intentar eliminar.")
                                Toast.makeText(this, "Error al eliminar la receta: ID no encontrado.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        if (getConnectionType(this) == ConnectionType.WIFI || viewModel.loadWifiPreference(usuarioEmail.toString()) == true ){
                        viewModel.agregarReceta(
                            titulo,
                            descripcion,
                            tiempo,
                            porciones,
                            dificultad,
                            imagenPrincipalReceta,
                            imagenesRecetaParaEnvio,
                            usuarioEmail.toString(),
                            listaIngredientes,
                            pasosFinales,
                            categoria
                        )}else{
                            Log.d("BOTON_CONTINUAR", "Guardando receta OFFLINE")
                            viewModel.agregarRecetaOffline(titulo, descripcion, tiempo, porciones, dificultad, imagenPrincipalReceta, imagenesRecetaParaEnvio,
                                usuarioEmail.toString(), listaIngredientes, pasosFinales, categoria)
                            Toast.makeText(this, "Receta guardada localmente, se subirá cuando tengas WIFI", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.d("BOTON_CONTINUAR", "Guardando receta OFFLINE")
                    viewModel.agregarRecetaOffline(titulo, descripcion, tiempo, porciones, dificultad, imagenPrincipalReceta, imagenesRecetaParaEnvio,
                        usuarioEmail.toString(), listaIngredientes, pasosFinales, categoria)
                    Toast.makeText(this, "Receta guardada localmente", Toast.LENGTH_SHORT).show()
                }

                Log.d("BOTON_CONTINUAR", "=== FIN VALIDACIÓN FINAL ===")
                //finish()
            }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // Función para verificar la conexión a Internet
    enum class ConnectionType {
        WIFI,
        MOBILE_DATA,
        ETHERNET, // Podrías añadir otros tipos si son relevantes (VPN, Bluetooth, etc.)
        NO_INTERNET
    }

    private fun getConnectionType(context: Context): ConnectionType {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Para Android 10 (API 29) y superior, NetworkInfo está deprecado.
        // Se recomienda usar NetworkCapabilities.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // API 23+
            val network = connectivityManager.activeNetwork ?: return ConnectionType.NO_INTERNET
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return ConnectionType.NO_INTERNET

            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.MOBILE_DATA
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
                // Puedes añadir más TRANSPORT_ aquí para otros tipos (VPN, Bluetooth)
                else -> ConnectionType.NO_INTERNET
            }
        } else {
            // Para versiones de Android anteriores a Marshmallow (API 23)
            // NetworkInfo todavía se usa
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            return if (networkInfo != null && networkInfo.isConnected) {
                when (networkInfo.type) {
                    ConnectivityManager.TYPE_WIFI -> ConnectionType.WIFI
                    ConnectivityManager.TYPE_MOBILE -> ConnectionType.MOBILE_DATA
                    ConnectivityManager.TYPE_ETHERNET -> ConnectionType.ETHERNET
                    else -> ConnectionType.NO_INTERNET
                }
            } else {
                ConnectionType.NO_INTERNET
            }
        }
    }

    // Puedes mantener tu función isInternetAvailable si la usas mucho,
// o simplemente llamar a getConnectionType y verificar si no es NO_INTERNET
    private fun isInternetAvailable(context: Context): Boolean {
        return getConnectionType(context) != ConnectionType.NO_INTERNET
    }

    private fun guardarImagenLocalmente(context: Context, uri: Uri, nombreArchivo: String): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val archivo = File(context.filesDir, "$nombreArchivo.jpg")
        val outputStream = FileOutputStream(archivo)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        return archivo.absolutePath // Devuelve la ruta local que luego podrás usar
    }

    // 2. Función auxiliar para actualizar la descripción del paso
    private fun actualizarDescripcionPaso(pasoId: Int, descripcion: String) {
        Log.d("ACTUALIZAR_DESCRIPCION", "Intentando actualizar paso $pasoId con descripción: '$descripcion'")

        val pasoToUpdateIndex = listaPasos.indexOfFirst {
            it.numeroDePaso == pasoId.toString()
        }

        if (pasoToUpdateIndex != -1) {
            val currentPaso = listaPasos[pasoToUpdateIndex]
            if (currentPaso.contenido != descripcion) {
                listaPasos[pasoToUpdateIndex] = currentPaso.copy(contenido = descripcion)
                Log.d("ACTUALIZAR_DESCRIPCION", "✓ Descripción actualizada para el paso $pasoId: '$descripcion'")
            } else {
                Log.d("ACTUALIZAR_DESCRIPCION", "= Descripción sin cambios para el paso $pasoId")
            }
        } else {
            Log.e("ACTUALIZAR_DESCRIPCION", "✗ No se encontró el paso con ID: $pasoId. Pasos disponibles: ${listaPasos.map { it.numeroDePaso }}")
        }
    }

    // 4. Función para manejar imagen de receta principal
    private fun manejarImagenRecetaPrincipal(uri: Uri) {
        Log.d("IMAGEN_RECETA", "Procesando imagen principal de receta")

        binding.imagenSeleccionada.apply {
            setImageURI(uri)
            visibility = View.VISIBLE
        }

        if (isInternetConnected) {
            Log.d("IMAGEN_RECETA", "Subiendo imagen principal online...")
            viewModel.subirImagen(this, uri) { url ->
                if (url != null) {
                    recetaImagenPrincipalUrl = url
                    if (!recetaImagenesUrlsList.contains(url)) {
                        recetaImagenesUrlsList.add(url)
                    }
                    Log.d("IMAGEN_RECETA", "✓ Imagen principal subida online: $url")
                } else {
                    Log.e("IMAGEN_RECETA", "✗ Error al subir imagen principal online")
                    Toast.makeText(this, "Error al subir la imagen principal", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Log.d("IMAGEN_RECETA", "Guardando imagen principal offline...")
            val localPath = guardarImagenLocalmente(this, uri, "receta_${System.currentTimeMillis()}")
            recetaImagenPrincipalUrl = localPath
            if (!recetaImagenesUrlsList.contains(localPath)) {
                recetaImagenesUrlsList.add(localPath)
            }
            Log.d("IMAGEN_RECETA", "✓ Imagen principal guardada offline: $localPath")
        }
    }

    // 5. Función para manejar imagen de paso específico
    private fun manejarImagenPaso(uri: Uri, pasoId: Int) {
        Log.d("IMAGEN_PASO", "Procesando imagen para paso $pasoId")

        currentImageViewForImage.apply {
            setImageURI(uri)
            visibility = View.VISIBLE
        }

        if (isInternetConnected) {
            Log.d("IMAGEN_PASO", "Subiendo imagen del paso $pasoId online...")
            viewModel.subirImagen(this, uri) { url ->
                if (url != null) {
                    Log.d("IMAGEN_PASO", "✓ Imagen subida online para paso $pasoId: $url")
                    actualizarImagenPaso(pasoId, url)
                } else {
                    Log.e("IMAGEN_PASO", "✗ Error al subir imagen online para paso $pasoId")
                    Toast.makeText(this, "Error al subir la imagen para el paso", Toast.LENGTH_SHORT).show()
                }
                currentPasoIndexForImage = -1
                Log.d("IMAGEN_PASO", "Reset currentPasoIndexForImage = -1")
            }
        } else {
            Log.d("IMAGEN_PASO", "Guardando imagen del paso $pasoId offline...")
            val localPath = guardarImagenLocalmente(this, uri, "paso_${pasoId}_${System.currentTimeMillis()}")
            Log.d("IMAGEN_PASO", "✓ Imagen guardada offline para paso $pasoId: $localPath")
            actualizarImagenPaso(pasoId, localPath)
            currentPasoIndexForImage = -1
            Log.d("IMAGEN_PASO", "Reset currentPasoIndexForImage = -1")
        }
    }

    // 6. Función para actualizar la imagen de un paso específico
    private fun actualizarImagenPaso(pasoId: Int, imageUrl: String) {
        Log.d("ACTUALIZAR_IMAGEN", "Intentando actualizar imagen del paso $pasoId con URL: $imageUrl")

        val pasoToUpdateIndex = listaPasos.indexOfFirst {
            it.numeroDePaso == pasoId.toString()
        }

        if (pasoToUpdateIndex != -1) {
            val currentPaso = listaPasos[pasoToUpdateIndex]
            Log.d("ACTUALIZAR_IMAGEN", "Paso encontrado. Estado actual - contenido: '${currentPaso.contenido}', archivoFoto: '${currentPaso.archivoFoto}'")

            // Actualizar el campo singular 'archivoFoto'
            val updatedPasoWithArchivoFoto = currentPaso.copy(archivoFoto = imageUrl)

            // Actualizar la lista 'imagenesPasos'
            val updatedImagenesPasosUrls = (currentPaso.imagenesPasos?.toMutableList() ?: mutableListOf()).apply {
                if (!contains(imageUrl)) {
                    add(imageUrl)
                }
            }

            val finalUpdatedPaso = updatedPasoWithArchivoFoto.copy(imagenesPasos = updatedImagenesPasosUrls)
            listaPasos[pasoToUpdateIndex] = finalUpdatedPaso

            Log.d("ACTUALIZAR_IMAGEN", "✓ Imagen actualizada para paso $pasoId")
            Log.d("ACTUALIZAR_IMAGEN", "  - archivoFoto: ${finalUpdatedPaso.archivoFoto}")
            Log.d("ACTUALIZAR_IMAGEN", "  - imagenesPasos: ${finalUpdatedPaso.imagenesPasos}")
            Log.d("ACTUALIZAR_IMAGEN", "  - contenido: '${finalUpdatedPaso.contenido}'")
        } else {
            Log.e("ACTUALIZAR_IMAGEN", "✗ No se encontró el paso con ID: $pasoId")
            Log.e("ACTUALIZAR_IMAGEN", "  Pasos disponibles: ${listaPasos.map { "${it.numeroDePaso}:'${it.contenido}'" }}")
        }
    }

    private fun showMensajeAtras(callback: (AccionDialogoAtras) -> Unit) {
        val inflater = LayoutInflater.from(this)
        val customView = inflater.inflate(R.layout.popup_atras_receta, null)

        val messageTextView: TextView = customView.findViewById(R.id.popupMessage)
        val editarButton: Button = customView.findViewById(R.id.editarButton)
        val reemplazarButton: Button = customView.findViewById(R.id.reemplazarButton)

        val dialog = AlertDialog.Builder(this)
            .setView(customView)
            .setCancelable(false) // Si es false, el usuario DEBE elegir una opción.
            .create()

        editarButton.setOnClickListener {
            dialog.dismiss()
            callback(AccionDialogoAtras.PERMANECER) // Llama al callback con la acción EDITAR
        }

        reemplazarButton.setOnClickListener {
            dialog.dismiss()
            callback(AccionDialogoAtras.VOLVER) // Llama al callback con la acción REEMPLAZAR
        }


        dialog.setOnCancelListener {
            callback(AccionDialogoAtras.PERMANECER)
        }

        dialog.show()
        Log.d("showMensajeAtras", "Diálogo mostrado. Esperando acción del usuario.")

    }
    enum class AccionDialogoAtras {
         VOLVER, PERMANECER // CANCELAR si el diálogo es cancelable y se descarta
    }

}






