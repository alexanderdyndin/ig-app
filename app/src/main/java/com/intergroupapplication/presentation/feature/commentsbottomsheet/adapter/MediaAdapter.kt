package com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.intergroupapplication.R
import com.intergroupapplication.data.model.AudioInAddFileModel
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.data.model.GalleryModel
import com.intergroupapplication.data.model.VideoModel
import com.intergroupapplication.databinding.ItemColorBinding
import com.intergroupapplication.databinding.ItemImageForAddFilesBottomSheetBinding
import com.intergroupapplication.databinding.ItemPhotoForAddFilesBottomSheetBinding
import com.intergroupapplication.databinding.ItemVideoForAddFilesBottomSheetBinding
import com.intergroupapplication.domain.entity.MediaType
import com.intergroupapplication.presentation.base.BaseHolder
import com.intergroupapplication.presentation.delegate.DialogDelegate
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.mediaPlayer.AudioForAddFilesBottomSheetPlayerView
import java.io.ByteArrayOutputStream
import java.io.File

sealed class MediaAdapter<T> : RecyclerView.Adapter<BaseHolder<T>>() {
    interface MediaCallback {
        fun changeCountChooseImage()
        fun changeCountChooseVideo()
        fun changeCountChooseAudio()
        fun attachPhoto()
        fun changeTextColor(color: Int)
    }

    abstract fun getChooseMedias(): Set<ChooseMedia>

    class GalleryAdapter(
        private val imageLoadingDelegate: ImageLoadingDelegate,
        private val mediaCallback: MediaCallback,
        private val dialogDelegate: DialogDelegate,
    ) : MediaAdapter<GalleryModel>() {

        val photos = mutableListOf(GalleryModel("photos", 0, false))
        val choosePhoto = mutableSetOf<ChooseMedia>()

        companion object {
            private const val PHOTO_HOLDER_KEY = 0
            private const val IMAGE_HOLDER_KEY = 1
        }


        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): BaseHolder<GalleryModel> {
            val view: View
            return when (viewType) {
                IMAGE_HOLDER_KEY -> {
                    view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_image_for_add_files_bottom_sheet, parent, false)
                    ImageHolder(view)
                }
                else -> {
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

        override fun getChooseMedias() = choosePhoto

        override fun onViewRecycled(holder: BaseHolder<GalleryModel>) {
            if (holder is ImageHolder) {
                holder.binding.imagePreview.controller = null
            }
            super.onViewRecycled(holder)
        }

        inner class PhotoHolder(view: View) : BaseHolder<GalleryModel>(view) {
            private val binding by viewBinding(ItemPhotoForAddFilesBottomSheetBinding::bind)
            override fun onBind(data: GalleryModel) {
                with(binding) {
                    makePhoto.setOnClickListener {
                        if (choosePhoto.size < 10) {
                            mediaCallback.attachPhoto()
                        } else {
                            Toast.makeText(
                                binding.root.context,
                                "Не больше 10 вложений", Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }

        }

        inner class ImageHolder(view: View) : BaseHolder<GalleryModel>(view) {
            val binding by viewBinding(ItemImageForAddFilesBottomSheetBinding::bind)
            override fun onBind(data: GalleryModel) {
                with(binding) {
                    imageLoadingDelegate.loadCompressedImageFromFile(data.url, imagePreview)
                    addImageFiles.apply {
                        activated(data.isChoose)
                        setOnClickListener {
                            data.run {
                                if (!isChoose && choosePhoto.size < 10 &&
                                    !choosePhoto.contains(
                                        ChooseMedia(
                                            url,
                                            name = url.substringAfterLast("/"),
                                            type = MediaType.IMAGE
                                        )
                                    )
                                ) {
                                    isChoose = true
                                    choosePhoto.addChooseMedia(
                                        ChooseMedia(
                                            url,
                                            name = url.substringAfterLast("/"),
                                            type = MediaType.IMAGE
                                        )
                                    )
                                } else if (isChoose) {
                                    isChoose = false
                                    choosePhoto.removeChooseMedia(url)
                                }
                                mediaCallback.changeCountChooseImage()
                                activated(isChoose)
                            }
                        }
                    }
                    imagePreview.setOnClickListener {
                        dialogDelegate.showPreviewDialog(true, data.url, data.isChoose)
                    }
                }
            }

        }

    }

    class AudioAdapter(private val mediaCallback: MediaCallback) :
        MediaAdapter<AudioInAddFileModel>() {
        val audios = mutableListOf<AudioInAddFileModel>()
        val chooseAudio = mutableSetOf<ChooseMedia>()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioHolder {
            return AudioHolder(AudioForAddFilesBottomSheetPlayerView(parent.context))
        }

        override fun onBindViewHolder(holder: BaseHolder<AudioInAddFileModel>, position: Int) {
            holder.onBind(audios[position])
        }

        override fun getItemCount() = audios.size

        override fun onViewRecycled(holder: BaseHolder<AudioInAddFileModel>) {
            if (holder is AudioHolder) {
                holder.view.exoPlayer.player?.pause()
                holder.view.exoPlayer.player = null
            }
            super.onViewRecycled(holder)
        }

        override fun getChooseMedias() = chooseAudio

        inner class AudioHolder(val view: AudioForAddFilesBottomSheetPlayerView) :
            BaseHolder<AudioInAddFileModel>(view) {
            override fun onBind(data: AudioInAddFileModel) {
                with(view) {
                    setupDownloadAudioPlayerView(data)
                    addAudioButton.apply {
                        activated(data.isChoose)
                        setOnClickListener {
                            data.run {
                                if (!isChoose && chooseAudio.size < 10 &&
                                    !chooseAudio.containsMedia(url)
                                ) {
                                    isChoose = true
                                    chooseAudio.addChooseMedia(
                                        ChooseMedia(
                                            url, name = data.name,
                                            author = data.author,
                                            duration = data.duration,
                                            type = MediaType.AUDIO
                                        )
                                    )
                                } else if (isChoose) {
                                    isChoose = false
                                    chooseAudio.removeChooseMedia(url)
                                }
                                mediaCallback.changeCountChooseAudio()
                                activated(isChoose)
                            }
                        }
                    }
                }

            }

        }
    }

    class VideoAdapter(
        private val imageLoadingDelegate: ImageLoadingDelegate,
        private val mediaCallback: MediaCallback, private val dialogDelegate: DialogDelegate,
    ) : MediaAdapter<VideoModel>() {
        val videos = mutableListOf<VideoModel>()
        val chooseVideo = mutableSetOf<ChooseMedia>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<VideoModel> {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_video_for_add_files_bottom_sheet, parent, false)
            return VideoHolder(view)
        }

        override fun onBindViewHolder(holder: BaseHolder<VideoModel>, position: Int) {
            holder.onBind(videos[position])
        }

        override fun getItemCount() = videos.size

        override fun getChooseMedias() = chooseVideo

        inner class VideoHolder(view: View) : BaseHolder<VideoModel>(view) {
            private val binding by viewBinding(ItemVideoForAddFilesBottomSheetBinding::bind)
            override fun onBind(data: VideoModel) {
                with(binding) {
                    imageLoadingDelegate.loadImageFromFile(data.url, imagePreview)
                    timeVideo.text = data.duration
                    addVideoFiles.apply {
                        activated(data.isChoose)
                        setOnClickListener {
                            data.run {
                                if (!isChoose && chooseVideo.size < 10 && !chooseVideo.containsMedia(
                                        url
                                    )
                                ) {
                                    isChoose = true
                                    chooseVideo.addChooseMedia(
                                        ChooseMedia(
                                            url,
                                            createFile(imagePreview.drawable),
                                            name = url.substringAfterLast("/"),
                                            duration = data.duration,
                                            type = MediaType.VIDEO
                                        )
                                    )
                                } else if (isChoose) {
                                    isChoose = false
                                    chooseVideo.removeChooseMedia(url)
                                }
                                mediaCallback.changeCountChooseVideo()
                                activated(isChoose)
                            }
                        }
                    }
                    imagePreview.setOnClickListener {
                        dialogDelegate.showPreviewDialog(
                            false, data.url, data.isChoose,
                            createFile(imagePreview.drawable)
                        )
                    }
                }
            }

            private fun createFile(drawable: Drawable): String {
                val cacheFile =
                    if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
                        || !Environment.isExternalStorageRemovable()
                    ) {
                        binding.root.context.externalCacheDir
                    } else {
                        binding.root.context.cacheDir
                    }
                val f = File(cacheFile, "previewImage")
                f.createNewFile()
                val bitmap = drawable.toBitmap(300, 300)
                val bos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
                val bitmapData = bos.toByteArray()
                f.writeBytes(bitmapData)
                return f.absolutePath
            }

        }

    }

    class PlaylistAdapter(imageLoadingDelegate: ImageLoadingDelegate) :
        MediaAdapter<String>() {
        val playlists = mutableListOf<String>()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<String> {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_attach_image, parent, false)
            return PlaylistHolder(view)
        }

        override fun onBindViewHolder(holder: BaseHolder<String>, position: Int) {
            holder.onBind(playlists[position])
        }

        override fun getItemCount() = playlists.size

        inner class PlaylistHolder(view: View) : BaseHolder<String>(view) {
            override fun onBind(data: String) {

            }

        }

        override fun getChooseMedias() = emptySet<ChooseMedia>()
    }

    class ColorAdapter(private val mediaCallback: MediaCallback) : MediaAdapter<Int>() {
        val colors = mutableListOf<Int>()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder<Int> {
            return ColorHolder(parent.inflate(R.layout.item_color))
        }

        override fun onBindViewHolder(holder: BaseHolder<Int>, position: Int) {
            holder.onBind(colors[position])
        }

        override fun getItemCount() = colors.size

        inner class ColorHolder(view: View) : BaseHolder<Int>(view) {

            private val colorBinding by viewBinding(ItemColorBinding::bind)

            override fun onBind(data: Int) {
                colorBinding.colorButton.run {
                    setBackgroundColor(data)
                    setOnClickListener {
                        mediaCallback.changeTextColor(data)
                    }
                }
            }
        }

        override fun getChooseMedias(): Set<ChooseMedia> = emptySet()

    }
}
