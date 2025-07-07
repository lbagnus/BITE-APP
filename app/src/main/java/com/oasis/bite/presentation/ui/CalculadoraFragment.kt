package com.oasis.bite.presentation.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.oasis.bite.R
import com.oasis.bite.databinding.DivisionCalculadoraBinding
import com.oasis.bite.databinding.ItemIngredienteDivisionBinding
import com.oasis.bite.domain.models.Ingrediente
import com.oasis.bite.domain.models.Receta
import com.oasis.bite.presentation.viewmodel.RecetaViewModel
import com.oasis.bite.presentation.viewmodel.RecetaViewModelFactory

class CalculadoraFragment: Fragment() {
    private var _binding: DivisionCalculadoraBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecetaViewModel
    private lateinit var inputPorciones: EditText
    private lateinit var layoutIngredientesCalculadora: LinearLayout
    private lateinit var recetaTitulo: TextView
    private lateinit var recetaAutor: TextView
    private var ingredientesCalculadora: MutableList<IngredienteCalculadora> = mutableListOf()
    private var originalPortions: Float = 1.0f // Porciones originales de la receta
    private var currentPortions: Float = 1.0f // Porciones actuales calculadas
    private var ignoreTextChanges = false // Para evitar bucles infinitos en TextWatcher
    private lateinit var originalReceta: Receta
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DivisionCalculadoraBinding.inflate(inflater, container, false)

        val factory = RecetaViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(this, factory).get(RecetaViewModel::class.java)
        val recetaId = arguments?.getInt("recetaId") ?: -1
        Log.d("RecetaFragment", "Receta ID recibido: $recetaId")
        if (recetaId != -1) {
            viewModel.cargarReceta(recetaId.toString())
        }
        Log.d("RecetaFragment", "Receta ID recibido: $recetaId")

        inputPorciones = binding.inputPorciones
        layoutIngredientesCalculadora = binding.layoutIngredientesCalculadora
        recetaTitulo = binding.recetaTitulo
        recetaAutor = binding.recetaAutor
        val botonCancelar = binding.btnVolver
        botonCancelar.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.receta.observe(viewLifecycleOwner) { receta ->
            receta?.let {loadedReceta ->
                originalReceta = loadedReceta
                inputPorciones.setText(receta.porciones.toString())
                recetaTitulo.text = receta.nombre
                recetaAutor.text = receta.username

                originalPortions = receta.porciones.toFloat()
                currentPortions = originalPortions
                ingredientesCalculadora = receta.ingredientes.map {
                    IngredienteCalculadora(
                        id = 0, // Asume un ID si lo tienen
                        nombre = it.nombre,
                        cantidadOriginal = it.cantidad,
                        unidad = it.unidad,
                        cantidadActual = it.cantidad
                    )
                }.toMutableList()
                setupListeners()
                displayIngredients()
            }
        }
        val btnGuardarReceta = binding.btnGuardarRecetaAjustada // Asegúrate de que el ID sea correcto
        btnGuardarReceta.setOnClickListener {
            saveAdjustedRecipe() // Llama a la función para guardar
        }

        // Observar el estado de guardado del ViewModel para feedback al usuario
        viewModel.saveAdjustedRecipeStatus.observe(viewLifecycleOwner) { isSuccess ->
            // Obtener el mensaje del ViewModel. Si es null, usar un mensaje por defecto.
            val message = viewModel.messageForUser.value ?: "Operación completada. (Sin mensaje específico)" // <-- ¡CAMBIO CLAVE AQUÍ!

            if (isSuccess) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                // Opcional: Podrías cerrar la calculadora o actualizar la UI
            } else {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            }
        }


        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    data class IngredienteCalculadora(
        val id: Int, // Si los ingredientes tienen ID
        val nombre: String,
        val cantidadOriginal: Float,
        val unidad: String,
        var cantidadActual: Float // ¡Esta será la cantidad que se recalcula!
    )
    private fun setupListeners() {
        // Listener para el input de porciones
        inputPorciones.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (ignoreTextChanges) return
                val newPortions = s.toString().toFloatOrNull()
                if (newPortions != null && newPortions > 0) {
                    recalculateByPortions(newPortions)
                } else if (s.toString().isBlank()) {
                     Toast.makeText(requireContext(), "Ingresa un número válido de porciones", Toast.LENGTH_SHORT).show()
                }
            }
        })

        // OnFocusChangeListener para porciones, para recalcular solo cuando se pierde el foco
        inputPorciones.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val newPortions = inputPorciones.text.toString().toFloatOrNull()
                if (newPortions != null && newPortions > 0) {
                    recalculateByPortions(newPortions)
                } else if (inputPorciones.text.toString().isBlank() || newPortions == 0f) {
                    // Si el usuario deja vacío o 0, puedes resetear al valor original o mostrar error
                    inputPorciones.setText(originalPortions.toInt().toString())
                    recalculateByPortions(originalPortions) // Vuelve a la base si es inválido
                }
            }
        }
    }
    private fun displayIngredients() {
        layoutIngredientesCalculadora.removeAllViews() // Limpiar vistas existentes
        val inflater = LayoutInflater.from(requireContext())

        ingredientesCalculadora.forEachIndexed { index, ingrediente ->
            val ingredientView = inflater.inflate(R.layout.item_ingrediente_division, layoutIngredientesCalculadora, false)
            val bindingItem = ItemIngredienteDivisionBinding.bind(ingredientView)

            bindingItem.nombreIngrediente.text = ingrediente.nombre
            bindingItem.unidadIngrediente.text = ingrediente.unidad
            bindingItem.inputCantidadIngrediente.setText(ingrediente.cantidadActual.toString())

            // Configurar listener para cada ingrediente
            bindingItem.inputCantidadIngrediente.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (ignoreTextChanges) return
                    val newAmount = s.toString().toFloatOrNull()
                    if (newAmount != null && newAmount > 0) {
                        recalculateByIngredient(index, newAmount) // Recalcular por este ingrediente
                    } else if (s.toString().isBlank()) {
                        // Si el campo está vacío, no hacer nada o resetear si pierde foco
                    }
                }
            })

            // OnFocusChangeListener para ingredientes, para recalcular solo cuando se pierde el foco
            bindingItem.inputCantidadIngrediente.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val newAmount = bindingItem.inputCantidadIngrediente.text.toString().toFloatOrNull()
                    if (newAmount != null && newAmount > 0) {
                        recalculateByIngredient(index, newAmount)
                    } else if (bindingItem.inputCantidadIngrediente.text.toString().isBlank() || newAmount == 0f) {
                        // Si el usuario deja vacío o 0, resetear a su cantidad calculada actual
                        ignoreTextChanges = true // Prevenir bucle
                        bindingItem.inputCantidadIngrediente.setText(ingrediente.cantidadActual.toString())
                        ignoreTextChanges = false
                    }
                }
            }

            layoutIngredientesCalculadora.addView(ingredientView)
        }
    }
    private fun recalculateByPortions(newPortions: Float) {
        if (originalPortions == 0f) return // Evitar división por cero
        val scaleFactor = newPortions / originalPortions
        Log.d("Calculadora", "Recalculando por porciones: $newPortions. Factor: $scaleFactor")
        applyScaleFactor(scaleFactor, true) // True indica que la fuente es porciones
    }

    private fun recalculateByIngredient(changedIngredientIndex: Int, newAmount: Float) {
        if (changedIngredientIndex < 0 || changedIngredientIndex >= ingredientesCalculadora.size) return
        val changedIngredient = ingredientesCalculadora[changedIngredientIndex]

        if (changedIngredient.cantidadOriginal == 0f) {
            // Si la cantidad original es 0, no podemos usarla como base para recalcular
            // Podrías mostrar un error o simplemente no recalcular
            Toast.makeText(requireContext(), "No se puede recalcular usando un ingrediente con cantidad original de cero.", Toast.LENGTH_SHORT).show()
            return
        }

        val scaleFactor = newAmount / changedIngredient.cantidadOriginal
        Log.d("Calculadora", "Recalculando por ingrediente: ${changedIngredient.nombre}, nueva cantidad: $newAmount. Factor: $scaleFactor")
        applyScaleFactor(scaleFactor, false) // False indica que la fuente es un ingrediente
    }

    private fun applyScaleFactor(scaleFactor: Float, sourceIsPortions: Boolean) {
        ignoreTextChanges = true // Desactivar TextWatchers temporalmente

        // Recalcular porciones
        currentPortions = originalPortions * scaleFactor
        if (sourceIsPortions) {
            // No actualizamos el inputPorciones si la fuente fue el mismo inputPorciones
            // porque ya contiene el valor que lo desencadenó
            inputPorciones.setText(currentPortions.toInt().toString())
        } else {
            // Actualizar el input de porciones si la fuente fue un ingrediente
            inputPorciones.setText(currentPortions.toInt().toString())
        }

        // Recalcular cantidades de todos los ingredientes
        ingredientesCalculadora.forEachIndexed { index, ingrediente ->
            ingrediente.cantidadActual = ingrediente.cantidadOriginal * scaleFactor

            // Encontrar el EditText correspondiente para actualizar
            val ingredientView = layoutIngredientesCalculadora.getChildAt(index)
            if (ingredientView != null) {
                val bindingItem = ItemIngredienteDivisionBinding.bind(ingredientView)
                // Solo actualiza el EditText si NO fue el ingrediente que se acaba de editar
                if (sourceIsPortions || index != findIndexOfFocusedIngredient()) { // Si la fuente es porciones, actualiza todos
                    bindingItem.inputCantidadIngrediente.setText(ingrediente.cantidadActual.toString())
                }
            }
        }
        ignoreTextChanges = false // Reactivar TextWatchers

        Log.d("Calculadora", "Nueva Cantidad de Porciones Calculada: ${currentPortions.toInt()}")
    }

    private fun findIndexOfFocusedIngredient(): Int {
        for (i in 0 until layoutIngredientesCalculadora.childCount) {
            val ingredientView = layoutIngredientesCalculadora.getChildAt(i)
            val bindingItem = ItemIngredienteDivisionBinding.bind(ingredientView)
            if (bindingItem.inputCantidadIngrediente.hasFocus()) {
                return i
            }
        }
        return -1
    }

    private fun saveAdjustedRecipe() {
        // 1. Construir la Receta (dominio) con los valores recalculados
        val adjustedIngredients = ingredientesCalculadora.map {
            Ingrediente(nombre = it.nombre, cantidad = it.cantidadActual, unidad = it.unidad) // Mapea de nuevo a tu Ingrediente de dominio
        }

        // Es vital tener la receta original (originalReceta) para copiar sus demás propiedades
        val recetaAjustada = originalReceta.copy(
            porciones = currentPortions.toInt(), // Actualiza las porciones
            ingredientes = adjustedIngredients, // Reemplaza los ingredientes con los ajustados
            // Puedes añadir aquí un campo extra si quieres marcarla como "ajustada"
            // Por ejemplo, si Receta tuviera un campo 'tipoReceta: String'
            // tipoReceta = "ajustada_por_usuario"
        )

        Log.d("Calculadora", "Receta ajustada para guardar: $recetaAjustada")

        // 2. Llamar al ViewModel para guardar
        viewModel.saveAdjustedRecipe(recetaAjustada)
    }

}