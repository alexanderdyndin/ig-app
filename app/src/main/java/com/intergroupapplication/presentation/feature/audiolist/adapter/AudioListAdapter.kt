package com.intergroupapplication.presentation.feature.audiolist.adapter

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.intergroupapplication.R
import com.intergroupapplication.databinding.ItemAudioBinding
import com.intergroupapplication.domain.entity.AudioEntity
import com.intergroupapplication.presentation.customview.AudioGalleryView
import com.intergroupapplication.presentation.exstension.inflate
import timber.log.Timber

class AudioListAdapter: PagingDataAdapter<AudioEntity, AudioListAdapter.AudioViewHolder>(diffUtil) {

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<AudioEntity>() {
            override fun areItemsTheSame(oldItem: AudioEntity, newItem: AudioEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: AudioEntity, newItem: AudioEntity): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        return  AudioViewHolder(parent.inflate(R.layout.item_audio))
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        getItem(position)?.let {
            holder.bind(it, position)
        }
    }

    inner class AudioViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private val viewBinding by viewBinding(ItemAudioBinding::bind)

        val audio: AudioGalleryView = viewBinding.audio

        fun bind(audioEntity: AudioEntity, position: Int) {
            audio.setAudios(listOf(audioEntity), position = position)
        }
    }



}