package com.intergroupapplication.presentation.feature.commentsdetails.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.appodeal.ads.NativeAd
import com.appodeal.ads.NativeAdView
import com.appodeal.ads.NativeIconView
import com.appodeal.ads.NativeMediaView
import com.appodeal.ads.native_ad.views.NativeAdViewAppWall
import com.appodeal.ads.native_ad.views.NativeAdViewContentStream
import com.appodeal.ads.native_ad.views.NativeAdViewNewsFeed
import com.intergroupapplication.R
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.doOrIfNull
import com.intergroupapplication.presentation.exstension.getDateDescribeByString
import com.intergroupapplication.presentation.exstension.inflate
import com.intergroupapplication.presentation.feature.commentsdetails.other.CommentEntityUI
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.item_comment.view.*
import kotlinx.android.synthetic.main.post_item_error.view.*
import timber.log.Timber

/**
 * Created by abakarmagomedov on 28/08/2018 at project InterGroupApplication.
 */
class CommentsAdapter(private val imageLoadingDelegate: ImageLoadingDelegate)
    : PagingDataAdapter<CommentEntityUI, RecyclerView.ViewHolder>(diffUtil) {

    companion object {
        private const val NATIVE_TYPE_NEWS_FEED = 1
        private const val NATIVE_TYPE_APP_WALL = 2
        private const val NATIVE_TYPE_CONTENT_STREAM = 3
        private const val NATIVE_WITHOUT_ICON = 4
        private const val VIEW_HOLDER_NATIVE_AD_TYPE = 600
        private const val DEFAULT_HOLDER = 1488
        private val diffUtil = object : DiffUtil.ItemCallback<CommentEntityUI>() {
            override fun areItemsTheSame(oldItem: CommentEntityUI, newItem: CommentEntityUI): Boolean {
                return if (oldItem is CommentEntityUI.CommentEntity && newItem is CommentEntityUI.CommentEntity) {
                    oldItem.id == newItem.id
                } else if (oldItem is CommentEntityUI.AdEntity && newItem is CommentEntityUI.AdEntity) {
                    oldItem.position == newItem.position
                } else {
                    false
                }
            }
            override fun areContentsTheSame(oldItem: CommentEntityUI, newItem: CommentEntityUI): Boolean {
                return if (oldItem is CommentEntityUI.CommentEntity && newItem is CommentEntityUI.CommentEntity) {
                    oldItem == newItem
                } else if (oldItem is CommentEntityUI.AdEntity && newItem is CommentEntityUI.AdEntity) {
                    oldItem == newItem
                } else {
                    false
                }
            }
        }
        var replyListener: (commentEntity: CommentEntityUI.CommentEntity) -> Unit = {}
        var complaintListener: (Int) -> Unit = {}
        var AD_TYPE = 1
        var AD_FREQ = 3
        var AD_FIRST = 3
    }


    private var compositeDisposable = CompositeDisposable()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
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
                return CommentViewHolder(parent.inflate(R.layout.item_comment))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let {
            if (holder is CommentViewHolder)
                holder.bind(it as CommentEntityUI.CommentEntity)
            else if (holder is NativeAdViewHolder) {
                holder.fillNative((it as CommentEntityUI.AdEntity).nativeAd)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CommentEntityUI.CommentEntity -> DEFAULT_HOLDER
            is CommentEntityUI.AdEntity -> AD_TYPE
            null -> throw IllegalStateException("Unknown view")
        }
    }

    inner class CommentViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: CommentEntityUI.CommentEntity) {
            with(itemView) {
                val name = item.commentOwner
                        ?.let { "${it.firstName} ${it.secondName}" }
                        ?: let { itemView.resources.getString(R.string.unknown_user) }
                userName.text = name
                idUser.text = context.getString(R.string.id,
                        item.commentOwner?.user ?: "нет id")
                postText.text = item.text
                compositeDisposable.add(getDateDescribeByString(item.date)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            timeComment.text = it
                        }, {
                            Timber.e(it)
                        }))
                userAvatarHolder.run {
                    doOrIfNull(item.commentOwner?.avatar, { imageLoadingDelegate.loadImageFromUrl(it, this) },
                            { imageLoadingDelegate.loadImageFromResources(R.drawable.application_logo, this) })
                }
                replyButton.setOnClickListener {
                    replyListener.invoke(item)
                }
//                doOrIfNull(item.answerTo, { //todo починить
//                    responseToOtherComment.text = context.getString(R.string.response_to,
//                            getUserNameByCommentId(it))
//                }, { responseToOtherComment.text = "" })
                idcGroupUser.text = itemView.context.getString(R.string.idc, item.id)
                settingsBtn.setOnClickListener { showPopupMenu(it, Integer.parseInt(item.id)) }

            }
        }

        private fun getUserNameByCommentId(commentId: String): String {
            val unknown = view.resources.getString(R.string.unknown)
            this@CommentsAdapter.snapshot().items.let {
                for (comment in it) {
                    if (comment is CommentEntityUI.CommentEntity) {
                        if (comment.id == commentId) {
                            return comment.commentOwner?.firstName ?: unknown
                        }
                    }
                }
            }
            return unknown
        }

    }

    override fun unregisterAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
        super.unregisterAdapterDataObserver(observer)
        compositeDisposable.clear()
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