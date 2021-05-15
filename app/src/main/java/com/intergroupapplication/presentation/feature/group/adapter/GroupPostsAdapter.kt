package com.intergroupapplication.presentation.feature.group.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.appodeal.ads.*
import com.appodeal.ads.native_ad.views.NativeAdViewAppWall
import com.appodeal.ads.native_ad.views.NativeAdViewContentStream
import com.appodeal.ads.native_ad.views.NativeAdViewNewsFeed
import com.danikula.videocache.HttpProxyCacheServer
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLink
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.presentation.base.AdViewHolder
import com.intergroupapplication.presentation.customview.AudioGalleryView
import com.intergroupapplication.presentation.customview.ImageGalleryView
import com.intergroupapplication.presentation.customview.VideoGalleryView
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.item_group_post.view.*
import kotlinx.android.synthetic.main.item_loading.view.*
import timber.log.Timber

class GroupPostsAdapter(private val imageLoadingDelegate: ImageLoadingDelegate,
                        private val proxyCacheServer: HttpProxyCacheServer)
    : PagingDataAdapter<GroupPostEntity, RecyclerView.ViewHolder>(diffUtil) {

    var isAdmin = false
    companion object {
        private const val DEFAULT_HOLDER = 1488
        private val diffUtil = object : DiffUtil.ItemCallback<GroupPostEntity>() {
            override fun areItemsTheSame(oldItem: GroupPostEntity, newItem: GroupPostEntity): Boolean {
                return if (oldItem is GroupPostEntity.PostEntity && newItem is GroupPostEntity.PostEntity) {
                    oldItem.id == newItem.id
                } else if (oldItem is GroupPostEntity.AdEntity && newItem is GroupPostEntity.AdEntity) {
                    oldItem.position == newItem.position
                } else {
                    false
                }
            }
            override fun areContentsTheSame(oldItem: GroupPostEntity, newItem: GroupPostEntity): Boolean {
                return if (oldItem is GroupPostEntity.PostEntity && newItem is GroupPostEntity.PostEntity) {
                    oldItem == newItem
                } else if (oldItem is GroupPostEntity.AdEntity && newItem is GroupPostEntity.AdEntity) {
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
        var complaintListener: (Int) -> Unit = {}
        var imageClickListener: (List<FileEntity>, Int) -> Unit = { list: List<FileEntity>, i: Int -> }
        var likeClickListener: (isLike: Boolean, isDislike: Boolean, item: GroupPostEntity.PostEntity, position: Int) -> Unit = { _, _, _, _ -> }
        var deleteClickListener: (postId: Int, position: Int) -> Unit = { _, _ ->}
        var editPostClickListener: (postEntity:GroupPostEntity.PostEntity) -> Unit = {}
        var bellClickListener: (item: GroupPostEntity.PostEntity, position: Int) -> Unit = { _, _ ->}
        var pinClickListener: (item: GroupPostEntity.PostEntity, position: Int) -> Unit = { _, _ ->}
        var isOwner = false
    }

    private var compositeDisposable = CompositeDisposable()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return when (viewType) {
            AdViewHolder.NATIVE_AD -> {
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
            if (holder is PostViewHolder && it is GroupPostEntity.PostEntity)
                holder.bind(it)
            else if (holder is AdViewHolder && it is GroupPostEntity.AdEntity) {
                holder.bind(it.nativeAd, AD_TYPE, "group_feed")
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is PostViewHolder) {
            holder.imageContainer.destroy()
        } else if (holder is AdViewHolder) {
            holder.clear()
        }
        super.onViewRecycled(holder)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is GroupPostEntity.PostEntity -> DEFAULT_HOLDER
            is GroupPostEntity.AdEntity -> AdViewHolder.NATIVE_AD
            null -> throw IllegalStateException("Unknown view")
        }
    }

    inner class PostViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val audioContainer = itemView.findViewById<AudioGalleryView>(R.id.audioBody)
        val videoContainer = itemView.findViewById<VideoGalleryView>(R.id.videoBody)
        val imageContainer = itemView.findViewById<ImageGalleryView>(R.id.imageBody)

        fun bind(item: GroupPostEntity.PostEntity) {
            with(itemView) {
                idpGroupPost.text = context.getString(R.string.idp, item.idp.toString())
                postLike.text = item.reacts.likesCount.toString()
                postDislike.text = item.reacts.dislikesCount.toString()
                if (item.reacts.isLike) {
                    postLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_like_active, 0, 0, 0)
                } else {
                    postLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_like, 0, 0, 0)
                }
                if (item.reacts.isDislike) {
                    postDislike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_dislike_active, 0, 0, 0)
                } else {
                    postDislike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_dislike, 0, 0, 0)
                }
                compositeDisposable.add(getDateDescribeByString(item.date)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ postPrescription.text = it }, { Timber.e(it) }))
                countComments.text = context.getString(R.string.comments_count, item.commentsCount, item.unreadComments)
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
                subCommentBtn.text = item.bells.count.toString()
                if (item.bells.isActive) {
                    subCommentBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_sub_comnts_blue, 0, 0, 0)
                } else {
                    subCommentBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_sub_comnts_grey, 0, 0, 0)
                }

                anchorBtn.setOnClickListener { pinClickListener.invoke(item, layoutPosition) }

//                if (isOwner) {
//                    anchorBtn.show()
//                    if (item.isPinned) {
//                        anchorBtn.setBackgroundResource(R.drawable.btn_anchor_act)
//                        anchorBtn.setImageResource(R.drawable.ic_anchor_act)
//                    } else {
//                        anchorBtn.setBackgroundResource(R.drawable.btn_anchor)
//                        anchorBtn.setImageResource(R.drawable.ic_anchor)
//                    }
//                } else {
//                    anchorBtn.gone()
//                }

                subCommentBtn.setOnClickListener {
                    bellClickListener.invoke(item, layoutPosition)
                }
                countComments.setOnClickListener {
                    commentClickListener.invoke(item)
                }
                postLikesClickArea.setOnClickListener {
                    likeClickListener.invoke(!item.reacts.isLike, item.reacts.isDislike, item, layoutPosition)
                }
                postDislikesClickArea.setOnClickListener {
                    likeClickListener.invoke(item.reacts.isLike, !item.reacts.isDislike, item, layoutPosition)
                }
                settingsPost.setOnClickListener {
                        showPopupMenu(settingsPost, Integer.parseInt(item.id),item) }

                doOrIfNull(item.groupInPost.avatar, {
                    imageLoadingDelegate.loadImageFromUrl(it, postAvatarHolder) },
                        { imageLoadingDelegate.loadImageFromResources(R.drawable.variant_10, postAvatarHolder) })

                btnRepost.setOnClickListener {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.type = "text/plain"
                    val link = Firebase.dynamicLinks.dynamicLink {
                        domainUriPrefix = "https://intergroupapplication.page.link"
                        link = Uri.parse("https://intergroup.com/post/${item.id}")
                        androidParameters(packageName = "com.intergroupapplication"){
                            minimumVersion = 1
                        }
                    }
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Firebase Deep Link")
                    intent.putExtra(Intent.EXTRA_TEXT,link.uri.toString())
                    context.startActivity(Intent.createChooser(intent,"Share using"))
                }

                videoContainer.proxy = proxyCacheServer
                videoContainer.imageLoadingDelegate = imageLoadingDelegate
                videoContainer.setVideos(item.videos, item.videosExpanded)
                videoContainer.expand = { item.videosExpanded = it }

                audioContainer.proxy = proxyCacheServer
                audioContainer.setAudios(item.audios, item.audiosExpanded)
                audioContainer.expand = { item.audiosExpanded = it }

                imageContainer.setImages(item.images, item.imagesExpanded)
                imageContainer.imageClick = imageClickListener
                imageContainer.expand = { item.imagesExpanded = it }
            }
        }

        private fun showPopupMenu(view: View, id: Int,groupPostEntity: GroupPostEntity.PostEntity) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.inflate(R.menu.settings_menu)
            popupMenu.menu.findItem(R.id.edit).isVisible = isAdmin
//            popupMenu.menu.findItem(R.id.delete).isVisible = isOwner
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.complaint -> complaintListener.invoke(id)
                    R.id.edit ->  editPostClickListener.invoke(groupPostEntity)
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

}