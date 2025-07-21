package com.oasis.bite.presentation.adapters
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.oasis.bite.AgregarRecetaActivity
import com.oasis.bite.AgregarRecetaActivity.AccionDialogoAtras
import com.oasis.bite.ForgotPasswordActivity
import com.oasis.bite.R
import com.oasis.bite.VerifyCodeActivity
import com.oasis.bite.domain.models.Receta
import com.oasis.bite.domain.models.RecetaStatus
import com.oasis.bite.domain.models.Role
import com.oasis.bite.domain.models.User
import com.oasis.bite.presentation.ui.home.HomeViewModel
import kotlin.getValue

class RecetaAdapter(
    private var listaRecetas: List<Receta>,
    private val onItemClick: (Receta)-> Unit,
    private var esFavorito: (Receta) -> Boolean,
    private val usuario : User,
    private val homeViewModel: HomeViewModel,
    private val esPropio: Boolean,
    private val context: Context

    ) :
    RecyclerView.Adapter<RecetaAdapter.RecetaViewHolder>() {

        class RecetaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imagen: ImageView = itemView.findViewById(R.id.imagenReceta)
            val titulo: TextView = itemView.findViewById(R.id.tituloReceta)
            val detalles: TextView = itemView.findViewById(R.id.detallesReceta)
            val favorito: ImageView = itemView.findViewById(R.id.favorito)
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

        @SuppressLint("SuspiciousIndentation")
        override fun onBindViewHolder(holder: RecetaViewHolder, position: Int) {
            val receta = listaRecetas[position]

            holder.itemView.setOnClickListener {
                onItemClick(receta)
            }

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
                    holder.editar.setOnClickListener {
                        val intent = Intent(context, AgregarRecetaActivity::class.java).apply {
                            putExtra("tituloReceta", receta.nombre)
                            putExtra("usuarioEmail", usuario.email)
                            putExtra("isEditando", true)
                            putExtra("reemplaza", false)
                            putExtra("idReceta", receta.id.toString())
                            Log.d("Receta AdapterEditar", receta.id.toString())
                        }
                        context.startActivity(intent)
                    }
                    holder.favorito.setOnClickListener {
                        // aca podria ir un eliminar o un pop up para eliminar
                        showMensajeEliminar  { accion ->
                            when (accion) {
                                AccionDialogoAtras.ELIMINAR -> {
                                   homeViewModel.eliminarReceta(receta.id.toString())
                                }
                                AccionDialogoAtras.CANCELAR -> {
                                    // no hace nada
                                }
                            }}
                    }
                }else{
                    holder.favorito.setImageResource(R.drawable.eye)
                    holder.ranking.text = "Pendiente"
                }

            }else
                holder.favorito.isSelected = esFavorito(receta)

            if(usuario.role == Role.GUEST){
                holder.favorito.isEnabled = false
            }

            holder.favorito.setOnClickListener {
                if (esFavorito(receta)) {
                    homeViewModel.eliminarRecetaFavorito(usuario.email.toString(), receta.id)
                    holder.favorito.isSelected = false
                    val index = listaRecetas.indexOf(receta)
                    if (index != -1) {
                        listaRecetas = listaRecetas.toMutableList().apply { removeAt(index) }
                        notifyItemRemoved(index)}

                } else {
                    homeViewModel.agregarRecetaFavorito(usuario.email.toString(), receta.id)
                    holder.favorito.isSelected = true
                }
            }
        }

        //Le dice al Recycler cuántos elementos tiene que mostrar en total. si devuelve 0 no muestra nada
        override fun getItemCount(): Int = listaRecetas.size

        //Es una función propia que vos definiste (no del Adapter base), que sirve para reemplazar la lista actual de recetas por una nueva, y refrescar la vista

        fun actualizarRecetas(nuevas: List<Receta>) {
            listaRecetas = nuevas
            notifyDataSetChanged()
        }

    private fun showMensajeEliminar(callback: (AccionDialogoAtras) -> Unit) {
        val inflater = LayoutInflater.from(context)
        val customView = inflater.inflate(R.layout.popup_eliminar_receta, null)

        val messageTextView: TextView = customView.findViewById(R.id.popupMessage)
        val editarButton: Button = customView.findViewById(R.id.editarButton)
        val reemplazarButton: Button = customView.findViewById(R.id.reemplazarButton)

        val dialog = AlertDialog.Builder(context)
            .setView(customView)
            .setCancelable(false) // Si es false, el usuario DEBE elegir una opción.
            .create()

        editarButton.setOnClickListener {
            dialog.dismiss()
            callback(AccionDialogoAtras.CANCELAR) // Llama al callback con la acción EDITAR
        }

        reemplazarButton.setOnClickListener {
            dialog.dismiss()
            callback(AccionDialogoAtras.ELIMINAR) // Llama al callback con la acción REEMPLAZAR
        }


        dialog.setOnCancelListener {
            callback(AccionDialogoAtras.CANCELAR)
        }

        dialog.show()
        Log.d("showMensajeAtras", "Diálogo mostrado. Esperando acción del usuario.")

    }
    enum class AccionDialogoAtras {
        ELIMINAR, CANCELAR // CANCELAR si el diálogo es cancelable y se descarta
    }



    }










