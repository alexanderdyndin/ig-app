package com.intergroupapplication.presentation.feature.image.adapter

import android.net.Uri
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.customview.zoomable.DoubleTapGestureListener
import com.intergroupapplication.presentation.customview.zoomable.ZoomableDraweeView

class ImageViewHolder(view: View): RecyclerView.ViewHolder(view) {

    val image = itemView.findViewById<ZoomableDraweeView>(R.id.image)

    fun bind(file: FileEntity) {
        val controller = Fresco.newDraweeControllerBuilder()
                .setUri(Uri.parse(file.file))
                .setAutoPlayAnimations(true)
                .build()
        image.controller = controller
        image.setOnClickListener {
            ImageAdapter.imageClickListener.invoke()
        }
        image.setAllowTouchInterceptionWhileZoomed(false)
        image.setIsLongpressEnabled(false)
        image.setTapListener(DoubleTapGestureListener(image))
    }
}