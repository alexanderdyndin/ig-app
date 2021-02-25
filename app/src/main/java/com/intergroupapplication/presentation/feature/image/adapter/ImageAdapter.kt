package com.intergroupapplication.presentation.feature.image.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.inflate

class ImageAdapter(private val items: List<FileEntity>): RecyclerView.Adapter<ImageViewHolder>() {

    companion object {
        var imageClickListener: () -> Unit = {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(parent.inflate(R.layout.item_image))
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.count()


}