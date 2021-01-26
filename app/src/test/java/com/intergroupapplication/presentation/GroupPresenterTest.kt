package com.intergroupapplication.presentation

import android.content.Context
import androidx.paging.PagedList
import com.intergroupapplication.domain.FakeData
import com.intergroupapplication.domain.gateway.ComplaintsGetaway
import com.intergroupapplication.domain.gateway.GroupGateway
import com.intergroupapplication.domain.gateway.GroupPostGateway
import com.intergroupapplication.domain.usecase.GroupUseCase
import com.intergroupapplication.presentation.delegate.ImageUploadingDelegate
import com.intergroupapplication.presentation.feature.group.pagingsource.GroupPostDataSourceFactory
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
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner


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
    private val groupPostDataSourceFactory: GroupPostDataSourceFactory = mock()
    private val imageUploadingDelegate: ImageUploadingDelegate = mock()
    private val complaintsGetaway: ComplaintsGetaway = mock()

    @Before
    fun setUp() {
        groupPresenter = GroupPresenter(router, groupGateway, groupPostDataSourceFactory,
                groupUseCase, imageUploadingDelegate, errorHandler, complaintsGetaway)
        groupPresenter.attachView(groupView)
    }

    @Test
    fun shouldUploadGroupPostsSuccessfully() {
        whenever(groupPostGateway.getGroupPosts("1", 1))
                .thenReturn(Single.just(FakeData.getGroupPostsList()))
        groupPresenter.getGroupPosts("1")
        verify(groupView).showLoading(true)
        verify(groupView).showLoading(false)
        verify(groupView).postsLoaded(mockPagedList(FakeData.getGroupPostsList()))
    }

    @Test
    fun shouldFollowGroup() {
        whenever(groupGateway.followGroup("1"))
                .thenReturn(Completable.complete())
        groupPresenter.followGroup("1", 228)
        verify(groupView).showLoading(true)
        verify(groupView).showLoading(false)
        verify(groupView).groupFollowed(228 + 1)
    }

    fun <T> mockPagedList(list: List<T>): PagedList<T> {
        val pagedList = Mockito.mock(PagedList::class.java) as PagedList<T>
        Mockito.`when`(pagedList.get(ArgumentMatchers.anyInt())).then { invocation ->
            val index = invocation.arguments.first() as Int
            list[index]
        }
        Mockito.`when`(pagedList.size).thenReturn(list.size)
        return pagedList
    }

}
