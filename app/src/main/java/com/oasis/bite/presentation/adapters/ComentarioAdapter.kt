package com.oasis.bite.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.oasis.bite.R
import com.oasis.bite.domain.models.Comentario

class ComentarioAdapter (
    private var comentarios: List<Comentario?>
    ) : RecyclerView.Adapter<ComentarioAdapter.ComentarioViewHolder>() {
        class ComentarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val titulo: TextView = itemView.findViewById(R.id.itemComentarioTitulo)
            val valoracion: TextView = itemView.findViewById(R.id.puntuacionComentario)
            val reseña: TextView = itemView.findViewById(R.id.textoComentario)
            val userName: TextView = itemView.findViewById(R.id.usuarioComentario)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComentarioViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comentario, parent, false)
            val layoutParams = view.layoutParams
            layoutParams.width = (parent.context.resources.displayMetrics.density * 300).toInt()
            view.layoutParams = layoutParams

            return ComentarioViewHolder(view)
        }

        override fun onBindViewHolder(holder: ComentarioViewHolder, position: Int) {
            val comentario = comentarios[position]

            holder.titulo.text = comentario?.titulo
            holder.valoracion.text = comentario?.valoracion.toString()
            holder.reseña.text = comentario?.reseña
            holder.userName.text = comentario?.userName
            }

        override fun getItemCount(): Int = comentarios.size



        fun actualizarComentarios(nuevas: List<Comentario>) {
            comentarios = nuevas
            notifyDataSetChanged()
        }
    }




