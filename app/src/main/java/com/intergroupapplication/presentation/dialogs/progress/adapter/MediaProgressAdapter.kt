package com.intergroupapplication.presentation.dialogs.progress.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ProgressMediaModel
import com.intergroupapplication.databinding.LayoutMediaProgressHolderBinding
import com.intergroupapplication.domain.entity.LoadMediaType
import com.intergroupapplication.presentation.base.BaseHolder
import com.intergroupapplication.presentation.exstension.hide
import com.intergroupapplication.presentation.exstension.inflate
import com.intergroupapplication.presentation.exstension.show

class MediaProgressAdapter:RecyclerView.Adapter<MediaProgressAdapter.MediaProgressHolder>() {

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
            binding.nameMedia.text = data.url
            when(data.type){
                LoadMediaType.START -> startedState(data.url)
                LoadMediaType.PROGRESS ->  progressState(data.type.progress)
                LoadMediaType.ERROR -> errorState()
                LoadMediaType.UPLOAD -> uploadState()
            }
        }

        private fun startedState(url: String){
            with(binding){
                imageUploadingProgressBar.show()
                stopUploading.show()
                refreshContainer.hide()
            }
            prepareListeners(url)
        }

        private fun progressState(progress:Float){
            binding.imageUploadingProgressBar.progress = progress
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

        private fun prepareListeners(url:String) {
            with(binding){
                refreshContainer.setOnClickListener {
                    imageUploadingProgressBar.progress = 0f
                    //presenter.retryLoading(chooseMedia)
                    startedState(url)
                }
                stopUploading.setOnClickListener {
                    //presenter.cancelUploading(chooseMedia.url)
                }
                detachMedia.setOnClickListener {
                    //presenter.removeContent(chooseMedia.url)
                }
            }
        }
    }

}