package com.oasis.bite.presentation.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.oasis.bite.Category
import com.oasis.bite.R

class CategoryAdapter(
    private val categories: List<Category>
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconoCategoria: ImageView = itemView.findViewById(R.id.iconoCategoria)
        val tituloCategoria: TextView = itemView.findViewById(R.id.tituloCategoria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        Log.d("ADAPTER", "onCreateViewHolder llamado")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_categoria, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val categoria = categories[position]
        holder.iconoCategoria.setImageResource(categoria.iconoResId)
        holder.tituloCategoria.text = categoria.nombre
    }

    override fun getItemCount(): Int = categories.size
}


