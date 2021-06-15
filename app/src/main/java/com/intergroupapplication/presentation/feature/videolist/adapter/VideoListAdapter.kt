package com.intergroupapplication.presentation.feature.videolist.adapter

import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import by.kirich1409.viewbindingdelegate.viewBinding
import com.intergroupapplication.R
import com.intergroupapplication.databinding.ItemVideoBinding
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.exstension.inflate
import com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter.BaseHolder

class VideoListAdapter:PagingDataAdapter<FileEntity, VideoListAdapter.VideoHolder>(diffUtil) {

    companion object{
        private val diffUtil = object: DiffUtil.ItemCallback<FileEntity>(){
            override fun areItemsTheSame(oldItem: FileEntity, newItem: FileEntity): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: FileEntity, newItem: FileEntity): Boolean {
               return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoHolder {
        return VideoHolder(parent.inflate(R.layout.item_video))
    }

    override fun onBindViewHolder(holder: VideoHolder, position: Int) {
        getItem(position)?.let {
            holder.onBind(it)
        }
    }

    inner class VideoHolder(val view: View):BaseHolder<FileEntity>(view){

        private val videoBinding by viewBinding(ItemVideoBinding::bind)

        override fun onBind(data: FileEntity) {
            videoBinding.video.setVideos(listOf(data))
        }
    }
}