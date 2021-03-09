package com.intergroupapplication.presentation.feature.news.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.children
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.appodeal.ads.*
import com.appodeal.ads.native_ad.views.NativeAdViewAppWall
import com.appodeal.ads.native_ad.views.NativeAdViewContentStream
import com.appodeal.ads.native_ad.views.NativeAdViewNewsFeed
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.presentation.feature.news.other.GroupPostEntityUI
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.item_group_post.view.*
import kotlinx.android.synthetic.main.item_loading.view.*
import timber.log.Timber

class NewsAdapter(private val imageLoadingDelegate: ImageLoadingDelegate)
    : PagingDataAdapter<GroupPostEntityUI, RecyclerView.ViewHolder>(diffUtil) {

    companion object {
        private const val NATIVE_TYPE_NEWS_FEED = 1
        private const val NATIVE_TYPE_APP_WALL = 2
        private const val NATIVE_TYPE_CONTENT_STREAM = 3
        private const val NATIVE_WITHOUT_ICON = 4
        private const val VIEW_HOLDER_NATIVE_AD_TYPE = 600
        private const val DEFAULT_HOLDER = 1488
        private val diffUtil = object : DiffUtil.ItemCallback<GroupPostEntityUI>() {
            override fun areItemsTheSame(oldItem: GroupPostEntityUI, newItem: GroupPostEntityUI): Boolean {
                return if (oldItem is GroupPostEntityUI.GroupPostEntity && newItem is GroupPostEntityUI.GroupPostEntity) {
                     oldItem.id == newItem.id
                } else if (oldItem is GroupPostEntityUI.AdEntity && newItem is GroupPostEntityUI.AdEntity) {
                    oldItem.position == newItem.position
                } else {
                    false
                }
            }
            override fun areContentsTheSame(oldItem: GroupPostEntityUI, newItem: GroupPostEntityUI): Boolean {
                return if (oldItem is GroupPostEntityUI.GroupPostEntity && newItem is GroupPostEntityUI.GroupPostEntity) {
                    oldItem == newItem
                } else if (oldItem is GroupPostEntityUI.AdEntity && newItem is GroupPostEntityUI.AdEntity) {
                    oldItem == newItem
                } else {
                    false
                }
            }
        }
        var AD_TYPE = 1
        var AD_FREQ = 3
        var AD_FIRST = 3
        var commentClickListener: (groupPostEntity: GroupPostEntityUI) -> Unit = {}
        var groupClickListener: (groupId: String) -> Unit = {}
        var complaintListener: (Int) -> Unit = {}
        var imageClickListener: (List<FileEntity>, Int) -> Unit = { list: List<FileEntity>, i: Int -> }
        var likeClickListener: (postId: String) -> Unit = { }
        var dislikeClickListener: (postId: String) -> Unit = { }
    }

    private lateinit var context: Context
    private var compositeDisposable = CompositeDisposable()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        val view: View
        return when (viewType) {
            NATIVE_TYPE_NEWS_FEED -> {
                view = NativeAdViewNewsFeed(parent.context)
                NativeCreatedAdViewHolder(view)
            }
            NATIVE_TYPE_APP_WALL -> {
                view = NativeAdViewAppWall(parent.context)
                NativeCreatedAdViewHolder(view)
            }
            NATIVE_TYPE_CONTENT_STREAM -> {
                view = NativeAdViewContentStream(parent.context)
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
                return PostViewHolder(parent.inflate(R.layout.item_group_post))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let {
            if (holder is PostViewHolder)
                holder.bind(it as GroupPostEntityUI.GroupPostEntity)
            else if (holder is NativeAdViewHolder) {
                holder.fillNative((it as GroupPostEntityUI.AdEntity).nativeAd)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is GroupPostEntityUI.GroupPostEntity -> DEFAULT_HOLDER
            is GroupPostEntityUI.AdEntity -> AD_TYPE
            null -> throw IllegalStateException("Unknown view")
        }
    }


    inner class PostViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: GroupPostEntityUI.GroupPostEntity) {
            with(itemView) {
                compositeDisposable.add(getDateDescribeByString(item.date)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ postPrescription.text = it }, { Timber.e(it) }))
                postCommentsCount.text = item.commentsCount
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
                commentImageClickArea.setOnClickListener {
                    commentClickListener.invoke(item)
                }
                groupPostAvatar.setOnClickListener {
                    groupClickListener.invoke(item.groupInPost.id)
                }
                goToGroupClickArea.setOnClickListener {
                    groupClickListener.invoke(item.groupInPost.id)
                }
                likeClickArea.setOnClickListener {
                    likeClickListener.invoke(item.id)
                }
                dislikeClickArea.setOnClickListener {
                    dislikeClickListener.invoke(item.id)
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
                mediaBody.removeAllViews()
                item.images.forEach { file ->
                    val image = SimpleDraweeView(itemView.context)
                    image.layoutParams = ViewGroup.LayoutParams(400, 400)
                    //image.scaleType = ImageView.ScaleType.CENTER_CROP
                    image.setOnClickListener { imageClickListener.invoke(item.images, item.images.indexOf(file)) }
//                    if (file.file.contains(".gif")) {
                        val controller = Fresco.newDraweeControllerBuilder()
                                .setUri(Uri.parse(file.file))
                                .setAutoPlayAnimations(true)
                                .build()
                        image.controller = controller
//                    } else {
//                        imageLoadingDelegate.loadImageFromUrl(file.file, image)
//                    }
                    imageContainer.addView(image)
                }
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

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is PostViewHolder) {
            holder.view.imageContainer.children.forEach {
                if (it is SimpleDraweeView) {
                    it.controller = null
                }
            }
            holder.view.imageContainer.removeAllViews()
        }

        super.onViewRecycled(holder)
    }

}