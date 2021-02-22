package com.intergroupapplication.presentation.feature.group.adapter

import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.marginBottom
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.FileEntity
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
    var imageClickListener: (List<FileEntity>, Int) -> Unit = { list: List<FileEntity>, i: Int -> }

    private val loadingViewType = 123       //todo Почему это переменная экземпляра? Мб лучше вынести в статик?
    private val errorViewType = 321
    private var isLoading = false
    private var isError = false
    private var compositeDisposable = CompositeDisposable()

    companion object {
        val TEST_VIDEO_URI = "https://intergroupmedia.s3-us-west-2.amazonaws.com/index2.mp4"
        val TEST_MUSIC_URI = "https://intergroupmedia.s3-us-west-2.amazonaws.com/videoplayback.webm"
    }


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
            holder.itemView.mediaBody.children.forEach {
                if (it is StyledPlayerView) {
                    it.player?.release()
                }
            }

//            holder.videoPlayerView.player?.release()
//            holder.videoPlayerView.player?.let {
//                Toast.makeText(holder.videoPlayerView.context, "Player released", Toast.LENGTH_SHORT).show()
//            }
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
        val videoPlayerView = itemView.findViewById<StyledPlayerView>(R.id.videoExoPlayerView)
        val musicPlayerView = itemView.findViewById<PlayerView>(R.id.musicExoPlayerView)

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
//                item.photo.apply {
//                    ifNotNull {
//                        postImage.show()
//                        imageLoadingDelegate.loadImageFromUrl(it, postImage)
//                    }
//                    ifNull { postImage.gone() }
//                }
//                doOrIfNull(item.groupInPost.avatar, { imageLoadingDelegate.loadImageFromUrl(it, groupPostAvatar) },
//                        { imageLoadingDelegate.loadImageFromResources(R.drawable.application_logo, groupPostAvatar) })
                settingsPost.setOnClickListener { showPopupMenu(settingsPost, Integer.parseInt(item.id)) }
                if (item.audios.isNotEmpty())
                    initializeAudioPlayer(item.audios[0].file)
                else
                    initializeAudioPlayer(TEST_MUSIC_URI)
                mediaBody.removeAllViews()
                imageContainer.removeAllViews()
                item.videos.forEach {
                    val player = StyledPlayerView(itemView.context)
                    player.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800)
                    val videoPlayer = SimpleExoPlayer.Builder(videoPlayerView.context).build()
                    player.player = videoPlayer
                    // Build the media item.
                    val videoMediaItem: MediaItem = MediaItem.fromUri(it.file)
                    // Set the media item to be played.
                    videoPlayer.setMediaItem(videoMediaItem)
                    // Prepare the player.
                    videoPlayer.prepare()
                    player.setShowBuffering(StyledPlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                    mediaBody.addView(player)
                }
                item.images.forEach { file ->
                    val image = SimpleDraweeView(itemView.context)
                    if (file.file.contains(".gif")) {
                        val controller = Fresco.newDraweeControllerBuilder()
                                .setUri(Uri.parse(file.file))
                                .setAutoPlayAnimations(true)
                                .build()
                        image.controller = controller
                    } else {
                        imageLoadingDelegate.loadImageFromUrl(file.file, image)
                    }
                    image.layoutParams = ViewGroup.LayoutParams(400, 400)
                    //image.scaleType = ImageView.ScaleType.CENTER_CROP
                    image.setOnClickListener { imageClickListener.invoke(item.images, item.images.indexOf(file)) }
                    image.controller?.animatable?.start()
                    imageContainer.addView(image)
                }
            }
        }

        private fun initializeVideoPlayer(uri: String) {
            val videoPlayer = SimpleExoPlayer.Builder(videoPlayerView.context).build()
            videoPlayerView.player = videoPlayer
            // Build the media item.
            val videoMediaItem: MediaItem = MediaItem.fromUri(uri)        //Todo юри видео должно быть в Entity
            // Set the media item to be played.
            videoPlayer.setMediaItem(videoMediaItem)
            // Prepare the player.
            videoPlayer.prepare()
        }

        private fun initializeAudioPlayer(uri: String) {
            val musicPlayer = SimpleExoPlayer.Builder(musicPlayerView.context).build()
            musicPlayerView.player = musicPlayer
            // Build the media item.
            val musicMediaItem: MediaItem = MediaItem.fromUri(uri)        //Todo юри аудио должно быть в Entity
            // Set the media item to be played.
            musicPlayer.setMediaItem(musicMediaItem)
            // Prepare the player.
            musicPlayer.prepare()
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
