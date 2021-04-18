package com.intergroupapplication.presentation.feature.group.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
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
import com.intergroupapplication.presentation.customview.AudioGalleryView
import com.intergroupapplication.presentation.customview.ImageGalleryView
import com.intergroupapplication.presentation.customview.VideoGalleryView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.news.adapter.NewsAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.item_group_post.view.*
import kotlinx.android.synthetic.main.item_loading.view.*
import timber.log.Timber

class GroupPostsAdapter(private val imageLoadingDelegate: ImageLoadingDelegate,
                        private val proxyCacheServer: HttpProxyCacheServer)
    : PagingDataAdapter<GroupPostEntity, RecyclerView.ViewHolder>(diffUtil) {

    companion object {
        private const val NATIVE_TYPE_NEWS_FEED = 1
        private const val NATIVE_TYPE_APP_WALL = 2
        private const val NATIVE_TYPE_CONTENT_STREAM = 3
        private const val NATIVE_WITHOUT_ICON = 4
        private const val VIEW_HOLDER_NATIVE_AD_TYPE = 600
        private const val DEFAULT_HOLDER = 1488
        private val diffUtil = object : DiffUtil.ItemCallback<GroupPostEntity>() {
            override fun areItemsTheSame(oldItem: GroupPostEntity, newItem: GroupPostEntity): Boolean {
                return if (oldItem is GroupPostEntity.PostEntity && newItem is GroupPostEntity.PostEntity) {
                    oldItem.id == newItem.id
                } else if (oldItem is GroupPostEntity.AdEntity && newItem is GroupPostEntity.AdEntity) {
                    oldItem.position == newItem.position
                } else {
                    false
                }
            }
            override fun areContentsTheSame(oldItem: GroupPostEntity, newItem: GroupPostEntity): Boolean {
                return if (oldItem is GroupPostEntity.PostEntity && newItem is GroupPostEntity.PostEntity) {
                    oldItem == newItem
                } else if (oldItem is GroupPostEntity.AdEntity && newItem is GroupPostEntity.AdEntity) {
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
        var complaintListener: (Int) -> Unit = {}
        var imageClickListener: (List<FileEntity>, Int) -> Unit = { list: List<FileEntity>, i: Int -> }
        var likeClickListener: (isLike: Boolean, isDislike: Boolean, item: GroupPostEntity.PostEntity, position: Int) -> Unit = { _, _, _, _ -> }
        var deleteClickListener: (postId: Int, position: Int) -> Unit = { _, _ ->}
        var bellClickListener: (item: GroupPostEntity.PostEntity, position: Int) -> Unit = { _, _ ->}
        var pinClickListener: (item: GroupPostEntity.PostEntity, position: Int) -> Unit = { _, _ ->}
        var isOwner = false
    }

    private var compositeDisposable = CompositeDisposable()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return when (viewType) {
            NATIVE_TYPE_NEWS_FEED -> {
                view = NativeAdViewNewsFeed(parent.context)
                view.setBackgroundColor(ContextCompat.getColor(parent.context, R.color.whiteTextColor))
                NativeCreatedAdViewHolder(view)
            }
            NATIVE_TYPE_APP_WALL -> {
                view = NativeAdViewAppWall(parent.context)
                view.setBackgroundColor(ContextCompat.getColor(parent.context, R.color.whiteTextColor))
                NativeCreatedAdViewHolder(view)
            }
            NATIVE_TYPE_CONTENT_STREAM -> {
                view = NativeAdViewContentStream(parent.context)
                view.setBackgroundColor(ContextCompat.getColor(parent.context, R.color.whiteTextColor))
                NativeCreatedAdViewHolder(view)
            }
            NATIVE_WITHOUT_ICON -> {
                view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.native_ads_without_icon, parent, false)
                NativeWithoutIconHolder(view)
            }
            VIEW_HOLDER_NATIVE_AD_TYPE -> {
                view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.include_native_ads, parent, false)
                NativeCustomAdViewHolder(view)
            }
            else -> {
                PostViewHolder(parent.inflate(R.layout.item_group_post))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let {
            if (holder is PostViewHolder && it is GroupPostEntity.PostEntity)
                holder.bind(it)
            else if (holder is NativeAdViewHolder && it is GroupPostEntity.AdEntity) {
                holder.fillNative(it.nativeAd)
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is PostViewHolder) {
            holder.imageContainer.destroy()
        }
        super.onViewRecycled(holder)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is GroupPostEntity.PostEntity -> DEFAULT_HOLDER
            is GroupPostEntity.AdEntity -> AD_TYPE
            null -> throw IllegalStateException("Unknown view")
        }
    }

    inner class PostViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val audioContainer = itemView.findViewById<AudioGalleryView>(R.id.audioBody)
        val videoContainer = itemView.findViewById<VideoGalleryView>(R.id.videoBody)
        val imageContainer = itemView.findViewById<ImageGalleryView>(R.id.imageBody)

        fun bind(item: GroupPostEntity.PostEntity) {
            with(itemView) {
                idpGroupPost.text = context.getString(R.string.idp, item.idp.toString())
                postLike.text = item.reacts.likesCount.toString()
                postDislike.text = item.reacts.dislikesCount.toString()
                if (item.reacts.isLike) {
                    postLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_like_active, 0, 0, 0)
                } else {
                    postLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_like, 0, 0, 0)
                }
                if (item.reacts.isDislike) {
                    postDislike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_dislike_active, 0, 0, 0)
                } else {
                    postDislike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_dislike, 0, 0, 0)
                }
                compositeDisposable.add(getDateDescribeByString(item.date)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ postPrescription.text = it }, { Timber.e(it) }))
                commentBtn.text = context.getString(R.string.comments_count, item.commentsCount, item.unreadComments)
                item.postText.let { it ->
                    if (it.isNotEmpty()) {
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
                subCommentBtn.text = item.bells.count.toString()
                if (item.bells.isActive) {
                    subCommentBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_sub_comnts_blue, 0, 0, 0)
                } else {
                    subCommentBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_sub_comnts_grey, 0, 0, 0)
                }

                anchorBtn.setOnClickListener { pinClickListener.invoke(item, layoutPosition) }

                if (isOwner) {
                    anchorBtn.show()
                    if (item.isPinned) {
                        anchorBtn.setBackgroundResource(R.drawable.btn_anchor_act)
                        anchorBtn.setImageResource(R.drawable.ic_anchor_act)
                    } else {
                        anchorBtn.setBackgroundResource(R.drawable.btn_anchor)
                        anchorBtn.setImageResource(R.drawable.ic_anchor)
                    }
                } else {
                    anchorBtn.gone()
                }

                subCommentBtn.setOnClickListener {
                    bellClickListener.invoke(item, layoutPosition)
                }
                commentBtn.setOnClickListener {
                    commentClickListener.invoke(item)
                }
                postLikesClickArea.setOnClickListener {
                    likeClickListener.invoke(!item.reacts.isLike, item.reacts.isDislike, item, layoutPosition)
                }
                postDislikesClickArea.setOnClickListener {
                    likeClickListener.invoke(item.reacts.isLike, !item.reacts.isDislike, item, layoutPosition)
                }
                settingsPost.setOnClickListener { showPopupMenu(settingsPost, Integer.parseInt(item.id)) }

                doOrIfNull(item.groupInPost.avatar, { imageLoadingDelegate.loadImageFromUrl(it, postAvatarHolder) },
                        { imageLoadingDelegate.loadImageFromResources(R.drawable.variant_10, postAvatarHolder) })

                videoContainer.proxy = proxyCacheServer
                videoContainer.setVideos(item.videos, item.videosExpanded)
                videoContainer.expand = { item.videosExpanded = it }

                audioContainer.proxy = proxyCacheServer
                audioContainer.setAudios(item.audios, item.audiosExpanded)
                audioContainer.expand = { item.audiosExpanded = it }

                imageContainer.setImages(item.images, item.imagesExpanded)
                imageContainer.imageClick = imageClickListener
                imageContainer.expand = { item.imagesExpanded = it }
            }
        }

        private fun showPopupMenu(view: View, id: Int) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.inflate(R.menu.settings_menu)
            popupMenu.menu.findItem(R.id.delete).isVisible = isOwner
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.complaint -> complaintListener.invoke(id)
                    R.id.delete -> deleteClickListener.invoke(id, layoutPosition)
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


    internal class NativeCustomAdViewHolder(itemView: View) : NativeAdViewHolder(itemView) {
        private val nativeAdView: NativeAdView
        private val tvTitle: TextView
        private val tvDescription: TextView
        private val ratingBar: RatingBar
        private val ctaButton: Button
        private val nativeIconView: NativeIconView
        private val tvAgeRestrictions: TextView
        private val nativeMediaView: NativeMediaView
        private val providerViewContainer: FrameLayout
        override fun fillNative(nativeAd: NativeAd?) {
            tvTitle.text = nativeAd?.title
            tvDescription.text = nativeAd?.description
            if (nativeAd?.rating == 0f) {
                ratingBar.visibility = View.INVISIBLE
            } else {
                ratingBar.visibility = View.VISIBLE
                nativeAd?.rating?.let {
                    ratingBar.rating = it }
                ratingBar.stepSize = 0.1f
            }
            ctaButton.text = nativeAd?.callToAction
            val providerView = nativeAd?.getProviderView(nativeAdView.context)
            if (providerView != null) {
                if (providerView.parent != null && providerView.parent is ViewGroup) {
                    (providerView.parent as ViewGroup).removeView(providerView)
                }
                providerViewContainer.removeAllViews()
                val layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                providerViewContainer.addView(providerView, layoutParams)
            }
            if (nativeAd?.ageRestrictions != null) {
                tvAgeRestrictions.text = nativeAd.ageRestrictions
                tvAgeRestrictions.visibility = View.VISIBLE
            } else {
                tvAgeRestrictions.visibility = View.GONE
            }
            if (nativeAd?.containsVideo() == true) {
                nativeAdView.nativeMediaView = nativeMediaView
                nativeMediaView.visibility = View.VISIBLE
            } else {
                nativeMediaView.visibility = View.GONE
            }
            nativeAdView.titleView = tvTitle
            nativeAdView.descriptionView = tvDescription
            nativeAdView.ratingView = ratingBar
            nativeAdView.callToActionView = ctaButton
            nativeAdView.setNativeIconView(nativeIconView)
            nativeAdView.providerView = providerView
            nativeAdView.registerView(nativeAd)
            nativeAdView.visibility = View.VISIBLE
        }

        override fun unregisterViewForInteraction() {
            nativeAdView.unregisterViewForInteraction()
        }

        init {
            nativeAdView = itemView.findViewById(R.id.native_item)
            tvTitle = itemView.findViewById(R.id.tv_title)
            tvDescription = itemView.findViewById(R.id.tv_description)
            ratingBar = itemView.findViewById(R.id.rb_rating)
            ctaButton = itemView.findViewById(R.id.b_cta)
            nativeIconView = itemView.findViewById(R.id.icon)
            providerViewContainer = itemView.findViewById(R.id.provider_view)
            tvAgeRestrictions = itemView.findViewById(R.id.tv_age_restriction)
            nativeMediaView = itemView.findViewById(R.id.appodeal_media_view_content)
        }
    }

    /**
     * View holder for create custom NativeAdView without NativeIconView
     */
    internal class NativeWithoutIconHolder(itemView: View) : NativeAdViewHolder(itemView) {
        private val nativeAdView: NativeAdView
        private val tvTitle: TextView
        private val tvDescription: TextView
        private val ratingBar: RatingBar
        private val ctaButton: Button
        private val tvAgeRestrictions: TextView
        private val nativeMediaView: NativeMediaView
        private val providerViewContainer: FrameLayout
        override fun fillNative(nativeAd: NativeAd?) {
            tvTitle.text = nativeAd?.title
            tvDescription.text = nativeAd?.description
            if (nativeAd?.rating == 0f) {
                ratingBar.visibility = View.INVISIBLE
            } else {
                ratingBar.visibility = View.VISIBLE
                nativeAd?.rating?.let {
                    ratingBar.rating = it }
                ratingBar.stepSize = 0.1f
            }
            ctaButton.text = nativeAd?.callToAction
            val providerView = nativeAd?.getProviderView(nativeAdView.context)
            if (providerView != null) {
                if (providerView.parent != null && providerView.parent is ViewGroup) {
                    (providerView.parent as ViewGroup).removeView(providerView)
                }
                providerViewContainer.removeAllViews()
                val layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                providerViewContainer.addView(providerView, layoutParams)
            }
            if (nativeAd?.ageRestrictions != null) {
                tvAgeRestrictions.text = nativeAd.ageRestrictions
                tvAgeRestrictions.visibility = View.VISIBLE
            } else {
                tvAgeRestrictions.visibility = View.GONE
            }
            if (nativeAd?.containsVideo() == true) {
                nativeAdView.nativeMediaView = nativeMediaView
            } else {
                nativeMediaView.visibility = View.GONE
            }
            nativeAdView.titleView = tvTitle
            nativeAdView.descriptionView = tvDescription
            nativeAdView.ratingView = ratingBar
            nativeAdView.callToActionView = ctaButton
            nativeAdView.providerView = providerView
            nativeAdView.registerView(nativeAd)
            nativeAdView.visibility = View.VISIBLE
        }

        override fun unregisterViewForInteraction() {
            nativeAdView.unregisterViewForInteraction()
        }

        init {
            nativeAdView = itemView.findViewById(R.id.native_item)
            tvTitle = itemView.findViewById(R.id.tv_title)
            tvDescription = itemView.findViewById(R.id.tv_description)
            ratingBar = itemView.findViewById(R.id.rb_rating)
            ctaButton = itemView.findViewById(R.id.b_cta)
            providerViewContainer = itemView.findViewById(R.id.provider_view)
            tvAgeRestrictions = itemView.findViewById(R.id.tv_age_restriction)
            nativeMediaView = itemView.findViewById(R.id.appodeal_media_view_content)
        }
    }

    /**
     * View holder for create NativeAdView by template
     */
    internal class NativeCreatedAdViewHolder(itemView: View?) : NativeAdViewHolder(itemView) {
        override fun fillNative(nativeAd: NativeAd?) {
            if (itemView is NativeAdViewNewsFeed) {
                itemView.setNativeAd(nativeAd)
            } else if (itemView is NativeAdViewAppWall) {
                itemView.setNativeAd(nativeAd)
            } else if (itemView is NativeAdViewContentStream) {
                itemView.setNativeAd(nativeAd)
            }
        }

        override fun unregisterViewForInteraction() {
            if (itemView is NativeAdViewNewsFeed) {
                itemView.unregisterViewForInteraction()
            } else if (itemView is NativeAdViewAppWall) {
                itemView.unregisterViewForInteraction()
            } else if (itemView is NativeAdViewContentStream) {
                itemView.unregisterViewForInteraction()
            }
        }
    }

    /**
     * Abstract view holders for create NativeAdView
     */
    internal abstract class NativeAdViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        abstract fun fillNative(nativeAd: NativeAd?)
        abstract fun unregisterViewForInteraction()
    }

}