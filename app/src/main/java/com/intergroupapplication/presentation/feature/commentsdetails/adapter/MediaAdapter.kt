package com.intergroupapplication.presentation.feature.commentsdetails.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.intergroupapplication.R
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import timber.log.Timber

class MediaAdapter(
        private val imageLoadingDelegate: ImageLoadingDelegate
): RecyclerView.Adapter<MediaAdapter.BaseHolder>() {


    val list = mutableListOf<String>()
    companion object {

        private const val PHOTO_HOLDER_KEY = 0
        private const val IMAGE_HOLDER_KEY = 1
        private const val AUDIO_HOLDER_KEY = 2
        private const val VIDEO_HOLDER_KEY = 3
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder {
        //TODO поменять вьюхи на нужные
        val view: View
        return when (viewType) {
            PHOTO_HOLDER_KEY -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.layout_attach_image, parent,false)
                PhotoHolder(view)
            }
            IMAGE_HOLDER_KEY -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.layout_attach_image, parent,false)
                ImageHolder(view)
            }
            AUDIO_HOLDER_KEY -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.layout_attach_image, parent,false)
                AudioHolder(view)
            }
            VIDEO_HOLDER_KEY -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.layout_attach_image, parent,false)
                VideoHolder(view)
            }
            else -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.layout_attach_image, parent,false)
                BaseHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> PHOTO_HOLDER_KEY
            else -> IMAGE_HOLDER_KEY
        }
    }

    open class BaseHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        open fun bind(url: String) {

        }
    }

    inner class PhotoHolder(private val view: View) : BaseHolder(view) {
        override fun bind(url: String) {
            super.bind(url)
        }
    }

    inner class ImageHolder(private val view: View) : BaseHolder(view) {
        override fun bind(url: String) {
            val simpleDraweeView = view.findViewById<SimpleDraweeView>(R.id.imagePreview)
            imageLoadingDelegate.loadImageFromFile(url,simpleDraweeView)
        }
    }

    inner class AudioHolder(private val view: View) : BaseHolder(view) {
        override fun bind(url: String) {
            super.bind(url)
        }
    }

    inner class VideoHolder(private val view: View) : BaseHolder(view) {
        override fun bind(url: String) {
            super.bind(url)
        }
    }
}