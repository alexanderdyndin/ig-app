package com.intergroupapplication.presentation.feature.image.adapter

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.intergroupapplication.R
import com.intergroupapplication.databinding.ItemImageBinding
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.customview.zoomable.DoubleTapGestureListener
import com.intergroupapplication.presentation.customview.zoomable.ZoomableDraweeView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.inflate


class ImageAdapter(private val items: List<FileEntity>,
                   val imageLoadingDelegate: ImageLoadingDelegate): RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    companion object {
        var imageClickListener: (() -> Unit)? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(parent.inflate(R.layout.item_image))
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.count()

    override fun onViewRecycled(holder: ImageViewHolder) {
        holder.image.controller = null
        super.onViewRecycled(holder)
    }

    inner class ImageViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val viewBinding by viewBinding(ItemImageBinding::bind)

        val image = viewBinding.image

        fun bind(file: FileEntity) {
//            val controller = Fresco.newDraweeControllerBuilder()
//                .setUri(Uri.parse(file.file))
//                .setAutoPlayAnimations(true)
//                .build()
//            image.controller = controller
            imageLoadingDelegate.loadImageFromUrl(file.file, image)
            image.setOnClickListener {
                imageClickListener?.invoke()
            }
            image.setAllowTouchInterceptionWhileZoomed(false)
            image.setIsLongpressEnabled(false)
            image.setTapListener(DoubleTapGestureListener(image))
        }
    }

}