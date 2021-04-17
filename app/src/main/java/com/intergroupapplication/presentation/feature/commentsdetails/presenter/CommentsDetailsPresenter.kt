package com.intergroupapplication.presentation.feature.commentsdetails.presenter

import android.util.Log
import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsView

import moxy.InjectViewState
import com.intergroupapplication.R
import com.intergroupapplication.domain.entity.CreateCommentEntity
import com.intergroupapplication.domain.gateway.CommentGateway
import com.intergroupapplication.domain.gateway.ComplaintsGateway
import com.intergroupapplication.domain.gateway.GroupPostGateway
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers


import javax.inject.Inject

@InjectViewState
class CommentsDetailsPresenter @Inject constructor(private val commentGateway: CommentGateway,
                                                   private val postGateway: GroupPostGateway,
                                                   private val complaintsGateway: ComplaintsGateway,
                                                   private val errorHandler: ErrorHandler)
    : BasePresenter<CommentsDetailsView>() {

    private val commentsDisposable = CompositeDisposable()

    fun createComment(postId: String, createCommentEntity: CreateCommentEntity) {
        compositeDisposable.add(commentGateway.createComment(postId, createCommentEntity)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { viewState.showCommentUploading(true) }
                .doFinally { viewState.showCommentUploading(false) }
                .subscribe({ viewState.commentCreated(it) }, {
                    errorHandler.handle(it)
                    viewState.hideSwipeLayout()
                }))
    }

    fun createAnswerToComment(answerToCommentId: String, createCommentEntity: CreateCommentEntity) {
        compositeDisposable.add(commentGateway.createAnswerToComment(answerToCommentId, createCommentEntity)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { viewState.showCommentUploading(true) }
                .doFinally { viewState.showCommentUploading(false) }
                .subscribe({ viewState.answerToCommentCreated(it) }, {
                    errorHandler.handle(it)
                    viewState.hideSwipeLayout()
                }))
    }

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
