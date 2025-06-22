package com.oasis.bite.presentation.adapters
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.oasis.bite.R
import com.oasis.bite.domain.models.Receta
import com.oasis.bite.domain.models.RecetaStatus
import com.oasis.bite.domain.models.User
import com.oasis.bite.presentation.ui.home.HomeViewModel
import kotlin.getValue

class RecetaAdapter(
    private var listaRecetas: List<Receta>,
    private val onItemClick: (Receta)-> Unit,
    private var esFavorito: (Receta) -> Boolean,
    private val usuario : User,
    private val homeViewModel: HomeViewModel,
    private val esPropio: Boolean

    ) :
    RecyclerView.Adapter<RecetaAdapter.RecetaViewHolder>() {

        class RecetaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imagen: ImageView = itemView.findViewById(R.id.imagenReceta)
            val titulo: TextView = itemView.findViewById(R.id.tituloReceta)
            val detalles: TextView = itemView.findViewById(R.id.detallesReceta)
            val favorito: ImageView = itemView.findViewById(R.id.iconoFavorito)
            val ranking : TextView = itemView.findViewById(R.id.reviewReceta)
            val editar : ImageView = itemView.findViewById(R.id.iconoEditar)
        }

        //Se ejecuta una sola vez por cada nuevo ítem que se necesita visualizar, y crea la vista de cada elemento.
        //Devuelve un ViewHolder que representa una fila del RecyclerView.

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_receta, parent, false)
        return RecetaViewHolder(view)
        }

        //Es la que rellena los datos en la vista ya creada. Se llama muchas veces, cada vez que una celda aparece en pantalla.

        override fun onBindViewHolder(holder: RecetaViewHolder, position: Int) {
            val receta = listaRecetas[position]

            holder.titulo.text = receta.nombre
            holder.detalles.text = "${receta.tiempo} - ${receta.dificultad} - Por ${receta.username}"
            holder.ranking.text = receta.averageRating.toString()
            Glide.with(holder.itemView.context)
                .load(receta.imagen)
                .into(holder.imagen)

            if(esPropio){
                if(receta.estado == RecetaStatus.APROBADA){
                holder.favorito.setImageResource(R.drawable.delete_outlined)
                holder.editar.visibility = View.VISIBLE
                }else{
                    holder.favorito.setImageResource(R.drawable.eye)
                    holder.ranking.text = "Pendiente"
                }

            }else {

                if (esFavorito(receta)) {
                    holder.favorito.setImageResource(R.drawable.favorite_filled)
                } else {
                    holder.favorito.setImageResource(R.drawable.ic_corazonvacio)
                }
                holder.favorito.setOnClickListener {
                    if (esFavorito(receta)) {
                        homeViewModel.eliminarRecetaFavorito(usuario.email.toString(), receta.id)
                        holder.favorito.setImageResource(R.drawable.ic_corazonvacio)
                    } else {
                        homeViewModel.agregarRecetaFavorito(usuario.email.toString(), receta.id)
                        holder.favorito.setImageResource(R.drawable.favorite_filled)
                    }
                }
            }

            holder.itemView.setOnClickListener {
                onItemClick(receta)
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










