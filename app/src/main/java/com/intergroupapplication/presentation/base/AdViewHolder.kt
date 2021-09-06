package com.intergroupapplication.presentation.base

import android.view.View
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.appodeal.ads.NativeAd
import com.appodeal.ads.NativeAdView
import com.appodeal.ads.native_ad.views.NativeAdViewAppWall
import com.appodeal.ads.native_ad.views.NativeAdViewContentStream
import com.appodeal.ads.native_ad.views.NativeAdViewNewsFeed
import com.intergroupapplication.R
import com.intergroupapplication.presentation.exstension.gone
import com.intergroupapplication.presentation.exstension.show

class AdViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    companion object {
        const val NATIVE_AD = 123
        private const val NATIVE_TYPE_NEWS_FEED = 1
        private const val NATIVE_TYPE_APP_WALL = 2
        private const val NATIVE_TYPE_CONTENT_STREAM = 3
    }

    private val card: CardView = itemView.findViewById(R.id.ad_card)
    private val container: FrameLayout = itemView.findViewById(R.id.ad_container)

    fun bind(nativeAd: NativeAd?, adType: Int = 1, placement: String = "default") {
        nativeAd?.let {
            card.show()
            when (adType) {
                NATIVE_TYPE_NEWS_FEED -> {
                    val adView = NativeAdViewNewsFeed(itemView.context)
                    adView.setBackgroundColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.whiteTextColor
                        )
                    )
                    adView.setPlacement(placement)
                    adView.setNativeAd(it)
                    container.addView(adView)
                }
                NATIVE_TYPE_APP_WALL -> {
                    val adView = NativeAdViewAppWall(itemView.context)
                    adView.setBackgroundColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.whiteTextColor
                        )
                    )
                    adView.setPlacement(placement)
                    adView.setNativeAd(it)
                    container.addView(adView)
                }
                NATIVE_TYPE_CONTENT_STREAM -> {
                    val adView = NativeAdViewContentStream(itemView.context)
                    adView.setBackgroundColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.whiteTextColor
                        )
                    )
                    adView.setPlacement(placement)
                    adView.setNativeAd(it)
                    container.addView(adView)
                }
                else -> {
                    val adView = NativeAdViewNewsFeed(itemView.context)
                    adView.setBackgroundColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.whiteTextColor
                        )
                    )
                    adView.setPlacement(placement)
                    adView.setNativeAd(it)
                    container.addView(adView)
                }
            }
        } ?: let { card.gone() }
    }

    fun clear() {
        container.children.forEach {
            if (it is NativeAdView) {
                it.unregisterViewForInteraction()
                container.removeView(it)
            }
        }
    }
}
