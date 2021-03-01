package com.intergroupapplication.presentation.feature.image.adapter

import android.net.Uri
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.customview.SimplePhotoView

class ImageViewHolder(view: View): RecyclerView.ViewHolder(view) {

    val image = itemView.findViewById<SimplePhotoView>(R.id.image)
    val imageLayout = itemView.findViewById<FrameLayout>(R.id.imageLayout)

    fun bind(file: FileEntity) {
        if (file.file.contains(".gif")) {
            val controller = Fresco.newDraweeControllerBuilder()
                    .setUri(Uri.parse(file.file))
                    .setAutoPlayAnimations(true)
                    .build()
            image.controller = controller
        } else
            image.setImageURI(file.file)
        imageLayout.setOnClickListener {
            ImageAdapter.imageClickListener.invoke()
        }
    }
}