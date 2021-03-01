package com.intergroupapplication.presentation

import android.content.Context
import com.intergroupapplication.domain.FakeData
import com.intergroupapplication.domain.gateway.CommentGateway
import com.intergroupapplication.domain.gateway.ComplaintsGateway
import com.intergroupapplication.domain.gateway.GroupPostGateway
import com.intergroupapplication.presentation.feature.commentsdetails.presenter.CommentsDetailsPresenter
import com.intergroupapplication.presentation.feature.commentsdetails.view.CommentsDetailsView
import com.intergroupapplication.testingutils.RxSchedulesRule
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.workable.errorhandler.ErrorHandler
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner


/**
 * Created by abakarmagomedov on 16/09/2018 at project InterGroupApplication.
 */
@RunWith(MockitoJUnitRunner::class)
class CommentDetailsPresenterTest {
    @get:Rule
    val schedulerRule = RxSchedulesRule()

    private lateinit var commentsDetailsPresenter: CommentsDetailsPresenter
    private val commentGateway: CommentGateway = mock()
    private val postGateway: GroupPostGateway = mock()
    private val errorHandler: ErrorHandler = spy(ErrorHandler.defaultErrorHandler())
    private val context: Context = mock()
    private val commentDetailsView: CommentsDetailsView = mock()
    private val complaintsGateway: ComplaintsGateway = mock()

    @Before
    fun setUp() {
//        commentsDetailsPresenter = CommentsDetailsPresenter(router, commentGateway,
//                postGateway, commentsDataSourceFactory, complaintsGateway, errorHandler)
//        commentsDetailsPresenter.attachView(commentDetailsView)
    }

    @Test
    fun shouldSuccessCreateComment() {
        whenever(commentGateway.createComment("0", FakeData.getCreateCommentEntity()))
                .thenReturn(Single.just(FakeData.getAnswerCommentEntity()))
        commentsDetailsPresenter.createComment("0", FakeData.getCreateCommentEntity())
        verify(commentDetailsView).showCommentUploading(true)
        verify(commentDetailsView).showCommentUploading(false)
        verify(commentDetailsView).commentCreated(FakeData.getAnswerCommentEntity())
    }

    @Test
    fun shouldSuccessCreateAnswerToComment() {
        whenever(commentGateway.createAnswerToComment("15", FakeData.getCreateCommentEntity()))
                .thenReturn(Single.just(FakeData.getAnswerCommentEntity()))
        commentsDetailsPresenter.createAnswerToComment("15", FakeData.getCreateCommentEntity())
        verify(commentDetailsView).showCommentUploading(true)
        verify(commentDetailsView).showCommentUploading(false)
        verify(commentDetailsView).answerToCommentCreated(FakeData.getAnswerCommentEntity())
    }

    @Test
    fun shouldSuccessUploadPostsComments() {
        whenever(commentGateway.getComments("1",1)).thenReturn(Single.just(FakeData.getCommentsList()))
//        commentsDetailsPresenter.getPostComments("1")
//        verify(commentDetailsView).showLoading(true)
//        verify(commentDetailsView).showLoading(false)
//        verify(commentDetailsView).commentsLoaded(mockPagedList(FakeData.getCommentsList()))
    }

    @Test
    fun shouldExitGroupBecauseItIsBlocked() {
        //whenever(commentGateway.getComments("1")).thenReturn()
        //To test it backend should add exception for group blocked
    }

//    fun <T> mockPagedList(list: List<T>): PagedList<T> {
//        val pagedList = Mockito.mock(PagedList::class.java) as PagedList<T>
//        Mockito.`when`(pagedList.get(ArgumentMatchers.anyInt())).then { invocation ->
//            val index = invocation.arguments.first() as Int
//            list[index]
//        }
//        Mockito.`when`(pagedList.size).thenReturn(list.size)
//        return pagedList
//    }


}