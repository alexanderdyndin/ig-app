package com.intergroupapplication.presentation.dialogs.progress.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.data.model.ProgressMediaModel
import com.intergroupapplication.databinding.LayoutMediaProgressHolderBinding
import com.intergroupapplication.domain.entity.LoadMediaType
import com.intergroupapplication.presentation.base.BaseHolder
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.inflate
import com.intergroupapplication.presentation.exstension.show
import timber.log.Timber

class MediaProgressAdapter(private val callback: ProgressCallback)
    :RecyclerView.Adapter<MediaProgressAdapter.MediaProgressHolder>() {

    val progressMedia = mutableListOf<ProgressMediaModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaProgressHolder {
        return MediaProgressHolder(parent.inflate(R.layout.layout_media_progress_holder,
            false))
    }

    override fun onBindViewHolder(holder: MediaProgressHolder, position: Int) {
        holder.onBind(progressMedia[position])
    }

    override fun getItemCount() = progressMedia.size

    inner class MediaProgressHolder(view:View): BaseHolder<ProgressMediaModel>(view){
        private val binding by viewBinding(LayoutMediaProgressHolderBinding::bind)

        override fun onBind(data: ProgressMediaModel) {
            binding.nameMedia.text = data.chooseMedia.name
            prepareListeners(data.chooseMedia)
            when(data.type){
                LoadMediaType.START -> startedState()
                LoadMediaType.PROGRESS ->  progressState(data.type.progress)
                LoadMediaType.ERROR -> errorState()
                LoadMediaType.UPLOAD -> uploadState()
            }
        }

        private fun startedState(){
            with(binding){
                imageUploadingProgressBar.show()
                stopUploading.show()
                refreshContainer.hide()
                detachMedia.hide()
            }
        }

        private fun progressState(progress:Float){
            with(binding){
                imageUploadingProgressBar.progress = progress
                detachMedia.hide()
            }
        }

        private fun errorState(){
            with(binding){
                imageUploadingProgressBar.hide()
                detachMedia.show()
                refreshContainer.show()
                stopUploading.hide()
            }
        }

        private fun uploadState() {
            with(binding){
                imageUploadingProgressBar.hide()
                detachMedia.show()
                stopUploading.hide()
            }
        }

        private fun prepareListeners(chooseMedia: ChooseMedia) {
            with(binding){
                refreshContainer.setOnClickListener {
                    imageUploadingProgressBar.progress = 0f
                    callback.retryLoading(chooseMedia)
                    startedState()
                    Timber.tag("tut_retry").d(chooseMedia.name)
                }
                stopUploading.setOnClickListener {
                    Timber.tag("tut_cancel").d(chooseMedia.name)
                    callback.cancelUploading(chooseMedia)
                }
                detachMedia.setOnClickListener {
                    callback.removeContent(chooseMedia)
                }
            }
        }
    }

    interface ProgressCallback{
        fun retryLoading(chooseMedia: ChooseMedia)
        fun cancelUploading(chooseMedia: ChooseMedia)
        fun removeContent(chooseMedia: ChooseMedia)
    }

}