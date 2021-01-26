package com.intergroupapplication.presentation.feature.grouplist.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Typeface
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.paging.PagedListAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.facebook.common.util.UriUtil
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.domain.entity.GroupInfoEntity
import com.intergroupapplication.presentation.base.adapter.PagingAdapter
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.news.adapter.NewsAdapter3
import kotlinx.android.synthetic.main.item_group_in_list.view.*
import kotlinx.android.synthetic.main.item_loading.view.*
import kotlinx.android.synthetic.main.post_item_error.view.*
import kotlinx.android.synthetic.main.post_item_error.view.buttonRetry


/**
 * Created by abakarmagomedov on 02/08/2018 at project InterGroupApplication.
 */
class GroupListAdapter3(diffCallback: DiffUtil.ItemCallback<GroupEntity>,
                        private val imageLoadingDelegate: ImageLoadingDelegate,
                       )
    : PagingDataAdapter<GroupEntity, RecyclerView.ViewHolder>(diffCallback), PagingAdapter {

    companion object {
        var lettersToSpan = ""
        var userID: String? = null
        var groupClickListener: (groupId: String) -> Unit = {}
        var unsubscribeClickListener: (groupId: String, view: View) -> Unit = {_, _ -> }
        var subscribeClickListener: (groupId: String, view: View) -> Unit = {_, _ -> }
    }

    private lateinit var context: Context
    private val loadingViewType = 123
    private val errorViewType = 321
    private var isLoading = false
    private var isError = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        return when (viewType) {
            loadingViewType -> ErrorLoadingViewHolder(parent.inflate(R.layout.item_loading))
            errorViewType -> ErrorLoadingViewHolder(parent.inflate(R.layout.item_loading))
            else -> GroupViewHolder(parent.inflate(R.layout.item_group_in_list))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            loadingViewType -> (holder as ErrorLoadingViewHolder).bindLoading()
            errorViewType -> (holder as ErrorLoadingViewHolder).bindError()
            else -> getItem(position)?.let { (holder as GroupViewHolder).bind(it) }
        }
    }


    inner class GroupViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: GroupEntity) {
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
                with (item_group__text_sub) {
                    if (item.isFollowing) {
                        setOnClickListener {
                            unsubscribeClickListener.invoke(item.id, view)
                        }
                        text = resources.getText(R.string.unsubscribe)
                        setBackgroundResource(R.drawable.btn_unsub)
                    } else {
                        setOnClickListener {
                            subscribeClickListener.invoke(item.id, view)
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
                    imageLoadingDelegate.loadImageFromUrl(it, groupAvatarHolder)
                }, { imageLoadingDelegate.loadImageFromResources(R.drawable.variant_10, groupAvatarHolder)
                })
            }
        }

        private fun spanLetters(item: GroupEntity) {
                val spanStartPositions = item.name.mapIndexed { index: Int, c: Char -> item.name.indexOf(lettersToSpan, index, true) }.filterNot { it == -1 }.toSet()
                val wordToSpan: Spannable = SpannableString(item.name)
                spanStartPositions.forEach{
                    wordToSpan.setSpan(ForegroundColorSpan(Color.CYAN), it, it + lettersToSpan.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    wordToSpan.setSpan(StyleSpan(Typeface.BOLD), it, it + lettersToSpan.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                itemView.item_group__list_header.text = wordToSpan
        }
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

    inner class ErrorLoadingViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bindError() {
            itemView.error_layout.show()
            itemView.loading_layout.gone()
            itemView.buttonRetry.setOnClickListener { this@GroupListAdapter3.retry() }
        }
        fun bindLoading() {
            itemView.error_layout.gone()
            itemView.loading_layout.show()
        }
    }

}
