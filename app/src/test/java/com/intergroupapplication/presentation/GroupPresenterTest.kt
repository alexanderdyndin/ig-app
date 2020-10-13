package com.intergroupapplication.presentation

import android.content.Context
import com.intergroupapplication.domain.FakeData
import com.intergroupapplication.domain.gateway.GroupGateway
import com.intergroupapplication.domain.gateway.GroupPostGateway
import com.intergroupapplication.domain.usecase.GroupUseCase
import com.intergroupapplication.presentation.feature.group.presenter.GroupPresenter
import com.intergroupapplication.presentation.feature.group.view.GroupView
import com.intergroupapplication.testingutils.RxSchedulesRule
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.workable.errorhandler.ErrorHandler
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import ru.terrakok.cicerone.Router

/**
 * Created by abakarmagomedov on 16/09/2018 at project InterGroupApplication.
 */
@RunWith(MockitoJUnitRunner::class)
class GroupPresenterTest {
    @get:Rule
    val schedulerRule = RxSchedulesRule()

    private lateinit var groupPresenter: GroupPresenter
    private val router: Router = mock()
    private val groupGateway: GroupGateway = mock()
    private val groupPostGateway: GroupPostGateway = mock()
    private val groupUseCase: GroupUseCase = mock()
    private val errorHandler: ErrorHandler = spy(ErrorHandler.defaultErrorHandler())
    private val context: Context = mock()
    private val groupView: GroupView = mock()

    @Before
    fun setUp() {
        groupPresenter = GroupPresenter(router, groupGateway, groupPostGateway,
                groupUseCase, errorHandler, context)
        groupPresenter.attachView(groupView)
    }

    @Test
    fun shouldUploadGroupPostsSuccessfully() {
        whenever(groupPostGateway.getGroupPosts("1", 1))
                .thenReturn(Single.just(FakeData.getGroupPostsList()))
        groupPresenter.getGroupPosts("1")
        verify(groupView).showLoading(true)
        verify(groupView).showLoading(false)
        verify(groupView).postsLoaded(FakeData.getGroupPostsList())
    }

    @Test
    fun shouldFollowGroup() {
        whenever(groupGateway.followGroup("1"))
                .thenReturn(Completable.complete())
        groupPresenter.followGroup("1")
        verify(groupView).showLoading(true)
        verify(groupView).showLoading(false)
        verify(groupView).groupFollowed()
    }

}
