package com.intergroupapplication.presentation.feature.commentsdetails.presenter

import android.webkit.MimeTypeMap
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsView

import moxy.InjectViewState
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ImageUploadDto
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.entity.AudioRequestEntity
import com.intergroupapplication.domain.entity.CreateCommentEntity
import com.intergroupapplication.domain.entity.FileRequestEntity
import com.intergroupapplication.domain.exception.CanNotUploadAudio
import com.intergroupapplication.domain.exception.CanNotUploadPhoto
import com.intergroupapplication.domain.exception.CanNotUploadVideo
import com.intergroupapplication.domain.gateway.CommentGateway
import com.intergroupapplication.domain.gateway.ComplaintsGateway
import com.intergroupapplication.domain.gateway.GroupPostGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.workable.errorhandler.ErrorHandler
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber


import javax.inject.Inject

@InjectViewState
class CommentsDetailsPresenter @Inject constructor(
                                                   private val postGateway: GroupPostGateway,
                                                   private val complaintsGateway: ComplaintsGateway,
                                                   private val errorHandler: ErrorHandler,
                                                   )
    : BasePresenter<CommentsDetailsView>() {

    private val commentsDisposable = CompositeDisposable()
    var postId: String? = null

    fun getPostDetailsInfo(postId: String) {
        compositeDisposable.add(postGateway.getPostById(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ viewState.showPostDetailInfo(it) }, {
                    errorHandler.handle(it)
                    viewState.hideSwipeLayout()
                }))
    }

    fun complaintPost(postId: Int) {
        commentsDisposable.add(complaintsGateway.complaintPost(postId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    viewState?.showMessage(R.string.complaint_send)
                }, {
                    errorHandler.handle(it)
                }))
    }

    fun complaintComment(commentId: Int) {
        commentsDisposable.add(complaintsGateway.complaintComment(commentId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    viewState?.showMessage(R.string.complaint_send)
                }, {
                    errorHandler.handle(it)
                }))
    }

    override fun onDestroy() {
        super.onDestroy()
        commentsDisposable.clear()
    }

}
