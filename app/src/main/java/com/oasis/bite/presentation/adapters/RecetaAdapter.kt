package com.oasis.bite.presentation.adapters
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.oasis.bite.R
import com.oasis.bite.Receta


class RecetaAdapter(private val listaRecetas: List<Receta>) :
    RecyclerView.Adapter<RecetaAdapter.RecetaViewHolder>() {
    class RecetaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagen: ImageView = itemView.findViewById(R.id.imagenReceta)
        val review: TextView = itemView.findViewById(R.id.reviewReceta)
        val titulo: TextView = itemView.findViewById(R.id.tituloReceta)
        val detalles: TextView = itemView.findViewById(R.id.detallesReceta)
        val favorito: ImageView = itemView.findViewById(R.id.iconoFavorito)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_receta, parent, false)
        return RecetaViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecetaViewHolder, position: Int) {
        val receta = listaRecetas[position]

        holder.titulo.text = receta.titulo
        holder.review.text = receta.review
        holder.detalles.text = "${receta.tiempo} - ${receta.dificultad} - Por ${receta.autor}"

        // Cargamos la imagen usando el resource ID directamente
        holder.imagen.setImageResource(receta.imagenResId)


        // Puedes setear una imagen por defecto o gestionar favorito con lógica aparte
        holder.favorito.setImageResource(R.drawable.ic_corazonvacio)
    }

    override fun getItemCount(): Int = listaRecetas.size

}
