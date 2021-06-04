package com.intergroupapplication.presentation.feature.news.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.appodeal.ads.*
import com.danikula.videocache.HttpProxyCacheServer
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.intergroupapplication.R
import com.intergroupapplication.data.model.MarkupModel
import com.intergroupapplication.databinding.ItemGroupPostBinding
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.entity.NewsEntity
import com.intergroupapplication.presentation.base.AdViewHolder
import com.intergroupapplication.presentation.base.AdViewHolder.Companion.NATIVE_AD
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import com.omega_r.libs.omegaintentbuilder.OmegaIntentBuilder
import com.omega_r.libs.omegaintentbuilder.downloader.DownloadCallback
import com.omega_r.libs.omegaintentbuilder.handlers.ContextIntentHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber


class NewsAdapter(private val imageLoadingDelegate: ImageLoadingDelegate,
                  private val cacheServer: HttpProxyCacheServer)
    : PagingDataAdapter<NewsEntity, RecyclerView.ViewHolder>(diffUtil) {

    companion object {
        private const val DEFAULT_HOLDER = 1488
        private val diffUtil = object : DiffUtil.ItemCallback<NewsEntity>() {
            override fun areItemsTheSame(oldItem: NewsEntity, newItem: NewsEntity): Boolean {
                return if (oldItem is NewsEntity.Post && newItem is NewsEntity.Post) {
                    oldItem.id == newItem.id
                } else if (oldItem is NewsEntity.AdEntity && newItem is NewsEntity.AdEntity) {
                    oldItem.position == newItem.position
                } else {
                    false
                }
            }
            override fun areContentsTheSame(oldItem: NewsEntity, newItem: NewsEntity): Boolean {
                return if (oldItem is NewsEntity.Post && newItem is NewsEntity.Post) {
                    oldItem == newItem
                } else if (oldItem is NewsEntity.AdEntity && newItem is NewsEntity.AdEntity) {
                    oldItem == newItem
                } else {
                    false
                }
            }
        }
        var AD_TYPE = 1
        var AD_FREQ = 3
        var AD_FIRST = 3
        var commentClickListener: (groupPostEntity: GroupPostEntity.PostEntity) -> Unit = {}
        var groupClickListener: (groupId: String) -> Unit = {}
        var complaintListener: (Int) -> Unit = {}
        var imageClickListener: (List<FileEntity>, Int) -> Unit = { _, _ -> }
        var likeClickListener: (isLike: Boolean, isDislike: Boolean, item: GroupPostEntity.PostEntity, position: Int) -> Unit = { _, _, _, _ -> }
        var deleteClickListener: (postId: Int, position: Int) -> Unit = { _, _ ->}
        var bellClickListener: (item: GroupPostEntity.PostEntity, position: Int) -> Unit = { _, _ ->}
        var progressBarVisibility:(visibility:Boolean) -> Unit = {_->}
        var USER_ID: Int? = null
    }

    private var compositeDisposable = CompositeDisposable()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return when (viewType) {
            NATIVE_AD -> {
                view = parent.inflate(R.layout.layout_admob_news)
                AdViewHolder(view)
            }
            else -> {
                PostViewHolder(parent.inflate(R.layout.item_group_post))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let {
            if (holder is PostViewHolder && it is NewsEntity.Post)
                holder.bind(it)
            else if (holder is AdViewHolder && it is NewsEntity.AdEntity) {
                holder.bind(it.nativeAd, AD_TYPE, "news_feed")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is NewsEntity.Post -> DEFAULT_HOLDER
            is NewsEntity.AdEntity -> NATIVE_AD
            null -> throw IllegalStateException("Unknown view")
        }
    }

    inner class PostViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val viewBinding by viewBinding(ItemGroupPostBinding::bind)

        fun bind(item: NewsEntity.Post) {
            with(viewBinding) {
                idpGroupPost.text = itemView.context.getString(R.string.idp, item.post.idp.toString())
                postLike.text = item.post.reacts.likesCount.toString()
                if (item.post.reacts.isLike) {
                    postLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_like_active, 0, 0, 0)
                } else {
                    postLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_like, 0, 0, 0)
                }
                postDislike.text = item.post.reacts.dislikesCount.toString()
                if (item.post.reacts.isDislike) {
                    postDislike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_dislike_active, 0, 0, 0)
                } else {
                    postDislike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_dislike, 0, 0, 0)
                }
                compositeDisposable.add(getDateDescribeByString(item.post.date)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ postPrescription.text = it }, { Timber.e(it) }))
                countComments.text = itemView.context.getString(R.string.comments_count, item.post.commentsCount, item.post.unreadComments)

                postCustomView.proxy = cacheServer
                postCustomView.imageClickListener = imageClickListener
                postCustomView.imageLoadingDelegate = imageLoadingDelegate
                postCustomView.setUpPost(mapToGroupEntityPost(item.post))

                groupName.text = item.post.groupInPost.name
                subCommentBtn.text = item.post.bells.count.toString()
                if (item.post.bells.isActive) {
                    subCommentBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_sub_comnts_blue, 0, 0, 0)
                } else {
                    subCommentBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_sub_comnts_grey, 0, 0, 0)
                }

                subCommentBtn.setOnClickListener {
                    bellClickListener.invoke(item.post, layoutPosition)
                }
                countComments.setOnClickListener {
                    commentClickListener.invoke(item.post)
                }
                postAvatarHolder.setOnClickListener {
                    groupClickListener.invoke(item.post.groupInPost.id)
                }
                headerPostFromGroup.setOnClickListener {
                    groupClickListener.invoke(item.post.groupInPost.id)
                }
                postLikesClickArea.setOnClickListener {
                    likeClickListener.invoke(!item.post.reacts.isLike, item.post.reacts.isDislike, item.post, layoutPosition)
                }
                postDislikesClickArea.setOnClickListener {
                    likeClickListener.invoke(item.post.reacts.isLike, !item.post.reacts.isDislike, item.post, layoutPosition)
                }
                settingsPost.setOnClickListener { showPopupMenu(settingsPost, Integer.parseInt(item.post.id), item.id, item.post.author.id) }

                btnRepost.setOnClickListener {
                   sharePost(view.context,item)
                }

                doOrIfNull(item.post.groupInPost.avatar, {
                    imageLoadingDelegate.loadImageFromUrl(it, postAvatarHolder) },
                        { imageLoadingDelegate.loadImageFromResources(R.drawable.application_logo, postAvatarHolder) })

            }
        }

        private fun mapToGroupEntityPost(postEntity: GroupPostEntity.PostEntity) =
                MarkupModel(postEntity.postText,postEntity.images,postEntity.audios,postEntity.videos,
                        postEntity.imagesExpanded,postEntity.audiosExpanded,postEntity.videosExpanded)

        private fun sharePost(context: Context, item: NewsEntity.Post){
            progressBarVisibility.invoke(true)
            /*val link = Firebase.dynamicLinks.dynamicLink {
                domainUriPrefix = context.getString(R.string.deeplinkDomain)
                link =Uri.parse("https://intergroup.com/post/${item.post.id}")
                androidParameters(packageName = "com.intergroupapplication"){
                    minimumVersion = 1
                }
            }*/
            FirebaseDynamicLinks.getInstance().createDynamicLink()
                    //.setLongLink(link.uri)
                    .setDomainUriPrefix( context.getString(R.string.deeplinkDomain))
                    .setLink(Uri.parse("https://intergroup.com/post/${item.post.id}"))
                    .setAndroidParameters(DynamicLink.AndroidParameters.Builder("com.intergroupapplication")
                        .setMinimumVersion(1).build())
                    .buildShortDynamicLink(ShortDynamicLink.Suffix.SHORT)
                    .addOnCompleteListener {
                        createShareIntent(context,item,it.result.previewLink.toString())
                    }
        }

        private fun createShareIntent(context: Context, item: NewsEntity.Post, url:String){
            val text = url+"/${item.post.id}"
            val filesUrls = mutableListOf<String>()
            filesUrls.addAll(item.post.videos.map { it.file })
            filesUrls.addAll(item.post.images.map { it.file })
            OmegaIntentBuilder.from(context)
                    .share()
                    .text(text)
                    .filesUrls(filesUrls)
                    .download(object: DownloadCallback {
                        override fun onDownloaded(success: Boolean, contextIntentHandler: ContextIntentHandler) {
                            progressBarVisibility.invoke(false)
                            contextIntentHandler.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    .startActivity()
                        }
                    })
        }

        private fun showPopupMenu(view: View, postId: Int, newsId: Int, userId: Int) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.inflate(R.menu.settings_menu)
//            popupMenu.menu.findItem(R.id.delete).isVisible = USER_ID == userId
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.complaint -> complaintListener.invoke(postId)
                    R.id.delete -> deleteClickListener.invoke(newsId, layoutPosition)
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
        if (holder is PostViewHolder) {
           // holder.imageContainer.destroy()
        } else if (holder is AdViewHolder) {
            holder.clear()
        }
        super.onViewRecycled(holder)
    }

}