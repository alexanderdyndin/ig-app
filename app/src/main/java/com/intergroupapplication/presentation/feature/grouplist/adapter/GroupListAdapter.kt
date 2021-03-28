package com.intergroupapplication.presentation.feature.grouplist.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.appodeal.ads.*
import com.appodeal.ads.native_ad.views.NativeAdViewAppWall
import com.appodeal.ads.native_ad.views.NativeAdViewContentStream
import com.appodeal.ads.native_ad.views.NativeAdViewNewsFeed
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.intergroupapplication.R
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.grouplist.other.GroupEntityUI
import kotlinx.android.synthetic.main.item_group_in_list.view.*


/**
 * Created by abakarmagomedov on 02/08/2018 at project InterGroupApplication.
 */
class GroupListAdapter(private val imageLoadingDelegate: ImageLoadingDelegate)
    : PagingDataAdapter<GroupEntityUI, RecyclerView.ViewHolder>(diffUtil) {

    companion object {
        var lettersToSpan = ""
        var userID: String? = null
        var groupClickListener: (groupId: String) -> Unit = {}
        var unsubscribeClickListener: (item: GroupEntityUI.GroupEntity, position: Int) -> Unit = {_, _ -> }
        var subscribeClickListener: (item: GroupEntityUI.GroupEntity, position: Int) -> Unit = {_, _ -> }
        private const val NATIVE_TYPE_NEWS_FEED = 1
        private const val NATIVE_TYPE_APP_WALL = 2
        private const val NATIVE_TYPE_CONTENT_STREAM = 3
        private const val NATIVE_WITHOUT_ICON = 4
        private const val VIEW_HOLDER_NATIVE_AD_TYPE = 600
        private const val DEFAULT_HOLDER = 1488
        private val diffUtil = object : DiffUtil.ItemCallback<GroupEntityUI>() {
            override fun areItemsTheSame(oldItem: GroupEntityUI, newItem: GroupEntityUI): Boolean {
                return if (oldItem is GroupEntityUI.GroupEntity && newItem is GroupEntityUI.GroupEntity) {
                    oldItem.id == newItem.id
                } else if (oldItem is GroupEntityUI.AdEntity && newItem is GroupEntityUI.AdEntity) {
                    oldItem.position == newItem.position
                } else {
                    false
                }
            }
            override fun areContentsTheSame(oldItem: GroupEntityUI, newItem: GroupEntityUI): Boolean {
                return if (oldItem is GroupEntityUI.GroupEntity && newItem is GroupEntityUI.GroupEntity) {
                    oldItem == newItem
                } else if (oldItem is GroupEntityUI.AdEntity && newItem is GroupEntityUI.AdEntity) {
                    oldItem == newItem
                } else {
                    false
                }
            }
        }
        var AD_TYPE = 1
        var AD_FREQ = 3
        var AD_FIRST = 3
    }

    private lateinit var context: Context

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
                GroupViewHolder(parent.inflate(R.layout.item_group_in_list))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is GroupViewHolder) {
            holder.bind(item as GroupEntityUI.GroupEntity)
        } else if (holder is NativeAdViewHolder) {
                holder.fillNative((item as GroupEntityUI.AdEntity).nativeAd)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is GroupEntityUI.GroupEntity -> DEFAULT_HOLDER
            is GroupEntityUI.AdEntity -> AD_TYPE
            null -> throw IllegalStateException("Unknown view")
        }
    }


    inner class GroupViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        val avatar: SimpleDraweeView = itemView.findViewById(R.id.groupAvatarHolder)

        fun bind(item: GroupEntityUI.GroupEntity) {
            with(itemView) {
                spanLetters(item)
                //item_group__subscribers.text = context.getGroupFollowersCount(item.followersCount.toInt())
                item_group__subscribers.text = item.followersCount
                item_group__posts.text = item.postsCount
                item_group__comments.text = item.CommentsCount
                item_group__dislike.text = item.postsLikes
                item_group__like.text = item.postsDislikes
                item_group__text_age.text = item.ageRestriction
                when(item.ageRestriction) {
                    "12+" -> {
                        item_group__text_age.setBackgroundResource(R.drawable.bg_age12)
                        item_group__text_age.setTextColor(ContextCompat.getColor(context, R.color.mainBlack))
                    }
                    "16+" -> {
                        item_group__text_age.setBackgroundResource(R.drawable.bg_age16)
                        item_group__text_age.setTextColor(ContextCompat.getColor(context, R.color.mainBlack))
                    }
                    "18+" -> {
                        item_group__text_age.setBackgroundResource(R.drawable.bg_age18)
                        item_group__text_age.setTextColor(ContextCompat.getColor(context, R.color.whiteTextColor))
                    }
                    else -> {
                        item_group__text_age.setBackgroundResource(R.drawable.bg_age12)
                        item_group__text_age.setTextColor(ContextCompat.getColor(context, R.color.mainBlack))
                    }
                }
                if (item.isClosed) {
                    item_group__lock.setImageResource(R.drawable.icon_close)
                    item_group__lock.setBackgroundResource(R.drawable.bg_lock)
                } else {
                    item_group__lock.setImageResource(R.drawable.icon_open)
                    item_group__lock.setBackgroundResource(R.drawable.bg_unlock)
                }
                groupAvatarHolder.setOnClickListener {
                    groupClickListener.invoke(item.id)
                }
                if (item.isSubscribing) {
                    subscribingProgressBar.show()
                    item_group__text_sub.hide()
                } else {
                    subscribingProgressBar.hide()
                    item_group__text_sub.show()
                }
                with (item_group__text_sub) {
                    if (item.isFollowing) {
                        setOnClickListener {
                            unsubscribeClickListener.invoke(item, layoutPosition)
                        }
                        text = resources.getText(R.string.unsubscribe)
                        setBackgroundResource(R.drawable.btn_unsub)
                    } else {
                        setOnClickListener {
                            subscribeClickListener.invoke(item, layoutPosition)
                        }
                        text = resources.getText(R.string.subscribe)
                        setBackgroundResource(R.drawable.btn_sub)
                    }
                    visibility = if (userID == item.owner) {
                        View.INVISIBLE
                    } else {
                        View.VISIBLE
                    }
                }
                doOrIfNull(item.avatar, {
                    val request: ImageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(item.avatar))
                            .setResizeOptions(ResizeOptions(100, 100))
                            .build()
                    avatar.controller = Fresco.newDraweeControllerBuilder()
                            .setAutoPlayAnimations(true)
                            .setOldController(avatar.controller)
                            .setImageRequest(request)
                            .build()
                }, { imageLoadingDelegate.loadImageFromResources(R.drawable.variant_10, avatar)
                })


            }
        }

        private fun spanLetters(item: GroupEntityUI.GroupEntity) {
                val spanStartPositions = item.name.mapIndexed { index: Int, c: Char -> item.name.indexOf(lettersToSpan, index, true) }.filterNot { it == -1 }.toSet()
                val wordToSpan: Spannable = SpannableString(item.name)
                spanStartPositions.forEach{
                    wordToSpan.setSpan(ForegroundColorSpan(Color.CYAN), it, it + lettersToSpan.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    wordToSpan.setSpan(StyleSpan(Typeface.BOLD), it, it + lettersToSpan.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                itemView.item_group__list_header.text = wordToSpan
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is GroupViewHolder) {
            holder.avatar.controller = null
        }
        super.onViewRecycled(holder)
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
                //ratingBar.rating = nativeAd?.rating
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
                //ratingBar.rating = nativeAd?.rating
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
