package com.intergroupapplication.presentation.feature.commentsdetails.adapter

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.presentation.base.adapter.PagingAdapter
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.doOrIfNull
import com.intergroupapplication.presentation.exstension.getDateDescribeByString
import com.intergroupapplication.presentation.exstension.inflate
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.item_comment.view.*
import kotlinx.android.synthetic.main.post_item_error.view.*
import timber.log.Timber

/**
 * Created by abakarmagomedov on 28/08/2018 at project InterGroupApplication.
 */
class CommentDetailsAdapter(diffCallback: DiffUtil.ItemCallback<CommentEntity>, private val imageLoadingDelegate: ImageLoadingDelegate)
    : PagedListAdapter<CommentEntity, RecyclerView.ViewHolder>(diffCallback), PagingAdapter {

    companion object {
        const val loadingViewType = 123
        const val errorViewType = 321
    }

    var replyListener: (commentEntity: CommentEntity) -> Unit = {}
    var retryClickListener: () -> Unit = {}
    var complaintListener: (Int) -> Unit = {}
    private var isLoading = false
    private var isError = false
    private var compositeDisposable = CompositeDisposable()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            loadingViewType -> LoadingViewHolder(parent.inflate(R.layout.post_item_loading))
            errorViewType -> ErrorViewHolder(parent.inflate(R.layout.post_item_error))
            else -> CommentViewHolder(parent.inflate(R.layout.item_comment))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            loadingViewType -> {
            }
            errorViewType -> (holder as ErrorViewHolder).bind()
            else -> getItem(position)?.let { (holder as CommentViewHolder).bind(it) }
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
        notifyItemInserted(itemCount - 1)
    }

    override fun removeLoading() {
        if (!isLoading) {
            return
        }
        isLoading = false
        notifyItemInserted(itemCount - 1)
    }

    override fun addError() {
        if (isError) {
            return
        }
        isError = true
        notifyItemInserted(itemCount - 1)
    }

    override fun removeError() {
        if (!isError) {
            return
        }
        isError = false
        notifyItemInserted(itemCount - 1)
    }

    inner class CommentViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: CommentEntity) {
            with(itemView) {
                val name = item.commentOwner
                        ?.let { "${it.firstName} ${it.secondName}" }
                        ?: let { itemView.resources.getString(R.string.unknown_user) }
                commentUserName.text = name
                commentUserId.text = context.getString(R.string.id,
                        item.commentOwner?.user ?: "нет id")
                commentText.text = item.text
                compositeDisposable.add(getDateDescribeByString(item.date)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            commentPrescription.text = it
                        }, {
                            Timber.e(it)
                        }))
                commentAvatar.run {
                    doOrIfNull(item.commentOwner?.avatar, { imageLoadingDelegate.loadImageFromUrl(it, this) },
                            { imageLoadingDelegate.loadImageFromResources(R.drawable.application_logo, this) })
                }
                reply.setOnClickListener {
                    replyListener.invoke(item)
                }
                doOrIfNull(item.answerTo, {
                    responseToOtherComment.text = context.getString(R.string.response_to,
                            getUserNameByCommentId(it))
                }, { responseToOtherComment.text = "" })
                settingsComment.setOnClickListener { showPopupMenu(it, Integer.parseInt(item.id)) }

            }
        }

        private fun getUserNameByCommentId(commentId: String): String {
            val unknown = view.resources.getString(R.string.unknown)
            currentList?.let {
                for (comment in it) {
                    if (comment.id == commentId) {
                        return comment.commentOwner?.firstName ?: unknown
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

    inner class LoadingViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    inner class ErrorViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
            itemView.buttonRetry.setOnClickListener { retryClickListener.invoke() }
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