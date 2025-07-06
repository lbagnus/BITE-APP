package com.oasis.bite.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.oasis.bite.R
import com.oasis.bite.domain.models.Comentario
import com.oasis.bite.domain.models.Receta
import com.oasis.bite.domain.models.RecetaStatus
import com.oasis.bite.presentation.ui.home.HomeViewModel

class AutorizarComentarioAdapter(
    private var comentarios: List<Comentario>,
    private val homeViewModel: HomeViewModel
) : RecyclerView.Adapter<AutorizarComentarioAdapter.ComentarioViewHolder>() {
    class ComentarioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo: TextView = itemView.findViewById(R.id.itemComentarioTitulo)
        val valoracion: TextView = itemView.findViewById(R.id.puntuacionComentario)
        val reseña: TextView = itemView.findViewById(R.id.textoComentario)
        val userName: TextView = itemView.findViewById(R.id.usuarioComentario)
        val aprobar : Button = itemView.findViewById(R.id.btnAprobar)
        val rechazar : Button = itemView.findViewById(R.id.btnRechazar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComentarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comentario_autorizar, parent, false)
        return ComentarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComentarioViewHolder, position: Int) {
        val comentario = comentarios[position]
        var estado : RecetaStatus
        holder.titulo.text = comentario.titulo
        holder.valoracion.text = comentario.valoracion.toString()
        holder.reseña.text = comentario.reseña
        holder.userName.text = comentario.userName
        holder.aprobar.setOnClickListener {
            estado = RecetaStatus.APROBADA
            homeViewModel.editarEstadoComentario(comentario.id,estado.label.toString())
        }
        holder.rechazar.setOnClickListener {
            estado = RecetaStatus.RECHAZADA
            homeViewModel.editarEstadoComentario(comentario.id,estado.label.toString())
        }
    }

    override fun getItemCount(): Int = comentarios.size



    fun actualizarComentarios(nuevas: List<Comentario>) {
        comentarios = nuevas
        notifyDataSetChanged()
    }
}