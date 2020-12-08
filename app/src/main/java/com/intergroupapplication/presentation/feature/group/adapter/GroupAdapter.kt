package com.intergroupapplication.presentation.feature.group.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.presentation.base.adapter.PagingAdapter
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.item_group_post.view.*
import kotlinx.android.synthetic.main.post_item_error.view.*
import timber.log.Timber


/**
 * Created by abakarmagomedov on 27/08/2018 at project InterGroupApplication.
 */
class GroupAdapter(diffCallback: DiffUtil.ItemCallback<GroupPostEntity>,
                   private val imageLoadingDelegate: ImageLoadingDelegate)
    : PagedListAdapter<GroupPostEntity, RecyclerView.ViewHolder>(diffCallback), PagingAdapter {

    var commentClickListener: (groupPostEntity: GroupPostEntity) -> Unit = {}
    var retryClickListener: () -> Unit = {}
    var complaintListener: (Int) -> Unit = {}
    private val loadingViewType = 123       //todo Почему это переменная экземпляра? Мб лучше вынести в статик?
    private val errorViewType = 321
    private var isLoading = false
    private var isError = false
    private var compositeDisposable = CompositeDisposable()

    private val TEST_VIDEO_URI = "http://92.53.65.93:8888/test/index.mp4?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=minio/20201208//s3/aws4_request&X-Amz-Date=20201208T190620Z&X-Amz-Expires=432000&X-Amz-SignedHeaders=host&X-Amz-Signature=2f0f8e7b65ed33bbb387dd4e3483ea644a826fd45ad85c84a48d8309728cbe07"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            loadingViewType -> LoadingViewHolder(parent.inflate(R.layout.post_item_loading))
            errorViewType -> ErrorViewHolder(parent.inflate(R.layout.post_item_error))
            else -> PostViewHolder(parent.inflate(R.layout.item_group_post))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            loadingViewType -> {
            }
            errorViewType -> (holder as ErrorViewHolder).bind()
            else -> getItem(position)?.let { (holder as PostViewHolder).bind(it) }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is PostViewHolder) {
            holder.exoPlayerView.player?.release()
            holder.exoPlayerView.player?.let {
                Toast.makeText(holder.exoPlayerView.context, "Player released", Toast.LENGTH_SHORT).show()
            }
        }

        super.onViewRecycled(holder)
    }

    override fun getItemCount() = super.getItemCount() + (if (isLoading) 1 else 0) + (if (isError) 1 else 0)

    override fun itemCount() = itemCount

    override fun getItemViewType(position: Int): Int {
        if (position == itemCount - 1) {
            if (isLoading) {
                return loadingViewType
            }
            if (isError) {
                return errorViewType
            }
        }
        return super.getItemViewType(position)
    }

    override fun addLoading() {
        if (isLoading) {
            return
        }
        isLoading = true
        notifyItemInserted(itemCount)
    }

    override fun removeLoading() {
        if (!isLoading) {
            return
        }
        isLoading = false
        notifyItemRemoved(itemCount)
    }

    override fun addError() {
        if (isError) {
            return
        }
        isError = true
        notifyItemInserted(itemCount)
    }

    override fun removeError() {
        if (!isError) {
            return
        }
        isError = false
        notifyItemRemoved(itemCount)
    }

    inner class PostViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val exoPlayerView = itemView.findViewById<StyledPlayerView>(R.id.playerView)

        fun bind(item: GroupPostEntity) {
            with(itemView) {
                compositeDisposable.add(getDateDescribeByString(item.date)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ postPrescription.text = it }, { Timber.e(it) }))
                //postPrescription.text = getDateDescribeByString(item.date)
                postCommentsCount.text = item.commentsCount
                item.postText.let { it ->
                    if (!it.isEmpty()) {
                        postText.text = item.postText
                        postText.show()
                        postText.setOnClickListener {
                            commentClickListener.invoke(item)
                        }
                    } else {
                        postText.gone()
                    }
                }
                groupName.text = item.groupInPost.name
                commentImageClickArea.setOnClickListener {
                    commentClickListener.invoke(item)
                }
                item.photo.apply {
                    ifNotNull {
                        postImage.show()
                        imageLoadingDelegate.loadImageFromUrl(it, postImage)
                    }
                    ifNull { postImage.gone() }
                }
                doOrIfNull(item.groupInPost.avatar, { imageLoadingDelegate.loadImageFromUrl(it, groupPostAvatar) },
                        { imageLoadingDelegate.loadImageFromResources(R.drawable.application_logo, groupPostAvatar) })
                settingsPost.setOnClickListener { showPopupMenu(settingsPost, Integer.parseInt(item.id)) }

                //init player
                val player = SimpleExoPlayer.Builder(exoPlayerView.context).build()
                exoPlayerView.player = player
                // Build the media item.
                val mediaItem: MediaItem = MediaItem.fromUri(TEST_VIDEO_URI)        //Todo юри видео должно быть в Entity
                // Set the media item to be played.
                player.setMediaItem(mediaItem)
                // Prepare the player.
                player.prepare()
            }
        }


        private fun showPopupMenu(view: View, id: Int) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.inflate(R.menu.settings_menu)

            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.complaint -> complaintListener.invoke(id)
                }
                return@setOnMenuItemClickListener true
            }

            popupMenu.show()
        }
    }

    override fun unregisterAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
        super.unregisterAdapterDataObserver(observer)
        compositeDisposable.clear()
    }

    inner class LoadingViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    inner class ErrorViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
            itemView.buttonRetry.setOnClickListener { retryClickListener.invoke() }
        }
    }

}
