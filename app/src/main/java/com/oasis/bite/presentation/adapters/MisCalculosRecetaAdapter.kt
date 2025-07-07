package com.oasis.bite.presentation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.oasis.bite.R
import com.oasis.bite.domain.models.Receta

class MisCalculosRecetaAdapter(
    private var listaRecetas: List<Receta>,
    private val onDeleteClick: (Receta) -> Unit, // Callback para eliminar la receta local
    private val onItemClick: (Receta)-> Unit,
    private val context: Context // Necesitas el contexto para Glide y AlertDialog
) : RecyclerView.Adapter<MisCalculosRecetaAdapter.MisCalculosRecetaViewHolder>() {

    class MisCalculosRecetaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagen: ImageView = itemView.findViewById(R.id.imagenReceta)
        val titulo: TextView = itemView.findViewById(R.id.tituloReceta)
        val detalles: TextView = itemView.findViewById(R.id.detallesReceta)
        val btnEliminarLocal: ImageView = itemView.findViewById(R.id.btnEliminarLocal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MisCalculosRecetaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_receta_mis_calculos, parent, false)
        return MisCalculosRecetaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MisCalculosRecetaViewHolder, position: Int) {
        val receta = listaRecetas[position]

        holder.titulo.text = receta.nombre
        holder.detalles.text = "Para ${receta.porciones} porciones - ${receta.tiempo} - ${receta.dificultad.label}"

        // Carga la imagen con Glide
        receta.imagen?.let { imageUrl ->
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .into(holder.imagen)
        } ?: run {
            holder.imagen.setImageResource(R.drawable.ic_pasta) // Placeholder si no hay imagen
        }

        // Listener para el botón de eliminar
        holder.btnEliminarLocal.setOnClickListener {
            // Mostrar un diálogo de confirmación antes de eliminar
            showConfirmDeleteDialog(receta)
        }

        // Listener para hacer clic en el ítem completo (opcional, si quieres ir al detalle)
        holder.itemView.setOnClickListener {
            onItemClick(receta) // Descomentar si quieres que el clic en el ítem vaya al detalle
        }
    }

    override fun getItemCount(): Int = listaRecetas.size

    fun actualizarRecetas(nuevas: List<Receta>) {
        listaRecetas = nuevas
        notifyDataSetChanged()
    }

    private fun showConfirmDeleteDialog(receta: Receta) {
        AlertDialog.Builder(context)
            .setTitle("Eliminar Receta Local")
            .setMessage("¿Estás seguro de que quieres eliminar '${receta.nombre}' de tus recetas guardadas?")
            .setPositiveButton("Eliminar") { dialog, _ ->
                onDeleteClick(receta) // Invoca el callback para eliminar
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}

