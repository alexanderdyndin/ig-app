package com.intergroupapplication.presentation.feature.news.adapter

import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedList
import com.github.marlonlom.utilities.timeago.onNotNull
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.presentation.base.adapter.PagingAdapter
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.news.pagingsource.NewsDataSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.item_group_post.view.*
import kotlinx.android.synthetic.main.post_item_error.view.*
import timber.log.Timber

class NewsAdapter(diffCallback: DiffUtil.ItemCallback<GroupPostEntity>,
                  private val imageLoadingDelegate: ImageLoadingDelegate)
    : PagedListAdapter<GroupPostEntity, RecyclerView.ViewHolder>(diffCallback), PagingAdapter {

    var commentClickListener: (groupPostEntity: GroupPostEntity) -> Unit = {}
    var retryClickListener: () -> Unit = {}
    var groupClickListener: (groupId: String) -> Unit = {}
    var complaintListener: (Int) -> Unit = {}
    var items: List<GroupPostEntity> = mutableListOf<GroupPostEntity>()
    private val loadingViewType = 123
    private val errorViewType = 321
    private var isLoading = false
    private var isError = false
    private var compositeDisposable = CompositeDisposable()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            loadingViewType -> LoadingViewHolder(parent.inflate(R.layout.post_item_loading))
            errorViewType -> ErrorViewHolder(parent.inflate(R.layout.post_item_error))
            else -> PostViewHolder(parent.inflate(R.layout.item_group_post))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            loadingViewType -> {
            }
            errorViewType -> (holder as ErrorViewHolder).bind()
            else -> getItem(position)?.let { (holder as PostViewHolder).bind(it) }
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

    fun itemUpdate(id: String, countComment: String) {
        currentList?.first { it.id == id }.let {
            it?.commentsCount = countComment
        }
        notifyDataSetChanged()
    }

    inner class PostViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: GroupPostEntity) {
            with(itemView) {
                compositeDisposable.add(getDateDescribeByString(item.date)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ postPrescription.text = it }, { Timber.e(it) }))
                //postPrescription.text = getDateDescribeByString(item.date)
                postCommentsCount.text = item.commentsCount
                item.postText.let { it ->
                    if (!it.isEmpty()) {
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

    inner class LoadingViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    inner class ErrorViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
            itemView.buttonRetry.setOnClickListener { retryClickListener.invoke() }
        }
    }

    override fun submitList(pagedList: PagedList<GroupPostEntity>?) {
        super.submitList(pagedList)
        if (pagedList?.loadedCount ?:0 > 0) {
            items = (pagedList?.dataSource as NewsDataSource).list
        }
    }

}