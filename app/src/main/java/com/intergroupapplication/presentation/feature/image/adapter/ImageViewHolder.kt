package com.intergroupapplication.presentation.feature.image.adapter

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.FileEntity
import kotlinx.android.synthetic.main.item_image.view.*

class ImageViewHolder(view: View): RecyclerView.ViewHolder(view) {

    val image = itemView.findViewById<ImageView>(R.id.image)

    fun bind(file: FileEntity) {
        Glide.with(itemView.context).load(file.file).into(image);
    }
}