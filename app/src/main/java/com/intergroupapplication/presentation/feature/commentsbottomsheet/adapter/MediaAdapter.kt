package com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.intergroupapplication.R
import com.intergroupapplication.data.model.AudioInAddFileModel
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.data.model.GalleryModel
import com.intergroupapplication.data.model.VideoModel
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.activated
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.*
import java.util.concurrent.atomic.AtomicBoolean


//два сомнительных по архитектуре момента, но лучше пока не придумал
val chooseMedias = mutableSetOf<ChooseMedia>()
fun MutableSet<ChooseMedia>.addChooseMedia(chooseMedia: ChooseMedia){
    this.forEach {
        if (it.url == chooseMedia.url){
            return
        }
    }
    this.add(chooseMedia)
}
fun MutableSet<ChooseMedia>.removeChooseMedia(url:String){
    this.forEach {
        if (it.url == url){
            this.remove(it)
            return
        }
    }
}

fun MutableSet<ChooseMedia>.containsMedia(url:String):Boolean{
    this.forEach {
        if (it.url == url){
            return true
        }
    }
    return false
}
interface MediaCallback{
    fun changeCountChooseImage()
    fun changeCountChooseVideo()
    fun changeCountChooseAudio()
    fun attachPhoto()
}

class GalleryAdapter(private val imageLoadingDelegate: ImageLoadingDelegate,
                     private val mediaCallback: MediaCallback,
                     private val dialogDelegate: DialogDelegate,
                     private val manager: FragmentManager
):RecyclerView.Adapter<BaseHolder<GalleryModel>>(){

    val photos = mutableListOf(GalleryModel("photos", 0, false))

    companion object {
        private const val PHOTO_HOLDER_KEY = 0
        private const val IMAGE_HOLDER_KEY = 1
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<GalleryModel> {
        val view: View
        return when (viewType) {
            IMAGE_HOLDER_KEY -> {
                view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_image_for_add_files_bottom_sheet, parent, false)
                ImageHolder(view)
            }
            else->{
                view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_photo_for_add_files_bottom_sheet, parent, false)
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

    override fun onViewRecycled(holder: BaseHolder<GalleryModel>) {
        if (holder is ImageHolder){
            holder.simpleDraweeView.controller = null
        }
        super.onViewRecycled(holder)
    }

    inner class PhotoHolder(private val view: View) : BaseHolder<GalleryModel>(view){
        override fun onBind(data: GalleryModel) {
            with(view){
                val makePhoto = findViewById<ImageView>(R.id.make_photo)
                makePhoto.setOnClickListener {
                    if (chooseMedias.size<10) {
                        mediaCallback.attachPhoto()
                    }else{
                        Toast.makeText(context, "Не больше 10 вложений", Toast.LENGTH_SHORT)
                                .show()
                    }
                }
            }
        }

    }

    fun getChoosePhotosFromObservable():Observable<ChooseMedia> = Observable.fromIterable(chooseMedias)

    inner class ImageHolder(private val view: View) : BaseHolder<GalleryModel>(view) {
        val simpleDraweeView = view.findViewById<SimpleDraweeView>(R.id.imagePreview)
        override fun onBind(data: GalleryModel) {
            with(view){
                imageLoadingDelegate.loadCompressedImageFromFile(data.url, simpleDraweeView)
                val addImageFiles = findViewById<Button>(R.id.addImageFiles)
                addImageFiles.apply {
                    activated(data.isChoose)
                    setOnClickListener {
                        data.run {
                            if(!isChoose && chooseMedias.size<10 && !chooseMedias.containsMedia(url)){
                                isChoose = true
                                chooseMedias.addChooseMedia(ChooseMedia(url))
                            } else if (isChoose) {
                                isChoose = false
                                chooseMedias.removeChooseMedia(url)
                            }
                            mediaCallback.changeCountChooseImage()
                            activated(isChoose)
                        }
                    }
                }
                simpleDraweeView.setOnClickListener {
                    dialogDelegate.showPreviewDialog(true, data.url, data.isChoose, manager)
                }
            }
        }

    }
}
class AudioAdapter(private val mediaCallback: MediaCallback)
    :RecyclerView.Adapter<BaseHolder<AudioInAddFileModel>>(){
    val audios = mutableListOf<AudioInAddFileModel>()
    private var isPlaying = AtomicBoolean(false)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<AudioInAddFileModel> {
        val view = LayoutInflater.from(parent.context).
        inflate(R.layout.item_audio_for_add_files_bottom_sheet, parent, false)
        return AudioHolder(view)
    }

    override fun onBindViewHolder(holder: BaseHolder<AudioInAddFileModel>, position: Int) {
       holder.onBind(audios[position])
    }

    override fun getItemCount() = audios.size

    fun getChooseAudiosFromObservable() = Observable.fromIterable(chooseMedias)

    inner class AudioHolder(private val view: View) : BaseHolder<AudioInAddFileModel>(view){
        override fun onBind(data: AudioInAddFileModel) {
            with(view){
                val trackName = findViewById<TextView>(R.id.trackName)
                val audioDuration = findViewById<TextView>(R.id.audioDuration)
                val addAudioButton = findViewById<Button>(R.id.addAudioButton)
                val audioPlay = findViewById<ImageButton>(R.id.audioPlay)
                trackName.text = data.name
                audioDuration.text = data.duration
                addAudioButton.apply {
                    activated(data.isChoose)
                    setOnClickListener {
                        data.run {
                            if(!isChoose && chooseMedias.size<10 && !chooseMedias.containsMedia(url)){
                                isChoose = true
                                chooseMedias.addChooseMedia(ChooseMedia(url,name = data.name,
                                        authorMusic = data.author,
                                        duration = data.duration))
                            } else if (isChoose) {
                                isChoose = false
                                chooseMedias.removeChooseMedia(url)
                            }
                            mediaCallback.changeCountChooseAudio()
                            activated(isChoose)
                        }
                    }
                }
                audioPlay.setOnClickListener {
                    if(!isPlaying.get()) {
                        isPlaying.set(true)
                        val mediaPlayer = MediaPlayer.create(context, Uri.parse(data.url))
                        Single.just("1").subscribeOn(Schedulers.io()).subscribe { it ->
                            mediaPlayer.start()
                            SystemClock.sleep(4000)
                            mediaPlayer.stop()
                            isPlaying.set(false)
                        }
                    }
                }
            }

        }

    }
}

class VideoAdapter(private val imageLoadingDelegate: ImageLoadingDelegate,
                   private val mediaCallback: MediaCallback, private val dialogDelegate: DialogDelegate,
                   private val manager: FragmentManager)
    :RecyclerView.Adapter<BaseHolder<VideoModel>>(){
    val videos = mutableListOf<VideoModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<VideoModel> {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video_for_add_files_bottom_sheet, parent, false)
        return VideoHolder(view)
    }

    override fun onBindViewHolder(holder: BaseHolder<VideoModel>, position: Int) {
        holder.onBind(videos[position])
    }

    override fun getItemCount() = videos.size

    fun getChooseVideosFromObservable() = Observable.fromIterable(chooseMedias)

    inner class VideoHolder(private val view: View) : BaseHolder<VideoModel>(view) {
        override fun onBind(data: VideoModel) {
            with(view){
                val simpleDraweeView = findViewById<SimpleDraweeView>(R.id.imagePreview)
                imageLoadingDelegate.loadImageFromFile(data.url, simpleDraweeView)
                val textView = findViewById<TextView>(R.id.timeVideo)
                textView.text = data.duration
                val addVideoFiles = findViewById<Button>(R.id.addVideoFiles)
                addVideoFiles.apply {
                    activated(data.isChoose)
                    setOnClickListener {
                        data.run {
                            if(!isChoose && chooseMedias.size<10 && !chooseMedias.containsMedia(url)){
                                isChoose = true
                                chooseMedias.addChooseMedia(ChooseMedia(url,createFile(simpleDraweeView.drawable),
                                duration = data.duration))
                            } else if (isChoose){
                                isChoose = false
                                chooseMedias.removeChooseMedia(url)
                            }
                            mediaCallback.changeCountChooseVideo()
                            activated(isChoose)
                        }
                    }
                }
                simpleDraweeView.setOnClickListener {
                    dialogDelegate.showPreviewDialog(false, data.url,data.isChoose,manager)
                }
            }
        }

        private fun createFile(drawable:Drawable):String{
            val cacheFile =
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
                    || !Environment.isExternalStorageRemovable()) {
                view.context.externalCacheDir
            } else {
                view.context.cacheDir
            }
            val f = File(cacheFile, "previewImage")
            f.createNewFile()
            val bitmap = drawable.toBitmap(300,300)
            val bos = ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100 , bos);
            val bitmapData = bos.toByteArray();
            f.writeBytes(bitmapData)
            return f.absolutePath
        }

    }
}

class PlaylistAdapter(private val imageLoadingDelegate: ImageLoadingDelegate):RecyclerView.Adapter<BaseHolder<String>>(){
    val playlists = mutableListOf<String>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<String> {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.layout_attach_image, parent, false)
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
    abstract fun onBind(data: T)
}
