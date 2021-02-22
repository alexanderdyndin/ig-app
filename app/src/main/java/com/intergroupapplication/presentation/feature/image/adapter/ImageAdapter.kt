package com.intergroupapplication.presentation.feature.image.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.inflate

class ImageAdapter(private val items: List<FileEntity>): RecyclerView.Adapter<ImageViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(parent.inflate(R.layout.item_image))
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.count()

    override fun onViewRecycled(holder: ImageViewHolder) {
        super.onViewRecycled(holder)
        Glide.get(holder.itemView.context).clearMemory()
    }

    override fun onViewDetachedFromWindow(holder: ImageViewHolder) {
        super.onViewDetachedFromWindow(holder)
        Glide.get(holder.itemView.context).clearMemory()
    }

}