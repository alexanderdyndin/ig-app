package com.intergroupapplication.presentation.feature.news.adapter

import android.content.Context
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.forEach
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.presentation.base.adapter.PagingAdapter
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.item_group_post.view.*
import kotlinx.android.synthetic.main.item_loading.view.*
import timber.log.Timber

class NewsAdapter3(diffCallback: DiffUtil.ItemCallback<GroupPostEntity>,
                   private val imageLoadingDelegate: ImageLoadingDelegate)
    : PagingDataAdapter<GroupPostEntity, RecyclerView.ViewHolder>(diffCallback), PagingAdapter {

    var commentClickListener: (groupPostEntity: GroupPostEntity) -> Unit = {}
    var groupClickListener: (groupId: String) -> Unit = {}
    var complaintListener: (Int) -> Unit = {}
    var imageClickListener: (List<FileEntity>, Int) -> Unit = { list: List<FileEntity>, i: Int -> }

    private lateinit var context: Context
    private var compositeDisposable = CompositeDisposable()
    private val loadingViewType = 123
    private val errorViewType = 321
    private var isLoading = false
    private var isError = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        context = parent.context
        return when (viewType) {
            loadingViewType -> ErrorLoadingViewHolder(parent.inflate(R.layout.item_loading))
            errorViewType -> ErrorLoadingViewHolder(parent.inflate(R.layout.item_loading))
            else -> PostViewHolder(parent.inflate(R.layout.item_group_post))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            loadingViewType -> (holder as NewsAdapter3.ErrorLoadingViewHolder).bindLoading()
            errorViewType -> (holder as NewsAdapter3.ErrorLoadingViewHolder).bindError()
            else -> getItem(position)?.let { (holder as NewsAdapter3.PostViewHolder).bind(it) }
        }
    }


    inner class PostViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: GroupPostEntity) {
            with(itemView) {
                compositeDisposable.add(getDateDescribeByString(item.date)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ postPrescription.text = it }, { Timber.e(it) }))
                postCommentsCount.text = item.commentsCount
                item.postText.let { it ->
                    if (it.isNotEmpty()) {
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
                mediaBody.removeAllViews()
                imageContainer.removeAllViews()
                item.images.forEach { file ->
                    val image = SimpleDraweeView(itemView.context)
                    image.layoutParams = ViewGroup.LayoutParams(400, 400)
                    //image.scaleType = ImageView.ScaleType.CENTER_CROP
                    image.setOnClickListener { imageClickListener.invoke(item.images, item.images.indexOf(file)) }
                    if (file.file.contains(".gif")) {
                        val controller = Fresco.newDraweeControllerBuilder()
                                .setUri(Uri.parse(file.file))
                                .setAutoPlayAnimations(true)
                                .build()
                        image.controller = controller
                    } else {
                        imageLoadingDelegate.loadImageFromUrl(file.file, image)
                    }
                    imageContainer.addView(image)
                }
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
            itemView.buttonRetry.setOnClickListener { this@NewsAdapter3.retry() }
        }
        fun bindLoading() {
            itemView.error_layout.gone()
            itemView.loading_layout.show()
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is PostViewHolder) {

        }
    }


}