package com.intergroupapplication.presentation.feature.commentsdetails.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
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
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.presentation.base.AdViewHolder
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.doOrIfNull
import com.intergroupapplication.presentation.exstension.getDateDescribeByString
import com.intergroupapplication.presentation.exstension.inflate
import com.intergroupapplication.presentation.feature.news.adapter.NewsAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.item_comment.view.*
import kotlinx.android.synthetic.main.item_comment_answer.view.*
import kotlinx.android.synthetic.main.post_item_error.view.*
import timber.log.Timber

/**
 * Created by abakarmagomedov on 28/08/2018 at project InterGroupApplication.
 */
class CommentsAdapter(private val imageLoadingDelegate: ImageLoadingDelegate)
    : PagingDataAdapter<CommentEntity, RecyclerView.ViewHolder>(diffUtil) {

    companion object {
        private const val DEFAULT_HOLDER = 1488
        private const val ANSWER_HOLDER = 228
        private val diffUtil = object : DiffUtil.ItemCallback<CommentEntity>() {
            override fun areItemsTheSame(oldItem: CommentEntity, newItem: CommentEntity): Boolean {
                return if (oldItem is CommentEntity.Comment && newItem is CommentEntity.Comment) {
                    oldItem.id == newItem.id
                } else if (oldItem is CommentEntity.AdEntity && newItem is CommentEntity.AdEntity) {
                    oldItem.position == newItem.position
                } else {
                    false
                }
            }
            override fun areContentsTheSame(oldItem: CommentEntity, newItem: CommentEntity): Boolean {
                return if (oldItem is CommentEntity.Comment && newItem is CommentEntity.Comment) {
                    oldItem == newItem
                } else if (oldItem is CommentEntity.AdEntity && newItem is CommentEntity.AdEntity) {
                    oldItem == newItem
                } else {
                    false
                }
            }
        }
        var replyListener: (commentEntity: CommentEntity.Comment) -> Unit = {}
        var complaintListener: (Int) -> Unit = {}
        var likeClickListener: (isLike: Boolean, isDislike: Boolean, item: CommentEntity.Comment, position: Int) -> Unit = { _, _, _, _ -> }
        var deleteClickListener: (postId: Int, position: Int) -> Unit = { _, _ ->}
        var AD_TYPE = 1
        var AD_FREQ = 3
        var AD_FIRST = 3
        var USER_ID: Int? = null
    }


    private var compositeDisposable = CompositeDisposable()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return when (viewType) {
            AdViewHolder.NATIVE_AD -> {
                view = parent.inflate(R.layout.layout_admob_news)
                AdViewHolder(view)
            }
            DEFAULT_HOLDER -> {
                CommentViewHolder(parent.inflate(R.layout.item_comment))
            }
            else -> {
                CommentAnswerViewHolder(parent.inflate(R.layout.item_comment_answer))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let {
            when (holder) {
                is CommentViewHolder -> if (it is CommentEntity.Comment) holder.bind(it)
                is CommentAnswerViewHolder -> if (it is CommentEntity.Comment) holder.bind(it)
                is AdViewHolder -> if (it is CommentEntity.AdEntity) holder.bind(it.nativeAd, AD_TYPE, "comments")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = getItem(position)) {
            is CommentEntity.Comment -> {
                if (item.answerTo == null)
                    DEFAULT_HOLDER
                else
                    ANSWER_HOLDER
            }
            is CommentEntity.AdEntity -> AdViewHolder.NATIVE_AD
            null -> throw IllegalStateException("Unknown view")
        }
    }


    inner class CommentViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: CommentEntity.Comment) {
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
                postDislike.text = item.reacts.dislikesCount.toString()
                postLike.text = item.reacts.likesCount.toString()
                userAvatarHolder.run {
                    doOrIfNull(item.commentOwner?.avatar, { imageLoadingDelegate.loadImageFromUrl(it, this) },
                            { imageLoadingDelegate.loadImageFromResources(R.drawable.application_logo, this) })
                }
                replyButton.setOnClickListener {
                    replyListener.invoke(item)
                }
                postDislikes.setOnClickListener {
                    likeClickListener.invoke(item.reacts.isLike, !item.reacts.isDislike, item, layoutPosition)
                }
                postLikes.setOnClickListener {
                    likeClickListener.invoke(!item.reacts.isLike, item.reacts.isDislike, item, layoutPosition)
                }
                idcGroupUser.text = itemView.context.getString(R.string.idc, item.idc.toString())
                settingsBtn.setOnClickListener { showPopupMenu(it, Integer.parseInt(item.id), item.commentOwner?.user) }

            }
        }

        private fun showPopupMenu(view: View, id: Int, userId: Int?) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.inflate(R.menu.settings_menu)
            popupMenu.menu.findItem(R.id.delete).isVisible = userId == USER_ID
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

    inner class CommentAnswerViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: CommentEntity.Comment) {
            with(itemView) {
                idcGroupUser2.text = itemView.context.getString(R.string.idc, item.idc.toString())
                compositeDisposable.add(getDateDescribeByString(item.date)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            timeComment2.text = it
                        }, {
                            Timber.e(it)
                        }))
                userName2.text = item.commentOwner
                        ?.let { "${it.firstName} ${it.secondName}" }
                        ?: let { itemView.resources.getString(R.string.unknown_user) }
                idUser2.text = context.getString(R.string.id,
                        item.commentOwner?.user ?: "нет id")
                val replyName =  item.answerTo?.commentOwner
                        ?.let { "${it.firstName} ${it.secondName}, " }
                        ?: let { itemView.resources.getString(R.string.unknown_user) }
                nameUserReply.text = replyName
                postText2.text = item.text
                endReply.text = itemView.context.getString(R.string.reply_to, item.answerTo?.idc.toString())
                postDislike2.text = item.reacts.dislikesCount.toString()
                postLike2.text = item.reacts.likesCount.toString()
                userAvatarHolder2.run {
                    doOrIfNull(item.commentOwner?.avatar, { imageLoadingDelegate.loadImageFromUrl(it, this) },
                            { imageLoadingDelegate.loadImageFromResources(R.drawable.application_logo, this) })
                }
                userReplyAvatarHolder.run {
                    doOrIfNull(item.answerTo?.commentOwner?.avatar, { imageLoadingDelegate.loadImageFromUrl(it, this) },
                            { imageLoadingDelegate.loadImageFromResources(R.drawable.application_logo, this) })
                }
                replyAnswerButton.setOnClickListener {
                    replyListener.invoke(item)
                }
                postDislikes2.setOnClickListener {
                    likeClickListener.invoke(item.reacts.isLike, !item.reacts.isDislike, item, layoutPosition)
                }
                postLikes2.setOnClickListener {
                    likeClickListener.invoke(!item.reacts.isLike, item.reacts.isDislike, item, layoutPosition)
                }
                settingsBtn2.setOnClickListener { showPopupMenu(it, Integer.parseInt(item.id), item.commentOwner?.user) }

            }
        }

        private fun showPopupMenu(view: View, id: Int, userId: Int?) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.inflate(R.menu.settings_menu)
//            popupMenu.menu.findItem(R.id.delete).isVisible = userId == USER_ID
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

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is AdViewHolder) {
            holder.clear()
        }
        super.onViewRecycled(holder)
    }

}