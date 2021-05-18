package com.intergroupapplication.presentation.feature.news.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.appodeal.ads.*
import com.appodeal.ads.native_ad.views.NativeAdViewAppWall
import com.appodeal.ads.native_ad.views.NativeAdViewContentStream
import com.appodeal.ads.native_ad.views.NativeAdViewNewsFeed
import com.danikula.videocache.HttpProxyCacheServer
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.entity.NewsEntity
import com.intergroupapplication.presentation.base.AdViewHolder
import com.intergroupapplication.presentation.base.AdViewHolder.Companion.NATIVE_AD
import com.intergroupapplication.presentation.customview.AudioGalleryView
import com.intergroupapplication.presentation.customview.ImageGalleryView
import com.intergroupapplication.presentation.customview.VideoGalleryView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.item_group_post.view.*
import kotlinx.android.synthetic.main.item_loading.view.*
import kotlinx.android.synthetic.main.layout_pic.view.*
import timber.log.Timber


class NewsAdapter(private val imageLoadingDelegate: ImageLoadingDelegate,
                  private val cacheServer: HttpProxyCacheServer)
    : PagingDataAdapter<NewsEntity, RecyclerView.ViewHolder>(diffUtil) {

    companion object {
        private const val DEFAULT_HOLDER = 1488
        private val diffUtil = object : DiffUtil.ItemCallback<NewsEntity>() {
            override fun areItemsTheSame(oldItem: NewsEntity, newItem: NewsEntity): Boolean {
                return if (oldItem is NewsEntity.Post && newItem is NewsEntity.Post) {
                    oldItem.id == newItem.id
                } else if (oldItem is NewsEntity.AdEntity && newItem is NewsEntity.AdEntity) {
                    oldItem.position == newItem.position
                } else {
                    false
                }
            }
            override fun areContentsTheSame(oldItem: NewsEntity, newItem: NewsEntity): Boolean {
                return if (oldItem is NewsEntity.Post && newItem is NewsEntity.Post) {
                    oldItem == newItem
                } else if (oldItem is NewsEntity.AdEntity && newItem is NewsEntity.AdEntity) {
                    oldItem == newItem
                } else {
                    false
                }
            }
        }
        var AD_TYPE = 1
        var AD_FREQ = 3
        var AD_FIRST = 3
        var commentClickListener: (groupPostEntity: GroupPostEntity.PostEntity) -> Unit = {}
        var groupClickListener: (groupId: String) -> Unit = {}
        var complaintListener: (Int) -> Unit = {}
        var imageClickListener: (List<FileEntity>, Int) -> Unit = { _, _ -> }
        var likeClickListener: (isLike: Boolean, isDislike: Boolean, item: GroupPostEntity.PostEntity, position: Int) -> Unit = { _, _, _, _ -> }
        var deleteClickListener: (postId: Int, position: Int) -> Unit = { _, _ ->}
        var bellClickListener: (item: GroupPostEntity.PostEntity, position: Int) -> Unit = { _, _ ->}
        var USER_ID: Int? = null
    }

    private var compositeDisposable = CompositeDisposable()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return when (viewType) {
            NATIVE_AD -> {
                view = parent.inflate(R.layout.layout_admob_news)
                AdViewHolder(view)
            }
            else -> {
                PostViewHolder(parent.inflate(R.layout.item_group_post))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let {
            if (holder is PostViewHolder && it is NewsEntity.Post)
                holder.bind(it)
            else if (holder is AdViewHolder && it is NewsEntity.AdEntity) {
                holder.bind(it.nativeAd, AD_TYPE)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is NewsEntity.Post -> DEFAULT_HOLDER
            is NewsEntity.AdEntity -> NATIVE_AD
            null -> throw IllegalStateException("Unknown view")
        }
    }

    inner class PostViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val audioContainer = itemView.findViewById<AudioGalleryView>(R.id.audioBody)
        val videoContainer = itemView.findViewById<VideoGalleryView>(R.id.videoBody)
        val imageContainer = itemView.findViewById<ImageGalleryView>(R.id.imageBody)

        fun bind(item: NewsEntity.Post) {
            with(itemView) {
                idpGroupPost.text = context.getString(R.string.idp, item.post.idp.toString())
                postLike.text = item.post.reacts.likesCount.toString()
                if (item.post.reacts.isLike) {
                    postLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_like_active, 0, 0, 0)
                } else {
                    postLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_like, 0, 0, 0)
                }
                postDislike.text = item.post.reacts.dislikesCount.toString()
                if (item.post.reacts.isDislike) {
                    postDislike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_dislike_active, 0, 0, 0)
                } else {
                    postDislike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_dislike, 0, 0, 0)
                }
                compositeDisposable.add(getDateDescribeByString(item.post.date)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ postPrescription.text = it }, { Timber.e(it) }))
                commentBtn.text = context.getString(R.string.comments_count, item.post.commentsCount, item.post.unreadComments)
                item.post.postText.let { it ->
                    if (it.isNotEmpty()) {
                        postText.text = item.post.postText
                        postText.show()
                        postText.setOnClickListener {
                            commentClickListener.invoke(item.post)
                        }
                    } else {
                        postText.gone()
                    }
                }
                groupName.text = item.post.groupInPost.name
                subCommentBtn.text = item.post.bells.count.toString()
                if (item.post.bells.isActive) {
                    subCommentBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_sub_comnts_blue, 0, 0, 0)
                } else {
                    subCommentBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_sub_comnts_grey, 0, 0, 0)
                }

//                anchorBtn.isVisible = item.post.isPinned

                subCommentBtn.setOnClickListener {
                    bellClickListener.invoke(item.post, layoutPosition)
                }
                commentBtn.setOnClickListener {
                    commentClickListener.invoke(item.post)
                }
                postAvatarHolder.setOnClickListener {
                    groupClickListener.invoke(item.post.groupInPost.id)
                }
                headerPostFromGroup.setOnClickListener {
                    groupClickListener.invoke(item.post.groupInPost.id)
                }
                postLikesClickArea.setOnClickListener {
                    likeClickListener.invoke(!item.post.reacts.isLike, item.post.reacts.isDislike, item.post, layoutPosition)
                }
                postDislikesClickArea.setOnClickListener {
                    likeClickListener.invoke(item.post.reacts.isLike, !item.post.reacts.isDislike, item.post, layoutPosition)
                }
                settingsPost.setOnClickListener { showPopupMenu(settingsPost, Integer.parseInt(item.post.id), item.id, item.post.author.id) }

                doOrIfNull(item.post.groupInPost.avatar, { imageLoadingDelegate.loadImageFromUrl(it, postAvatarHolder) },
                        { imageLoadingDelegate.loadImageFromResources(R.drawable.application_logo, postAvatarHolder) })

                videoContainer.proxy = cacheServer
                videoContainer.setVideos(item.post.videos, item.post.videosExpanded)
                videoContainer.expand = { item.post.videosExpanded = it }

                audioContainer.proxy = cacheServer
                audioContainer.setAudios(item.post.audios, item.post.audiosExpanded)
                audioContainer.expand = { item.post.audiosExpanded = it }

                imageContainer.setImages(item.post.images, item.post.imagesExpanded)
                imageContainer.imageClick = imageClickListener
                imageContainer.expand = { item.post.imagesExpanded = it }
            }
        }


        private fun showPopupMenu(view: View, postId: Int, newsId: Int, userId: Int) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.inflate(R.menu.settings_menu)
//            popupMenu.menu.findItem(R.id.delete).isVisible = USER_ID == userId
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.complaint -> complaintListener.invoke(postId)
                    R.id.delete -> deleteClickListener.invoke(newsId, layoutPosition)
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


    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is PostViewHolder) {
            holder.imageContainer.destroy()
        } else if (holder is AdViewHolder) {
            holder.clear()
        }
        super.onViewRecycled(holder)
    }

}