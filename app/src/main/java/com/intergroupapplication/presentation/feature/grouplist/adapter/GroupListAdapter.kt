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
import by.kirich1409.viewbindingdelegate.viewBinding
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
import com.intergroupapplication.databinding.ItemGroupInListBinding
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.presentation.base.AdViewHolder
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*


/**
 * Created by abakarmagomedov on 02/08/2018 at project InterGroupApplication.
 */
class GroupListAdapter(private val imageLoadingDelegate: ImageLoadingDelegate)
    : PagingDataAdapter<GroupEntity, RecyclerView.ViewHolder>(diffUtil) {

    companion object {
        var lettersToSpan = ""
        var userID: String? = null
        var groupClickListener: (groupId: String) -> Unit = {}
        var unsubscribeClickListener: (item: GroupEntity.Group, position: Int) -> Unit = { _, _ -> }
        var subscribeClickListener: (item: GroupEntity.Group, position: Int) -> Unit = {_, _ -> }
        private const val DEFAULT_HOLDER = 1488
        private val diffUtil = object : DiffUtil.ItemCallback<GroupEntity>() {
            override fun areItemsTheSame(oldItem: GroupEntity, newItem: GroupEntity): Boolean {
                return if (oldItem is GroupEntity.Group && newItem is GroupEntity.Group) {
                    oldItem.id == newItem.id
                } else if (oldItem is GroupEntity.AdEntity && newItem is GroupEntity.AdEntity) {
                    oldItem.position == newItem.position
                } else {
                    false
                }
            }
            override fun areContentsTheSame(oldItem: GroupEntity, newItem: GroupEntity): Boolean {
                return if (oldItem is GroupEntity.Group && newItem is GroupEntity.Group) {
                    oldItem == newItem
                } else if (oldItem is GroupEntity.AdEntity && newItem is GroupEntity.AdEntity) {
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return when (viewType) {
            AdViewHolder.NATIVE_AD -> {
                view = parent.inflate(R.layout.layout_admob_news)
                AdViewHolder(view)
            }
            else -> {
                GroupViewHolder(parent.inflate(R.layout.item_group_in_list))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is GroupViewHolder && item is GroupEntity.Group) {
            holder.bind(item)
        } else if (holder is AdViewHolder && item is GroupEntity.AdEntity) {
            item.nativeAd?.let {
                holder.bind(it, AD_TYPE, item.placement)
            } ?: let {
                val ad = Appodeal.getNativeAds(1)
                if (ad.size > 0) {
                    item.nativeAd = ad[0]
                }
                holder.bind(item.nativeAd, AD_TYPE, item.placement)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is GroupEntity.Group -> DEFAULT_HOLDER
            is GroupEntity.AdEntity -> AdViewHolder.NATIVE_AD
            null -> throw IllegalStateException("Unknown view")
        }
    }


    inner class GroupViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        private val viewBinding by viewBinding(ItemGroupInListBinding::bind)

        val avatar: SimpleDraweeView = itemView.findViewById(R.id.groupAvatarHolder)

        fun bind(item: GroupEntity.Group) {
            with(viewBinding) {
                spanLetters(item)
                //item_group__subscribers.text = context.getGroupFollowersCount(item.followersCount.toInt())
                itemGroupSubscribers.text = item.followersCount
                itemGroupPosts.text = item.postsCount
                itemGroupComments.text = item.CommentsCount
                itemGroupLike.text = item.postsLikes
                itemGroupDislike.text = item.postsDislikes
                itemGroupTextAge.text = item.ageRestriction
                when(item.ageRestriction) {
                    "12+" -> {
                        itemGroupTextAge.setBackgroundResource(R.drawable.bg_age12)
                        itemGroupTextAge.setTextColor(ContextCompat.getColor(itemView.context, R.color.mainBlack))
                    }
                    "16+" -> {
                        itemGroupTextAge.setBackgroundResource(R.drawable.bg_age16)
                        itemGroupTextAge.setTextColor(ContextCompat.getColor(itemView.context, R.color.mainBlack))
                    }
                    "18+" -> {
                        itemGroupTextAge.setBackgroundResource(R.drawable.bg_age18)
                        itemGroupTextAge.setTextColor(ContextCompat.getColor(itemView.context, R.color.whiteTextColor))
                    }
                    else -> {
                        itemGroupTextAge.setBackgroundResource(R.drawable.bg_age12)
                        itemGroupTextAge.setTextColor(ContextCompat.getColor(itemView.context, R.color.mainBlack))
                    }
                }
                if (item.isClosed) {
                    itemGroupLock.setImageResource(R.drawable.icon_close)
                    itemGroupLock.setBackgroundResource(R.drawable.bg_lock)
                } else {
                    itemGroupLock.setImageResource(R.drawable.icon_open)
                    itemGroupLock.setBackgroundResource(R.drawable.bg_unlock)
                }
                groupAvatarHolder.setOnClickListener {
                    groupClickListener.invoke(item.id)
                }
                if (item.isSubscribing) {
                    subscribingProgressBar.show()
                    itemGroupTextSub.hide()
                } else {
                    subscribingProgressBar.hide()
                    itemGroupTextSub.show()
                }
                with (itemGroupTextSub) {
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

        private fun spanLetters(item: GroupEntity.Group) {
                val spanStartPositions = item.name.mapIndexed { index: Int, c: Char -> item.name.indexOf(lettersToSpan, index, true) }.filterNot { it == -1 }.toSet()
                val wordToSpan: Spannable = SpannableString(item.name)
                spanStartPositions.forEach{
                    wordToSpan.setSpan(ForegroundColorSpan(Color.CYAN), it, it + lettersToSpan.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    wordToSpan.setSpan(StyleSpan(Typeface.BOLD), it, it + lettersToSpan.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                viewBinding.itemGroupListHeader.text = wordToSpan
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is GroupViewHolder) {
            holder.avatar.controller = null
        } else if (holder is AdViewHolder)
            holder.clear()
        super.onViewRecycled(holder)
    }
}
