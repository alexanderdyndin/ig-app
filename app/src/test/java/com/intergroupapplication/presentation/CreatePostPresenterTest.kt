package com.intergroupapplication.presentation

import com.intergroupapplication.domain.FakeData
import com.intergroupapplication.domain.gateway.GroupPostGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.feature.createpost.presenter.CreatePostPresenter
import com.intergroupapplication.presentation.feature.createpost.view.CreatePostView
import com.intergroupapplication.testingutils.RxSchedulesRule
import com.nhaarman.mockito_kotlin.*
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
class CreatePostPresenterTest {
    @get:Rule
    val schedulerRule = RxSchedulesRule()

    private lateinit var createPostPresenter: CreatePostPresenter
    private val groupPostGateway: GroupPostGateway = mock()
    private val errorHandler: ErrorHandler = spy(ErrorHandler.defaultErrorHandler())
    private val createPostView: CreatePostView = mock()
    private val photoGateway: PhotoGateway = mock()
    private val imageUploadingDelegate: ImageUploadingDelegate = mock()


    @Before
    fun setUp() {
        createPostPresenter = CreatePostPresenter(groupPostGateway, photoGateway,
                errorHandler)
        createPostPresenter.attachView(createPostView)
    }


    @Test
    fun shouldCreatePostSuccessfully() {
//        whenever(groupPostGateway.createPost(FakeData.getCreateGroupPostEntity(), "1"))
//                .thenReturn(Single.just(FakeData.getGroupPostEntity()))
        //createPostPresenter.createPost(FakeData.getCreateGroupPostEntity(), "1")
        verify(createPostView).showLoading(true)
        verify(createPostView).showLoading(false)
        //verify(createPostView).postCreateSuccessfully(FakeData.getGroupPostEntity())
    }

    @Test
    fun shouldHandleNetworkExcpetion() {
        whenever(groupPostGateway.createPost(FakeData.getCreateGroupPostEntity(), "1"))
                .thenReturn(Single.error(FakeData.ioException))
        //createPostPresenter.createPost(FakeData.getCreateGroupPostEntity(), "1")
        verify(createPostView).showLoading(true)
        verify(createPostView).showLoading(false)
        //verify(createPostView, never()).postCreateSuccessfully(FakeData.getGroupPostEntity())
        verify(errorHandler).handle(FakeData.ioException)
    }

}
