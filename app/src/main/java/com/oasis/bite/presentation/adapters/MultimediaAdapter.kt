package com.oasis.bite.presentation.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.oasis.bite.R

class MultimediaAdapter(
    private val items: List<Uri>,
    private val onAddClick: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ADD = 0
        private const val TYPE_ITEM = 1
    }

    override fun getItemCount(): Int = items.size + 1  // +1 para el botón de agregar

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_ADD else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_multimedia, parent, false)
        return if (viewType == TYPE_ADD) AddViewHolder(view) else ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AddViewHolder) {
            holder.bind(onAddClick)
        } else if (holder is ItemViewHolder) {
            holder.bind(items[position - 1]) // -1 porque el 0 es el botón
        }
    }

    class AddViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(onClick: () -> Unit) {
            val addLayout = itemView.findViewById<View>(R.id.addItemLayout)
            val image = itemView.findViewById<View>(R.id.imagePreview)

            addLayout.visibility = View.VISIBLE
            image.visibility = View.GONE

            addLayout.setOnClickListener { onClick() }
        }
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(uri: Uri) {
            val addLayout = itemView.findViewById<View>(R.id.addItemLayout)
            val image = itemView.findViewById<ImageView>(R.id.imagePreview)

            addLayout.visibility = View.GONE
            image.visibility = View.VISIBLE
            image.setImageURI(uri)
        }
    }
}

