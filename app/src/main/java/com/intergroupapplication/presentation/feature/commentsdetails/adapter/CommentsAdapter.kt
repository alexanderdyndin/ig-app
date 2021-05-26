package com.intergroupapplication.presentation.feature.commentsdetails.adapter

import android.content.Context
import android.content.Intent
import android.media.Image
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.danikula.videocache.HttpProxyCacheServer
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLink
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.domain.entity.GroupPostEntity
import com.intergroupapplication.domain.exception.FieldException
import com.intergroupapplication.domain.exception.NotFoundException
import com.intergroupapplication.presentation.customview.AudioGalleryView
import com.intergroupapplication.presentation.customview.ImageGalleryView
import com.intergroupapplication.presentation.customview.VideoGalleryView
import com.intergroupapplication.presentation.base.AdViewHolder
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsFragment
import com.intergroupapplication.presentation.feature.commentsdetails.viewmodel.CommentsViewModel
import com.omega_r.libs.omegaintentbuilder.OmegaIntentBuilder
import com.omega_r.libs.omegaintentbuilder.downloader.DownloadCallback
import com.omega_r.libs.omegaintentbuilder.handlers.ContextIntentHandler
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_comments_details.*
import kotlinx.android.synthetic.main.item_comment.view.*
import kotlinx.android.synthetic.main.item_comment.view.postDislike
import kotlinx.android.synthetic.main.item_comment.view.postLike
import kotlinx.android.synthetic.main.item_comment.view.postText as postText3
import kotlinx.android.synthetic.main.item_comment_answer.view.*
import kotlinx.android.synthetic.main.item_group_post.*
import kotlinx.android.synthetic.main.item_group_post.view.*
import kotlinx.android.synthetic.main.post_item_error.view.*
import timber.log.Timber
import kotlin.math.min

/**
 * Created by abakarmagomedov on 28/08/2018 at project InterGroupApplication.
 */
class CommentsAdapter(private val imageLoadingDelegate: ImageLoadingDelegate,
                      private val proxyCacheServer: HttpProxyCacheServer)
    : PagingDataAdapter<CommentEntity, RecyclerView.ViewHolder>(diffUtil) {

    companion object {
        private const val POST_IN_COMMENT_HOLDER = 332
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
        var imageClickListener: (List<FileEntity>, Int) -> Unit = { list: List<FileEntity>, i: Int -> }
        var likeClickListener: (isLike: Boolean, isDislike: Boolean, item: CommentEntity.Comment, position: Int) -> Unit = { _, _, _, _ -> }
        var deleteClickListener: (postId: Int, position: Int) -> Unit = { _, _ ->}
        var showPostDetailInfo: (groupPostEntity:GroupPostEntity.PostEntity) ->Unit = {_->}
        var clicksSettingPost: (settingPost: ImageView)->Unit = { _->}
        var changeCountComments: (count:Int)->Unit = {_->}
        var AD_TYPE = 1
        var AD_FREQ = 3
        var AD_FIRST = 3
        var USER_ID: Int? = null
    }


    private var compositeDisposable = CompositeDisposable()
    var groupPostEntity:GroupPostEntity.PostEntity? = null
    lateinit var errorHandler: ErrorHandler
    lateinit var viewModel:CommentsViewModel
    lateinit var postInCommentHolder: PostInCommentHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return when (viewType) {
            AdViewHolder.NATIVE_AD -> {
                view = parent.inflate(R.layout.layout_admob_news)
                AdViewHolder(view)
            }
            POST_IN_COMMENT_HOLDER ->{
                view = parent.inflate(R.layout.item_group_post)
                postInCommentHolder = PostInCommentHolder(view)
                postInCommentHolder
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
        if (position == 0) {
            groupPostEntity?.let { (holder as PostInCommentHolder).bind(it) }
        }
        else {
            getItem(position)?.let {
                when (holder) {
                    is CommentViewHolder -> if (it is CommentEntity.Comment) holder.bind(it)
                    is CommentAnswerViewHolder -> if (it is CommentEntity.Comment) holder.bind(it)
                    is AdViewHolder -> if (it is CommentEntity.AdEntity) holder.bind(it.nativeAd, AD_TYPE, "comments")
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) return POST_IN_COMMENT_HOLDER
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

    fun positionAnswerComment(commentId:String):Int{
        val rangePosition = IntRange(0,itemCount-1)
        rangePosition.forEach {position->
            val item = getItem(position)
            if(item is CommentEntity.Comment){
                if (item.id == commentId){
                    return@positionAnswerComment position
                }
            }

        }
        return 0
    }

    inner class PostInCommentHolder(val view:View):RecyclerView.ViewHolder(view){
        private val audioContainer = view.findViewById<AudioGalleryView>(R.id.audioBody)
        private val videoContainer = view.findViewById<VideoGalleryView>(R.id.videoBody)
        private val imageContainer = view.findViewById<ImageGalleryView>(R.id.imageBody)
        private val settingPostInComments = view.findViewById<ImageView>(R.id.settingsPost)
        val countComments = view.findViewById<TextView>(R.id.countComments)

        fun bind(groupPostEntity: GroupPostEntity.PostEntity){
            with(view){
                compositeDisposable.add(getDateDescribeByString(groupPostEntity.date)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ postPrescription.text = it }, { Timber.e(it) }))

                groupPostEntity.postText.let {
                    if (it.isNotEmpty()) {
                        postText.text = it
                        postText.show()
                    } else {
                        postText.gone()
                    }
                }
                idpGroupPost.text = context.getString(R.string.idp, groupPostEntity.id)
                countComments.text = groupPostEntity.commentsCount
                idpGroupPost.text = context.getString(R.string.idp, groupPostEntity.idp.toString())
                countComments.text = groupPostEntity.commentsCount
                groupName.text = groupPostEntity.groupInPost.name
                postDislike.text = groupPostEntity.reacts.dislikesCount.toString()
                postLike.text = groupPostEntity.reacts.likesCount.toString()
                subCommentBtn.text = groupPostEntity.bells.count.toString()
                btnRepost.setOnClickListener {
                    sharePost(groupPostEntity,context)
                }
                clicksSettingPost.invoke(settingPostInComments)

                if (groupPostEntity.reacts.isLike) {
                    postLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_like_active, 0, 0, 0)
                } else {
                    postLike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_like, 0, 0, 0)
                }
                if (groupPostEntity.reacts.isDislike) {
                    postDislike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_dislike_active, 0, 0, 0)
                } else {
                    postDislike.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_dislike, 0, 0, 0)
                }

                doOrIfNull(groupPostEntity.groupInPost.avatar, { imageLoadingDelegate.loadImageFromUrl(it, postAvatarHolder) },
                        { imageLoadingDelegate.loadImageFromResources(R.drawable.variant_10, postAvatarHolder) })


                imageContainer.setImages(groupPostEntity.images)
                imageContainer.imageClick = imageClickListener
                videoContainer.setVideos(groupPostEntity.videos)
                audioContainer.setAudios(groupPostEntity.audios)
                audioContainer.proxy = proxyCacheServer
                videoContainer.proxy = proxyCacheServer
                videoContainer.imageLoadingDelegate = imageLoadingDelegate

                subCommentBtn.setOnClickListener {
                    if (!groupPostEntity.isLoading) {
                        if (groupPostEntity.bells.isActive) {
                            compositeDisposable.add(viewModel.deleteBell(groupPostEntity.id)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .doOnSubscribe { groupPostEntity.isLoading = true }
                                    .doFinally {
                                        groupPostEntity.isLoading = false
                                        showPostDetailInfo(groupPostEntity)
                                    }
                                    .subscribe({
                                        groupPostEntity.bells.isActive = false
                                        groupPostEntity.bells.count--
                                    }, { exception ->
                                        if (exception is NotFoundException) {
                                            groupPostEntity.bells.isActive = false
                                            groupPostEntity.bells.count--
                                        } else
                                            errorHandler.handle(exception)
                                    }))
                        } else {
                            compositeDisposable.add(viewModel.setBell(groupPostEntity.id)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .doOnSubscribe { groupPostEntity.isLoading = true }
                                    .doFinally {
                                        groupPostEntity.isLoading = false
                                        showPostDetailInfo(groupPostEntity)
                                    }
                                    .subscribe({
                                        groupPostEntity.bells.isActive = true
                                        groupPostEntity.bells.count++

                                    }, { exception ->
                                        if (exception is CompositeException) {
                                            exception.exceptions.forEach { ex ->
                                                (ex as? FieldException)?.let {
                                                    if (it.field == "post") {
                                                        groupPostEntity.bells.isActive = true
                                                        groupPostEntity.bells.count++
                                                    }
                                                }
                                            }
                                        } else
                                            errorHandler.handle(exception)
                                    }))
                        }
                    }
                }
                postLikesClickArea.setOnClickListener {
                    if (!groupPostEntity.isLoading) {
                        compositeDisposable.add(viewModel.setReact(isLike = !groupPostEntity.reacts.isLike,
                                isDislike = groupPostEntity.reacts.isDislike, postId = groupPostEntity.id)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnSubscribe {
                                    groupPostEntity.isLoading = true
                                }
                                .doFinally {
                                    groupPostEntity.isLoading = false
                                    showPostDetailInfo(groupPostEntity)
                                }
                                .subscribe({
                                    groupPostEntity.reacts = it
                                },
                                        {
                                            errorHandler.handle(it)
                                        }))
                    }
                }
                postDislikesClickArea.setOnClickListener {
                    if (!groupPostEntity.isLoading) {
                        compositeDisposable.add(viewModel.setReact(isLike = groupPostEntity.reacts.isLike,
                                isDislike = !groupPostEntity.reacts.isDislike, postId = groupPostEntity.id)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnSubscribe {
                                    groupPostEntity.isLoading = true
                                }
                                .doFinally {
                                    groupPostEntity.isLoading = false
                                    showPostDetailInfo(groupPostEntity)
                                }
                                .subscribe({
                                    groupPostEntity.reacts = it
                                },
                                        {
                                            errorHandler.handle(it)
                                        }))
                    }
                }
            }
        }

        private fun sharePost(item: GroupPostEntity.PostEntity,context: Context){
            Timber.tag("tut_share").d("tut")
            //progressDownload.show()
            val link = Firebase.dynamicLinks.dynamicLink {
                domainUriPrefix =context.getString(R.string.deeplinkDomain)
                link = Uri.parse("https://intergroup.com/post/${item.id}")
                androidParameters(packageName = "com.intergroupapplication"){
                    minimumVersion = 1
                }
            }
            FirebaseDynamicLinks.getInstance().createDynamicLink()
                    .setLongLink(link.uri)
                    .buildShortDynamicLink(ShortDynamicLink.Suffix.SHORT)
                    .addOnCompleteListener {
                        createShareIntent(item ,it.result.previewLink.toString(),context)
                    }
        }

        fun increaseCommentsCounter() {
            var commentsCount = 0
            if (countComments.text.toString() != "")
                commentsCount = countComments.text.toString().toInt()
            else if(groupPostEntity != null){
                commentsCount = groupPostEntity?.activeCommentsCount?.toInt()?:0
            }
            commentsCount++
            countComments.text = commentsCount.toString()
            changeCountComments.invoke(commentsCount)
        }

        private fun createShareIntent(item: GroupPostEntity.PostEntity, url:String,context: Context){
            val text = url +"/${item.id}"
            val filesUrls = mutableListOf<String>()
            filesUrls.addAll(item.videos.map { it.file })
            filesUrls.addAll(item.images.map { it.file })
            OmegaIntentBuilder.from(context)
                    .share()
                    .text(text)
                    .filesUrls(filesUrls)
                    .download(object: DownloadCallback {
                        override fun onDownloaded(success: Boolean, contextIntentHandler: ContextIntentHandler) {
                            //progressDownload.gone()
                            contextIntentHandler.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    .startActivity()
                        }
                    })
        }
    }

    inner class CommentViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val audioContainer = itemView.findViewById<AudioGalleryView>(R.id.audioBody)
        val videoContainer = itemView.findViewById<VideoGalleryView>(R.id.videoBody)
        val imageContainer = itemView.findViewById<ImageGalleryView>(R.id.imageBody)

        fun bind(item: CommentEntity.Comment) {
            with(itemView) {
                val name = item.commentOwner
                        ?.let { "${it.firstName} ${it.secondName}" }
                        ?: let { itemView.resources.getString(R.string.unknown_user) }
                userName.text = name
                idUser.text = context.getString(R.string.id,
                        item.commentOwner?.user ?: "нет id")
                postText3.text = item.text
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
                    doOrIfNull(item.commentOwner?.avatar, {
                        imageLoadingDelegate.loadImageFromUrl(it, this) },
                            {
                                imageLoadingDelegate.loadImageFromResources(R.drawable.application_logo, this) })
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

                videoContainer.proxy = proxyCacheServer
                videoContainer.imageLoadingDelegate = imageLoadingDelegate
                videoContainer.setVideos(item.videos, false)
                //videoContainer.expand = { item.videosExpanded = it }

                audioContainer.proxy = proxyCacheServer
                audioContainer.setAudios(item.audios, false)
                //audioContainer.expand = { item.audiosExpanded = it }

                imageContainer.setImages(item.images, false)
                imageContainer.imageClick = imageClickListener
                //imageContainer.expand = { item.imagesExpanded = it }

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
        val audioContainer = itemView.findViewById<AudioGalleryView>(R.id.audioBody)
        val videoContainer = itemView.findViewById<VideoGalleryView>(R.id.videoBody)
        val imageContainer = itemView.findViewById<ImageGalleryView>(R.id.imageBody)

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

                videoContainer.proxy = proxyCacheServer
                videoContainer.imageLoadingDelegate = imageLoadingDelegate
                videoContainer.setVideos(item.videos, false)
                //videoContainer.expand = { item.videosExpanded = it }

                audioContainer.proxy = proxyCacheServer
                audioContainer.setAudios(item.audios, false)
                //audioContainer.expand = { item.audiosExpanded = it }

                imageContainer.setImages(item.images, false)
                imageContainer.imageClick = imageClickListener
                //imageContainer.expand = { item.imagesExpanded = it }

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