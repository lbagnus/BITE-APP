package com.oasis.bite.presentation.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.oasis.bite.R
import com.oasis.bite.databinding.FragmentRecetaBinding
import com.oasis.bite.presentation.adapters.ComentarioAdapter
import com.oasis.bite.presentation.viewmodel.RecetaViewModel

class RecetaFragment : Fragment() {

    private var _binding: FragmentRecetaBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecetaViewModel
    private lateinit var comentarioAdapter: ComentarioAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecetaBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(RecetaViewModel::class.java)

        val recetaId = arguments?.getInt("recetaId") ?: -1
        if (recetaId != -1) {
            viewModel.cargarReceta(recetaId.toString())
        }
        Log.d("RecetaFragment", "Receta ID recibido: $recetaId")

        viewModel.receta.observe(viewLifecycleOwner) { receta ->
            receta?.let {
                binding.recetaTitulo.text = it.nombre
                binding.recetaAutor.text = "Por ${it.username}"
                binding.dificultad.text = it.dificultad.toString()
                binding.porciones.text = it.porciones.toString()
                binding.personas2.text = "Para ${it.porciones}"
                binding.tiempo.text = it.tiempo ?: "-"
                binding.recetaDescripcion.text = it.descripcion
                binding.reviews.text = it.reviewCount.toString()
                binding.puntuacion2.text = it.averageRating.toString()
                binding.puntuacion.text = it.averageRating.toString()

                // Mostrar ingredientes con LinearLayout
                // Mostrar ingredientes usando item_ingrediente.xml
                val layoutIngredientes = binding.layoutIngredientes
                layoutIngredientes.removeAllViews()
                val inflater = LayoutInflater.from(requireContext())

                it.ingredientes.forEach { ingrediente ->
                    val itemView = inflater.inflate(R.layout.item_ingrediente, layoutIngredientes, false)

                    // Suponiendo que item_ingrediente.xml tiene un TextView con id txtIngrediente
                    val txtIngrediente = itemView.findViewById<TextView>(R.id.txtNombreIngrediente)
                    txtIngrediente.text = ingrediente.nombre

                    val txtCantidad = itemView.findViewById<TextView>(R.id.txtCantidad)
                    txtCantidad.text = ingrediente.cantidad.toString()

                    val txtUnidad = itemView.findViewById<TextView>(R.id.txtUnidad)
                    txtUnidad.text = ingrediente.unidad

                    layoutIngredientes.addView(itemView)
                }
                val layoutPasos = binding.layoutPasos
                layoutPasos.removeAllViews()
                val inflater2 = LayoutInflater.from(requireContext())

                it.pasos.forEach { paso ->
                    val itemView = inflater.inflate(R.layout.item_paso_receta, layoutPasos, false)

                    // Suponiendo que item_ingrediente.xml tiene un TextView con id txtIngrediente
                    val txtNumeroPaso = itemView.findViewById<TextView>(R.id.txtNumeroPaso)
                    txtNumeroPaso.text = paso.numeroDePaso

                    val txtContenido = itemView.findViewById<TextView>(R.id.txtContenido)
                    txtContenido.text = paso.contenido

                    layoutPasos.addView(itemView)
                }
                val comentarios = it.comentarios.orEmpty()
                comentarioAdapter = ComentarioAdapter(comentarios)
                binding.recyclerResenia.layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                binding.recyclerResenia.adapter = comentarioAdapter

                if (comentarios.isNotEmpty()) {
                    binding.recyclerResenia.visibility = View.VISIBLE
                    binding.tvSinComentarios.visibility = View.GONE
                    binding.recyclerResenia.adapter = comentarioAdapter
                    comentarioAdapter.actualizarComentarios(comentarios)
                } else {
                    binding.recyclerResenia.visibility = View.GONE
                    binding.tvSinComentarios.visibility = View.VISIBLE
                }


            }
        }
        binding.btnVolver.setOnClickListener {
            findNavController().navigateUp()
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}





