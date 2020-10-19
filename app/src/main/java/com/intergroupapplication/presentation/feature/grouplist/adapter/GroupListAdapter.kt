package com.intergroupapplication.presentation.feature.grouplist.adapter

import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.TypefaceSpan
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.GroupEntity
import com.intergroupapplication.presentation.base.adapter.PagingAdapter
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.doOrIfNull
import com.intergroupapplication.presentation.exstension.getGroupFollowersCount
import com.intergroupapplication.presentation.exstension.inflate
import kotlinx.android.synthetic.main.itemgroupinlist.view.*
import kotlinx.android.synthetic.main.post_item_error.view.*
import java.time.format.TextStyle


/**
 * Created by abakarmagomedov on 02/08/2018 at project InterGroupApplication.
 */
class GroupListAdapter(diffCallback: DiffUtil.ItemCallback<GroupEntity>,
                       private val imageLoadingDelegate: ImageLoadingDelegate,
                       private val userID: String?)
    : PagedListAdapter<GroupEntity, RecyclerView.ViewHolder>(diffCallback), PagingAdapter {

    companion object {
        var lettersToSpan = ""
    }

    var groupClickListener: (groupId: String) -> Unit = {}
    var retryClickListener: () -> Unit = {}
    var subscribeClickListener: (groupId: String) -> Unit = {}
    var unsubscribeClickListener: (groupId: String) -> Unit = {}

    private val loadingViewType = 123
    private val errorViewType = 321
    private var isLoading = false
    private var isError = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            loadingViewType -> LoadingViewHolder(parent.inflate(R.layout.post_item_loading))
            errorViewType -> ErrorViewHolder(parent.inflate(R.layout.post_item_error))
            else -> GroupViewHolder(parent.inflate(R.layout.itemgroupinlist))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            loadingViewType -> {
            }
            errorViewType -> (holder as ErrorViewHolder).bind()
            else -> getItem(position)?.let { (holder as GroupViewHolder).bind(it) }
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

    inner class GroupViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: GroupEntity) {
            with(itemView) {
                spanLetters(item)
                item_group__subscribers.text = context.getGroupFollowersCount(item.followersCount.toInt())
                item_group__posts.text = item.postsCount
                item_group__comments.text = item.CommentsCount
                item_group__dislike.text = item.postsLikes
                item_group__like.text = item.postsDislikes
                groupAvatarHolder.setOnClickListener {
                    groupClickListener.invoke(item.id)
                }
                if (item.isFollowing) {
                    item_group__btn_group_list.setOnClickListener {
                        unsubscribeClickListener.invoke(item.id)
                    }
                    item_group__text_sub.text = resources.getText(R.string.unsubscribe)
                    item_group__btn_group_list.setBackgroundResource(R.drawable.btn_unsub)
                } else {
                    item_group__btn_group_list.setOnClickListener {
                        subscribeClickListener.invoke(item.id)
                    }
                    item_group__text_sub.text = resources.getText(R.string.subscribe)
                    item_group__btn_group_list.setBackgroundResource(R.drawable.btn_sub)
                }
                if (userID == item.owner) {
                    item_group__btn_group_list.visibility = View.GONE
                    item_group__text_sub.visibility = View.GONE
                } else {
                    item_group__btn_group_list.visibility = View.VISIBLE
                    item_group__text_sub.visibility = View.VISIBLE
                }
                doOrIfNull(item.avatar, {
                    imageLoadingDelegate.loadImageFromUrl(it, groupAvatarHolder)
                }, { imageLoadingDelegate.loadImageFromResources(R.drawable.application_logo, groupAvatarHolder) })
            }
        }

        private fun spanLetters(item: GroupEntity) {
                val spanStartPositions = item.name.mapIndexed { index: Int, c: Char -> item.name.indexOf(lettersToSpan, index, true) }.filterNot { it == -1 }.toSet()
                val wordToSpan: Spannable = SpannableString(item.name)
                spanStartPositions.forEach{
                    wordToSpan.setSpan(ForegroundColorSpan(Color.CYAN), it, it+lettersToSpan.length , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    wordToSpan.setSpan(StyleSpan(Typeface.BOLD), it, it+lettersToSpan.length , Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                itemView.item_group__list_header.text = wordToSpan
        }
    }

    inner class LoadingViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    inner class ErrorViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
            itemView.buttonRetry.setOnClickListener { retryClickListener.invoke() }
        }
    }

}
