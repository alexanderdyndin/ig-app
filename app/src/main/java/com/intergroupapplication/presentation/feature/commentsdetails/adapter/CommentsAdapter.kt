package com.intergroupapplication.presentation.feature.commentsdetails.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

import com.danikula.videocache.HttpProxyCacheServer
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ShortDynamicLink
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.firebase.dynamiclinks.DynamicLink
import com.intergroupapplication.R
import com.intergroupapplication.data.model.MarkupModel
import com.intergroupapplication.databinding.ItemCommentAnswerBinding
import com.intergroupapplication.databinding.ItemCommentBinding
import com.intergroupapplication.databinding.ItemGroupPostBinding
import com.intergroupapplication.domain.entity.CommentEntity
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.domain.exception.FieldException
import com.intergroupapplication.domain.exception.NotFoundException
import com.intergroupapplication.presentation.base.AdViewHolder
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.exstension.*
import com.intergroupapplication.presentation.feature.commentsdetails.viewmodel.CommentsViewModel
import com.omega_r.libs.omegaintentbuilder.OmegaIntentBuilder
import com.omega_r.libs.omegaintentbuilder.downloader.DownloadCallback
import com.omega_r.libs.omegaintentbuilder.handlers.ContextIntentHandler
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.exceptions.CompositeException
import io.reactivex.schedulers.Schedulers

/**
 * Created by abakarmagomedov on 28/08/2018 at project InterGroupApplication.
 */
class CommentsAdapter(private val imageLoadingDelegate: ImageLoadingDelegate,
                      private val proxyCacheServer: HttpProxyCacheServer,
                      private val manager:FragmentManager)
    : PagingDataAdapter<CommentEntity, RecyclerView.ViewHolder>(diffUtil) {

    companion object {
        private const val POST_IN_COMMENT_HOLDER = 332
        private const val DEFAULT_HOLDER = 1488
        private const val ANSWER_HOLDER = 228
        const val EDIT_COMMENT_REQUEST = "edit_comment_request"
        const val COMMENT_KEY = "comment_key"
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
        var deleteAnswerLayout: () -> Unit = {}
        var complaintListener: (Int) -> Unit = {}
        var imageClickListener: (List<FileEntity>, Int) -> Unit = { _: List<FileEntity>, _: Int -> }
        var likeClickListener: (isLike: Boolean, isDislike: Boolean, item: CommentEntity.Comment, position: Int) -> Unit = { _, _, _, _ -> }
        var deleteClickListener: (postId: Int, position: Int) -> Unit = { _, _ ->}
        var showPostDetailInfo: (groupPostEntity:CommentEntity.PostEntity) ->Unit = {_->}
        var clicksSettingPost: (settingPost: ImageView)->Unit = { _->}
        var changeCountComments: (count:Int)->Unit = {_->}
        var progressBarVisibility:(visibility:Boolean) -> Unit = {_->}
        var AD_TYPE = 1
        var AD_FREQ = 3
        var AD_FIRST = 3
        var USER_ID: Int? = null
    }


    private var compositeDisposable = CompositeDisposable()
    private  var groupPostEntity:CommentEntity.PostEntity? = null
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
        getItem(position)?.let {
            when (holder) {
                is CommentViewHolder -> if (it is CommentEntity.Comment) holder.bind(it)
                is CommentAnswerViewHolder -> if (it is CommentEntity.Comment) holder.bind(it)
                is AdViewHolder -> if (it is CommentEntity.AdEntity) holder.bind(it.nativeAd, AD_TYPE, "comments")
                is PostInCommentHolder -> if (it is CommentEntity.PostEntity) {
                    groupPostEntity = it
                    holder.bind(it)
                }
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
            is CommentEntity.PostEntity -> POST_IN_COMMENT_HOLDER
            else -> throw IllegalStateException("Unknown view")
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
        private val settingPostInComments = view.findViewById<ImageView>(R.id.settingsPost)
        private val viewBinding by viewBinding (ItemGroupPostBinding::bind)
        private val countComments = view.findViewById<TextView>(R.id.countComments)

        fun bind(groupPostEntity: CommentEntity.PostEntity){
            with(viewBinding){
                compositeDisposable.add(getDateDescribeByString(groupPostEntity.date)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe{ text -> postPrescription.text = text })

                postCustomView.proxy = proxyCacheServer
                postCustomView.imageClickListener = imageClickListener
                postCustomView.imageLoadingDelegate = imageLoadingDelegate
                postCustomView.setUpPost(mapToGroupEntityPost(groupPostEntity))

                idpGroupPost.text = view.context.getString(R.string.idp, groupPostEntity.id)
                countComments.text = groupPostEntity.commentsCount
                idpGroupPost.text = view.context.getString(R.string.idp, groupPostEntity.idp.toString())
                countComments.text = groupPostEntity.commentsCount
                groupName.text = groupPostEntity.groupInPost.name
                postDislike.text = groupPostEntity.reacts.dislikesCount.toString()
                postLike.text = groupPostEntity.reacts.likesCount.toString()
                subCommentBtn.text = groupPostEntity.bells.count.toString()
                btnRepost.setOnClickListener {
                    sharePost(groupPostEntity,view.context)
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
                                    }
                                )
                            )
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

        private fun mapToGroupEntityPost(postEntity: CommentEntity.PostEntity) =
                MarkupModel(postEntity.postText,postEntity.images,postEntity.audios,postEntity.videos,
                        postEntity.imagesExpanded,postEntity.audiosExpanded,postEntity.videosExpanded)

        private fun sharePost(item: CommentEntity.PostEntity,context: Context){
            progressBarVisibility.invoke(true)
            FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setDomainUriPrefix( context.getString(R.string.deeplinkDomain))
                .setLink(Uri.parse("https://intergroup.com/post/${item.id}"))
                .setAndroidParameters(
                    DynamicLink.AndroidParameters.Builder("com.intergroupapplication")
                        .setMinimumVersion(1).build())
                .buildShortDynamicLink(ShortDynamicLink.Suffix.SHORT)
                .addOnCompleteListener {
                    createShareIntent(item ,it.result.previewLink.toString(),context)
                }
        }

        fun increaseCommentsCounter() {
            var commentsCount = if (countComments.text.toString() != "")
                countComments.text.toString().toInt()
            else {
                groupPostEntity?.activeCommentsCount?.toInt()?:0
            }
            commentsCount++
            countComments.text = commentsCount.toString()
            changeCountComments.invoke(commentsCount)
        }

        private fun createShareIntent(item:CommentEntity.PostEntity, url:String,context: Context){
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
                        progressBarVisibility.invoke(false)
                        contextIntentHandler.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                .startActivity()
                    }
                }
            )
        }
    }

    inner class CommentViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        private val viewBinding by viewBinding(ItemCommentBinding::bind)

        fun bind(item: CommentEntity.Comment) {
            with(viewBinding) {
                val name = item.commentOwner
                        ?.let { "${it.firstName} ${it.secondName}" }
                        ?: let { itemView.resources.getString(R.string.unknown_user) }
                userName.text = name
                idUser.text = itemView.context.getString(R.string.id,
                        item.commentOwner?.user ?: "нет id")
                //postText3.text = item.text
                compositeDisposable.add(getDateDescribeByString(item.date)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe{ text->
                            timeComment.text = text
                        })
                postDislike.text = item.reacts.dislikesCount.toString()
                postLike.text = item.reacts.likesCount.toString()
                userAvatarHolder.run {
                    doOrIfNull(item.commentOwner?.avatar, {
                        imageLoadingDelegate.loadImageFromUrl(it, this) },
                        {
                            imageLoadingDelegate.
                            loadImageFromResources(R.drawable.application_logo, this) })
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
                settingsBtn.setOnClickListener { showPopupMenu(it,item) }

                commentCustomView.proxy = proxyCacheServer
                commentCustomView.imageClickListener = imageClickListener
                commentCustomView.imageLoadingDelegate = imageLoadingDelegate
                commentCustomView.setUpPost(mapToGroupEntityPost(item))

            }
        }

        private fun showPopupMenu(view: View,item: CommentEntity.Comment) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.inflate(R.menu.settings_menu)
            popupMenu.menu.findItem(R.id.edit).isVisible = item.commentOwner?.user == USER_ID
            popupMenu.menu.findItem(R.id.delete).isVisible = item.commentOwner?.user == USER_ID
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.complaint -> complaintListener.invoke(item.id.toInt())
                    R.id.edit -> {
                        deleteAnswerLayout.invoke()
                        manager.setResult(EDIT_COMMENT_REQUEST, COMMENT_KEY to item)
                    }
                    R.id.delete -> deleteClickListener.invoke(item.id.toInt(), layoutPosition)
                }
                return@setOnMenuItemClickListener true
            }
            popupMenu.show()
        }

        private fun mapToGroupEntityPost(comment: CommentEntity.Comment) =
                MarkupModel(comment.text,comment.images,comment.audios,comment.videos)
    }

    inner class CommentAnswerViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        private val viewBinding by viewBinding(ItemCommentAnswerBinding::bind)

        fun bind(item: CommentEntity.Comment) {
            with(viewBinding) {
                idcGroupUser2.text = itemView.context.getString(R.string.idc, item.idc.toString())
                compositeDisposable.add(getDateDescribeByString(item.date)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe{ text ->
                            timeComment2.text = text
                        })
                userName2.text = item.commentOwner
                        ?.let { "${it.firstName} ${it.secondName}" }
                        ?: let { itemView.resources.getString(R.string.unknown_user) }
                idUser2.text = itemView.context.getString(R.string.id,
                        item.commentOwner?.user ?: "нет id")
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
                settingsBtn2.setOnClickListener { showPopupMenu(it, item) }

                val replyName =  item.answerTo?.commentOwner
                        ?.let { "${it.firstName} ${it.secondName}, " }
                        ?: let { itemView.resources.getString(R.string.unknown_user) }
                answerCommentCustomView.proxy = proxyCacheServer
                answerCommentCustomView.imageClickListener = imageClickListener
                answerCommentCustomView.imageLoadingDelegate = imageLoadingDelegate
                answerCommentCustomView.setUpPost(mapToGroupEntityPost(item,replyName+item.text))

            }
        }

        private fun mapToGroupEntityPost(comment: CommentEntity.Comment, textAnswerComment:String) =
                MarkupModel(textAnswerComment,comment.images,comment.audios,comment.videos)

        private fun showPopupMenu(view: View, item:CommentEntity.Comment) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.inflate(R.menu.settings_menu)
            popupMenu.menu.findItem(R.id.edit).isVisible = item.commentOwner?.user == USER_ID
//            popupMenu.menu.findItem(R.id.delete).isVisible = userId == USER_ID
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.complaint -> complaintListener.invoke(item.id.toInt())
                    R.id.edit -> {
                        deleteAnswerLayout.invoke()
                        manager.setResult(EDIT_COMMENT_REQUEST, COMMENT_KEY to item)
                    }
                    R.id.delete -> deleteClickListener.invoke(item.id.toInt(), layoutPosition)
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