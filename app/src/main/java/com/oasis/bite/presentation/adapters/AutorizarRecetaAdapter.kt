package com.oasis.bite.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.oasis.bite.R
import com.oasis.bite.domain.models.Receta
import com.oasis.bite.domain.models.RecetaStatus
import com.oasis.bite.presentation.ui.home.HomeViewModel

class AutorizarRecetaAdapter (
    private var listaRecetas: List<Receta>,
    private val onItemClick: (Receta)-> Unit,
    private val  homeViewModel: HomeViewModel

    ) :
    RecyclerView.Adapter<AutorizarRecetaAdapter.RecetaViewHolder>() {

        class RecetaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imagen: ImageView = itemView.findViewById(R.id.imagenReceta)
            val titulo: TextView = itemView.findViewById(R.id.tituloReceta)
            val detalles: TextView = itemView.findViewById(R.id.detallesReceta)
            val aprobar : Button = itemView.findViewById(R.id.aprobar)
            val rechazar : Button = itemView.findViewById(R.id.rechazar)
        }

        //Se ejecuta una sola vez por cada nuevo ítem que se necesita visualizar, y crea la vista de cada elemento.
        //Devuelve un ViewHolder que representa una fila del RecyclerView.

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetaViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_receta_autorizar, parent, false)
            return RecetaViewHolder(view)
        }

        //Es la que rellena los datos en la vista ya creada. Se llama muchas veces, cada vez que una celda aparece en pantalla.

        override fun onBindViewHolder(holder: RecetaViewHolder, position: Int) {
            val receta = listaRecetas[position]
            var estado : RecetaStatus
            holder.titulo.text = receta.nombre
            holder.detalles.text = "${receta.tiempo} - ${receta.dificultad} - Por ${receta.username}"
            Glide.with(holder.itemView.context)
                .load(receta.imagen)
                .into(holder.imagen)
            holder.itemView.setOnClickListener {
                onItemClick(receta)
            }
            holder.aprobar.setOnClickListener {
                estado = RecetaStatus.APROBADA
                homeViewModel.editarEstadoReceta(receta.id,estado.label.toString()) // cambiar a RECETA
            }
            holder.rechazar.setOnClickListener {
                estado = RecetaStatus.RECHAZADA
                homeViewModel.editarEstadoReceta(receta.id,estado.label.toString()) // CAMBIAR A RECETA
            }
        }

        //Le dice al Recycler cuántos elementos tiene que mostrar en total. si devuelve 0 no muestra nada
        override fun getItemCount(): Int = listaRecetas.size

        //Es una función propia que vos definiste (no del Adapter base), que sirve para reemplazar la lista actual de recetas por una nueva, y refrescar la vista

        fun actualizarRecetas(nuevas: List<Receta>) {
            listaRecetas = nuevas
            notifyDataSetChanged()
        }


    }