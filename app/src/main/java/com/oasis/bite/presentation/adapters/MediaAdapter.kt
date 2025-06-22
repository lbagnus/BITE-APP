package com.oasis.bite.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.oasis.bite.R
import com.oasis.bite.domain.models.MediaType
import com.oasis.bite.domain.models.MediaItem

class MediaAdapter(private val items: List<MediaItem>) :
    RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val videoView: VideoView = itemView.findViewById(R.id.videoView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val item = items[position]

        when (item.type) {
            MediaType.IMAGE -> {
                holder.imageView.visibility = View.VISIBLE
                holder.videoView.visibility = View.GONE
                Glide.with(holder.itemView.context)
                    .load(item.url)
                    .into(holder.imageView)
            }
            MediaType.VIDEO -> {
                holder.videoView.visibility = View.VISIBLE
                holder.imageView.visibility = View.GONE
                holder.videoView.setVideoPath(item.url)
                holder.videoView.setOnPreparedListener { mp ->
                    mp.isLooping = true
                    holder.videoView.start()
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
