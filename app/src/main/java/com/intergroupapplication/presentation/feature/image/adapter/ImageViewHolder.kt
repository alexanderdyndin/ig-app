package com.intergroupapplication.presentation.feature.image.adapter

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.facebook.drawee.view.SimpleDraweeView
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.customview.SimplePhotoView

class ImageViewHolder(view: View): RecyclerView.ViewHolder(view) {

    val image = itemView.findViewById<SimpleDraweeView>(R.id.image)

    fun bind(file: FileEntity) {
//        Glide.with(itemView.context)
//                .load(file.file)
//                .thumbnail(0.7f)
//                .skipMemoryCache(true)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .placeholder(R.drawable.variant_10)
//                .into(image)
        image.setImageURI(file.file)
    }
}