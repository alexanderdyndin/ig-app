package com.intergroupapplication.presentation.base.adapter

import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.appodeal.ads.*
import com.appodeal.ads.NativeCallbacks
import com.appodeal.ads.native_ad.views.NativeAdViewAppWall
import com.appodeal.ads.native_ad.views.NativeAdViewContentStream
import com.appodeal.ads.native_ad.views.NativeAdViewNewsFeed
import com.intergroupapplication.R

class AppodealWrapperAdapter constructor(userAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
                                                  nativeStep: Int,
                                                  nativeTemplateType: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), NativeCallbacks {
    private val userAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>?
    private val nativeStep: Int
    private val nativeTemplateType: Int
    private val nativeAdList = SparseArray<NativeAd?>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_HOLDER_NATIVE_AD_TYPE) {
            val view: View
            when (nativeTemplateType) {
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
                else -> {
                    view = LayoutInflater.from(parent.context)
                            .inflate(R.layout.include_native_ads, parent, false)
                    NativeCustomAdViewHolder(view)
                }
            }
        } else {
            userAdapter!!.onCreateViewHolder(parent, viewType)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is NativeAdViewHolder) {
            holder.fillNative(nativeAdList[position])
        } else {
            userAdapter!!.onBindViewHolder(holder, getPositionInUserAdapter(position))
        }
    }

    override fun getItemCount(): Int {
        var resultCount = 0
        resultCount += nativeAdsCount
        resultCount += userAdapterItemCount
        return resultCount
    }

    override fun getItemViewType(position: Int): Int {
        return if (isNativeAdPosition(position)) {
            VIEW_HOLDER_NATIVE_AD_TYPE
        } else {
            userAdapter!!.getItemViewType(getPositionInUserAdapter(position))
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is NativeAdViewHolder) {
            holder.unregisterViewForInteraction()
        }
    }

    /**
     * Destroy all used native ads
     */
    fun destroyNativeAds() {
        for (i in 0 until nativeAdList.size()) {
            val nativeAd = nativeAdList.valueAt(i)
            nativeAd!!.destroy()
        }
        nativeAdList.clear()
    }

    override fun onNativeLoaded() {
        fillListWithAd()
    }

    override fun onNativeFailedToLoad() {}
    override fun onNativeShown(nativeAd: NativeAd) {}
    override fun onNativeShowFailed(nativeAd: NativeAd) {}
    override fun onNativeClicked(nativeAd: NativeAd) {}
    override fun onNativeExpired() {}

    /**
     * @return count of loaded ads [com.appodeal.ads.NativeAd]
     */
    private val nativeAdsCount: Int
        private get() = nativeAdList.size()

    /**
     * @return user items count
     */
    private val userAdapterItemCount: Int
        private get() = userAdapter?.itemCount ?: 0

    /**
     * @param position index in wrapper adapter
     * @return `true` if item by position is [com.appodeal.ads.NativeAd]
     */
    private fun isNativeAdPosition(position: Int): Boolean {
        return nativeAdList[position] != null
    }

    /**
     * Method for searching position in user adapter
     *
     * @param position index in wrapper adapter
     * @return index in user adapter
     */
    private fun getPositionInUserAdapter(position: Int): Int {

        return if (position>1)
            position - 1 - nativeAdList.size().coerceAtMost(position / nativeStep)
        else
            position - nativeAdList.size().coerceAtMost(position / nativeStep)
    }

    /**
     * Method for filling list with [com.appodeal.ads.NativeAd]
     */
    private fun fillListWithAd() {
        var insertPosition = findNextAdPosition()
        var nativeAd: NativeAd? = null
        while (canUseThisPosition(insertPosition) && nativeAdItem.also { nativeAd = it } != null) {
            nativeAdList.put(insertPosition, nativeAd)
            notifyItemInserted(insertPosition)
            insertPosition = findNextAdPosition()
        }
    }

    /**
     * Get native ad item
     *
     * @return [com.appodeal.ads.NativeAd]
     */
    private val nativeAdItem: NativeAd?
        private get() {
            val ads = Appodeal.getNativeAds(1)
            return if (!ads.isEmpty()) ads[0] else null
        }

    /**
     * Method for finding next position suitable for [com.appodeal.ads.NativeAd]
     *
     * @return position for next native ad view
     */
    private fun findNextAdPosition(): Int {
        return if (nativeAdList.size() > 0) {
            nativeAdList.keyAt(nativeAdList.size() - 1) + nativeStep
        } else nativeStep - 1
    }

    /**
     * @param position index in wrapper adapter
     * @return `true` if you can add [com.appodeal.ads.NativeAd] to this position
     */
    private fun canUseThisPosition(position: Int): Boolean {
        return nativeAdList[position] == null && itemCount > position
    }

    /**
     * View holder for create custom NativeAdView
     */
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

    companion object {
        private const val NATIVE_TYPE_NEWS_FEED = 1
        private const val NATIVE_TYPE_APP_WALL = 2
        private const val NATIVE_TYPE_CONTENT_STREAM = 3
        private const val NATIVE_WITHOUT_ICON = 4
        private const val VIEW_HOLDER_NATIVE_AD_TYPE = 600
    }

    /**
     * @param userAdapter user adapter
     * @param nativeStep  step show [com.appodeal.ads.NativeAd]
     */
    init {
        this.userAdapter = userAdapter
        this.nativeStep = nativeStep + 1
        this.nativeTemplateType = nativeTemplateType
        userAdapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                notifyDataSetChanged()
                fillListWithAd()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                notifyDataSetChanged()
                fillListWithAd()
            }
        })
        Appodeal.setNativeCallbacks(this)
        fillListWithAd()
    }
}