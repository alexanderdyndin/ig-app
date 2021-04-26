package com.intergroupapplication.presentation.feature.bottomsheet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.intergroupapplication.R
import com.intergroupapplication.data.model.VideoModel
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
class GalleryAdapter(private val imageLoadingDelegate: ImageLoadingDelegate):RecyclerView.Adapter<BaseHolder<String>>(){

    val photos = mutableListOf("photos")

    companion object {
        private const val PHOTO_HOLDER_KEY = 0
        private const val IMAGE_HOLDER_KEY = 1
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<String> {
        val view: View
        return when (viewType) {
            IMAGE_HOLDER_KEY -> {
                view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_for_add_files_bottom_sheet, parent, false)
                ImageHolder(view)
            }
            else->{
                view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo_for_add_files_bottom_sheet, parent, false)
                PhotoHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseHolder<String>, position: Int) {
        holder.onBind(photos[position])
    }

    override fun getItemCount() = photos.size

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> PHOTO_HOLDER_KEY
            else -> IMAGE_HOLDER_KEY
        }
    }

    inner class PhotoHolder(private val view: View) : BaseHolder<String>(view){
        override fun onBind(data: String) {

        }

    }

    inner class ImageHolder(private val view: View) : BaseHolder<String>(view) {
        override fun onBind(data: String) {
            val simpleDraweeView = view.findViewById<SimpleDraweeView>(R.id.imagePreview)
            imageLoadingDelegate.loadImageFromFile(data,simpleDraweeView)
        }


    }
}
class AudioAdapter(private val imageLoadingDelegate: ImageLoadingDelegate):RecyclerView.Adapter<BaseHolder<String>>(){
    val audios = mutableListOf<String>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<String> {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_attach_image, parent,false)
        return AudioHolder(view)
    }

    override fun onBindViewHolder(holder: BaseHolder<String>, position: Int) {
       holder.onBind(audios[position])
    }

    override fun getItemCount() = audios.size

    inner class AudioHolder(private val view: View) : BaseHolder<String>(view){
        override fun onBind(data: String) {

        }

    }
}

class VideoAdapter(private val imageLoadingDelegate: ImageLoadingDelegate):RecyclerView.Adapter<BaseHolder<VideoModel>>(){
    val videos = mutableListOf<VideoModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<VideoModel> {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video_for_add_files_bottom_sheet, parent,false)
        return VideoHolder(view)
    }

    override fun onBindViewHolder(holder: BaseHolder<VideoModel>, position: Int) {
        holder.onBind(videos[position])
    }

    override fun getItemCount() = videos.size

    inner class VideoHolder(private val view: View) : BaseHolder<VideoModel>(view) {
        override fun onBind(data: VideoModel) {
            val simpleDraweeView = view.findViewById<SimpleDraweeView>(R.id.imagePreview)
            imageLoadingDelegate.loadImageFromFile(data.url,simpleDraweeView)
            val textView = view.findViewById<TextView>(R.id.timeVideo)
            textView.text = data.duration
        }

    }
}

class PlaylistAdapter(private val imageLoadingDelegate: ImageLoadingDelegate):RecyclerView.Adapter<BaseHolder<String>>(){
    val playlists = mutableListOf<String>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<String> {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_attach_image, parent,false)
        return PlaylistHolder(view)
    }

    override fun onBindViewHolder(holder: BaseHolder<String>, position: Int) {
        holder.onBind(playlists[position])
    }

    override fun getItemCount() = playlists.size

    inner class PlaylistHolder(private val view: View) : BaseHolder<String>(view){
        override fun onBind(data: String) {

        }

    }
}

abstract class BaseHolder<T>(view: View):RecyclerView.ViewHolder(view){
    abstract fun onBind(data:T)
}
