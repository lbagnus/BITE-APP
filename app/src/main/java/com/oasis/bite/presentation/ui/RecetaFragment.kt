package com.oasis.bite.presentation.ui

import android.content.Context
import android.content.Intent
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
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.oasis.bite.ComentarioActivity
import com.oasis.bite.R
import com.oasis.bite.databinding.FragmentRecetaBinding
import com.oasis.bite.domain.models.User
import com.oasis.bite.presentation.adapters.MediaAdapter
import com.oasis.bite.presentation.adapters.ComentarioAdapter
import com.oasis.bite.presentation.viewmodel.RecetaViewModel
import com.oasis.bite.presentation.viewmodel.RecetaViewModelFactory

class RecetaFragment : Fragment() {

    private var _binding: FragmentRecetaBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: RecetaViewModel
    private lateinit var comentarioAdapter: ComentarioAdapter
    private var idsFavoritos: List<Int> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecetaBinding.inflate(inflater, container, false)

        val factory = RecetaViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(this, factory).get(RecetaViewModel::class.java)
        val usuario = getUsuarioLogueado(requireContext())
        usuario?.let {
            viewModel.getRecetasFavoritos(it.email) { favoritos ->
                idsFavoritos = favoritos.map { receta -> receta.id }
            }
        }
        binding.mainContentScrollView.visibility = View.GONE
        binding.progressBarCargando.visibility = View.VISIBLE
        val recetaId = arguments?.getInt("recetaId") ?: -1
        if (recetaId != -1) {
            limpiarCampos() // LIMPIAR CAMPOS ANTES DE CARGAR
            viewModel.cargarReceta(recetaId.toString())
        }

        Log.d("RecetaFragment", "Receta ID recibido: $recetaId")

        val botonCalculadora = binding.calculadora
        botonCalculadora.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("recetaId", recetaId)
            }
            findNavController().navigate(R.id.CalculadoraFragment, bundle)
        }

        viewModel.receta.observe(viewLifecycleOwner) { receta ->
            binding.progressBarCargando.visibility = View.GONE
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

                binding.mainContentScrollView.visibility = View.VISIBLE
                if (idsFavoritos.contains(it.id)) {
                    binding.btnFavorito.setImageResource(R.drawable.favorite_filled)
                } else {
                    binding.btnFavorito.setImageResource(R.drawable.favorite_border)
                }

                binding.btnFavorito.setOnClickListener {
                    if (idsFavoritos.contains(receta.id)) {
                        viewModel.eliminarRecetaFavorito(usuario?.email.toString(), receta.id)
                        binding.btnFavorito.setImageResource(R.drawable.favorite_border)
                    } else {
                        viewModel.agregarRecetaFavorito(usuario?.email.toString(), receta.id)
                        binding.btnFavorito.setImageResource(R.drawable.favorite_filled)
                    }
                }

                // INGREDIENTES
                val layoutIngredientes = binding.layoutIngredientes
                layoutIngredientes.removeAllViews()
                val inflater = LayoutInflater.from(requireContext())

                it.ingredientes.forEach { ingrediente ->
                    val itemView = inflater.inflate(R.layout.item_ingrediente, layoutIngredientes, false)

                    val txtIngrediente = itemView.findViewById<TextView>(R.id.txtNombreIngrediente)
                    txtIngrediente.text = ingrediente.nombre

                    val txtCantidad = itemView.findViewById<TextView>(R.id.txtCantidad)
                    txtCantidad.text = ingrediente.cantidad.toString()

                    val txtUnidad = itemView.findViewById<TextView>(R.id.txtUnidad)
                    txtUnidad.text = ingrediente.unidad

                    layoutIngredientes.addView(itemView)
                }

                // PASOS
                val layoutPasos = binding.layoutPasos
                layoutPasos.removeAllViews()
                val inflater2 = LayoutInflater.from(requireContext())

                it.pasos.forEach { paso ->
                    val itemView = inflater2.inflate(R.layout.item_paso_receta, layoutPasos, false)

                    val txtNumeroPaso = itemView.findViewById<TextView>(R.id.txtPaso)
                    txtNumeroPaso.text = "${paso.numeroDePaso}. ${paso.contenido}"

                    val recyclerMediaPaso = itemView.findViewById<RecyclerView>(R.id.recyclerMediaPaso)

                    val listaMultimediaPaso = paso.imagenesPasos.orEmpty()
                    if (listaMultimediaPaso.isNotEmpty()) {
                        val adapter = MediaAdapter(listaMultimediaPaso)
                        recyclerMediaPaso.adapter = adapter
                        recyclerMediaPaso.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                        recyclerMediaPaso.visibility = View.VISIBLE
                    } else {
                        recyclerMediaPaso.visibility = View.GONE
                    }

                    layoutPasos.addView(itemView)
                }

                // COMENTARIOS
                val comentarios = it.comentarios.orEmpty()
                comentarioAdapter = ComentarioAdapter(comentarios)
                binding.recyclerResenia.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                binding.recyclerResenia.adapter = comentarioAdapter

                if (comentarios.isNotEmpty()) {
                    binding.recyclerResenia.visibility = View.VISIBLE
                    binding.tvSinComentarios.visibility = View.GONE
                    comentarioAdapter.actualizarComentarios(comentarios)
                } else {
                    binding.recyclerResenia.visibility = View.GONE
                    binding.tvSinComentarios.visibility = View.VISIBLE
                }

                // MULTIMEDIA PRINCIPAL
                val listaMultimedia = it.imagenes.orEmpty()
                val adapter = MediaAdapter(listaMultimedia)
                binding.recyclerMedia.adapter = adapter
                binding.recyclerMedia.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

                // BOTÓN AGREGAR RESEÑA
                binding.botonAgregarResenia.setOnClickListener {
                    val intent = Intent(requireContext(), ComentarioActivity::class.java)
                    intent.putExtra("recetaId", recetaId)
                    intent.putExtra("tituloReceta", receta.nombre)
                    intent.putExtra("usuarioEmail", usuario.email)
                    intent.putExtra("recetaAutor", receta.username)
                    Log.d("comentarioActivity", "recetaID: $recetaId")
                    startActivity(intent)
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

    private fun limpiarCampos() {
        binding.recetaTitulo.text = ""
        binding.recetaAutor.text = ""
        binding.dificultad.text = ""
        binding.porciones.text = ""
        binding.personas2.text = ""
        binding.tiempo.text = ""
        binding.recetaDescripcion.text = ""
        binding.reviews.text = ""
        binding.puntuacion2.text = ""
        binding.puntuacion.text = ""
        binding.layoutIngredientes.removeAllViews()
        binding.layoutPasos.removeAllViews()
        binding.recyclerResenia.adapter = null
        binding.recyclerMedia.adapter = null
        binding.tvSinComentarios.visibility = View.GONE
        binding.recyclerResenia.visibility = View.GONE
    }

    fun getUsuarioLogueado(context: Context): User {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val json = prefs.getString("usuario_logueado", null)
        return json.let { Gson().fromJson(it, User::class.java) }
    }
}








