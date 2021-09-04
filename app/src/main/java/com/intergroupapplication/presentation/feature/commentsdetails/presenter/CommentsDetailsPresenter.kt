package com.intergroupapplication.presentation.feature.commentsdetails.presenter

import com.intergroupapplication.presentation.base.BasePresenter
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsView

import com.intergroupapplication.R
import com.intergroupapplication.domain.gateway.ComplaintsGateway
import com.intergroupapplication.domain.gateway.GroupPostGateway
import com.workable.errorhandler.ErrorHandler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import moxy.InjectViewState
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
