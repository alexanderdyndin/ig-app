package com.intergroupapplication.presentation.feature.bottomsheet.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.intergroupapplication.R
import com.intergroupapplication.data.model.AudioInAddFileModel
import com.intergroupapplication.data.model.GalleryModel
import com.intergroupapplication.data.model.VideoModel
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.activated
import io.reactivex.Observable

class GalleryAdapter(private val imageLoadingDelegate: ImageLoadingDelegate):RecyclerView.Adapter<BaseHolder<GalleryModel>>(){

    val photos = mutableListOf(GalleryModel("photos",false))
    val choosePhotos = mutableListOf<String>()

    companion object {
        private const val PHOTO_HOLDER_KEY = 0
        private const val IMAGE_HOLDER_KEY = 1
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<GalleryModel> {
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

    override fun onBindViewHolder(holder: BaseHolder<GalleryModel>, position: Int) {
        holder.onBind(photos[position])
    }

    override fun getItemCount() = photos.size

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> PHOTO_HOLDER_KEY
            else -> IMAGE_HOLDER_KEY
        }
    }

    inner class PhotoHolder(private val view: View) : BaseHolder<GalleryModel>(view){
        override fun onBind(data: GalleryModel) {

        }

    }

    fun getChoosePhotosFromObservable() = Observable.fromIterable(choosePhotos)

    inner class ImageHolder(private val view: View) : BaseHolder<GalleryModel>(view) {
        override fun onBind(data: GalleryModel) {
            with(view){
                val simpleDraweeView = findViewById<SimpleDraweeView>(R.id.imagePreview)
                imageLoadingDelegate.loadImageFromFile(data.url,simpleDraweeView)
                val addImageFiles = findViewById<Button>(R.id.addImageFiles)
                addImageFiles.apply {
                    activated(data.isChoose)
                    setOnClickListener {
                        data.run {
                            if(!isChoose && choosePhotos.size<10){
                                isChoose = true
                                choosePhotos.add(url)
                            } else {
                                isChoose = false
                                choosePhotos.remove(url)
                            }
                            activated(isChoose)
                        }
                    }
                }
            }
        }

    }
}
class AudioAdapter:RecyclerView.Adapter<BaseHolder<AudioInAddFileModel>>(){
    val audios = mutableListOf<AudioInAddFileModel>()
    val chooseAudios = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<AudioInAddFileModel> {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_audio_for_add_files_bottom_sheet, parent,false)
        return AudioHolder(view)
    }

    override fun onBindViewHolder(holder: BaseHolder<AudioInAddFileModel>, position: Int) {
       holder.onBind(audios[position])
    }

    override fun getItemCount() = audios.size

    fun getChooseAudiosFromObservable() = Observable.fromIterable(chooseAudios)

    inner class AudioHolder(private val view: View) : BaseHolder<AudioInAddFileModel>(view){
        override fun onBind(data: AudioInAddFileModel) {
            with(view){
                val trackName = findViewById<TextView>(R.id.trackName)
                val audioDuration = findViewById<TextView>(R.id.audioDuration)
                val addAudioButton = findViewById<Button>(R.id.addAudioButton)
                trackName.text = data.name
                audioDuration.text = data.duration
                addAudioButton.apply {
                    activated(data.isChoose)
                    setOnClickListener {
                        data.run {
                            if(!isChoose && chooseAudios.size<10){
                                isChoose = true
                               chooseAudios.add(url)
                            } else {
                                isChoose = false
                                chooseAudios.remove(url)
                            }
                            activated(isChoose)
                        }
                    }
                }
            }

        }

    }
}

class VideoAdapter(private val imageLoadingDelegate: ImageLoadingDelegate):RecyclerView.Adapter<BaseHolder<VideoModel>>(){
    val videos = mutableListOf<VideoModel>()
    val chooseVideos = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<VideoModel> {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video_for_add_files_bottom_sheet, parent,false)
        return VideoHolder(view)
    }

    override fun onBindViewHolder(holder: BaseHolder<VideoModel>, position: Int) {
        holder.onBind(videos[position])
    }

    override fun getItemCount() = videos.size

    fun getChooseVideosFromObservable() = Observable.fromIterable(chooseVideos)

    inner class VideoHolder(private val view: View) : BaseHolder<VideoModel>(view) {
        override fun onBind(data: VideoModel) {
            with(view){
                val simpleDraweeView = findViewById<SimpleDraweeView>(R.id.imagePreview)
                imageLoadingDelegate.loadImageFromFile(data.url,simpleDraweeView)
                val textView = findViewById<TextView>(R.id.timeVideo)
                textView.text = data.duration
                val addVideoFiles = findViewById<Button>(R.id.addVideoFiles)
                addVideoFiles.apply {
                    activated(data.isChoose)
                    setOnClickListener {
                        data.run {
                            if(!isChoose && chooseVideos.size<10){
                                isChoose = true
                                chooseVideos.add(url)
                            } else {
                                isChoose = false
                                chooseVideos.remove(url)
                            }
                            activated(isChoose)
                        }
                    }
                }
            }
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
