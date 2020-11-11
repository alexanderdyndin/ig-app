package com.intergroupapplication.presentation

import androidx.paging.PagedList
import com.intergroupapplication.domain.FakeData
import com.intergroupapplication.domain.gateway.GroupGateway
import com.intergroupapplication.domain.usecase.AppStatusUseCase
import com.intergroupapplication.presentation.feature.grouplist.presenter.GroupListPresenter
import com.intergroupapplication.presentation.feature.grouplist.view.GroupListView
import com.intergroupapplication.presentation.feature.navigation.presenter.NavigationPresenter
import com.intergroupapplication.presentation.feature.navigation.view.NavigationView
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
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import ru.terrakok.cicerone.Router

/**
 * Created by abakarmagomedov on 18/09/2018 at project InterGroupApplication.
 */
@RunWith(MockitoJUnitRunner::class)
class GroupListPresenterTest {
    @get:Rule
    val schedulerRule = RxSchedulesRule()

    private lateinit var groupListPresenter: GroupListPresenter
    private val router: Router = mock()
    private val groupGateway: GroupGateway = mock()
    private val errorHandler: ErrorHandler = spy(ErrorHandler.defaultErrorHandler())
    private val appStatusUseCase: AppStatusUseCase = mock()
    private val groupListView: GroupListView = mock()

    @Before
    fun setUp() {
        groupListPresenter = GroupListPresenter(router, errorHandler, appStatusUseCase)
        groupListPresenter.attachView(groupListView)
    }

    @Test
    fun shouldUploadGroupList() {
        whenever(groupGateway.getGroupList(1,"")).thenReturn(Single.just(FakeData.getGroupList()))
        groupListPresenter.getGroupsList()
        verify(groupListView).showLoading(true)
        verify(groupListView).showLoading(false)
        verify(groupListView).groupListLoaded(mockPagedList(FakeData.getGroupList()))
    }

    @Test
    fun shouldShowInternetConnectionError() {
        whenever(groupGateway.getGroupList(1,""))
                .thenReturn(Single.error(FakeData.ioException))
        groupListPresenter.getGroupsList()
        verify(groupListView).showLoading(true)
        verify(groupListView).showLoading(false)
        verify(errorHandler).handle(FakeData.ioException)
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