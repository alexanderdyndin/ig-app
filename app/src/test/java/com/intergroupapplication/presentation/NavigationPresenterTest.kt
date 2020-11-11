package com.intergroupapplication.presentation

import com.intergroupapplication.domain.FakeData
import com.intergroupapplication.domain.gateway.GroupGateway
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
import org.mockito.junit.MockitoJUnitRunner
import ru.terrakok.cicerone.Router

/**
 * Created by abakarmagomedov on 18/09/2018 at project InterGroupApplication.
 */
@RunWith(MockitoJUnitRunner::class)
class NavigationPresenterTest {
    @get:Rule
    val schedulerRule = RxSchedulesRule()

    private lateinit var navigationPresenter: NavigationPresenter
    private val router: Router = mock()
    private val groupGateway: GroupGateway = mock()
    private val errorHandler: ErrorHandler = spy(ErrorHandler.defaultErrorHandler())
    private val navigationView: NavigationView = mock()

//    @Before
//    fun setUp() {
//        navigationPresenter = NavigationPresenter(router, groupGateway, errorHandler)
//        navigationPresenter.attachView(navigationView)
//    }
//
//    @Test
//    fun shouldUploadGroupList() {
//        whenever(groupGateway.getGroupList()).thenReturn(Single.just(FakeData.getGroupList()))
//        navigationPresenter.getGroupsList()
//        verify(navigationView).showLoading(true)
//        verify(navigationView).showLoading(false)
//        verify(navigationView).groupListLoaded(FakeData.getGroupList())
//    }
//
//    @Test
//    fun shouldShowInternetConnectionError() {
//        whenever(groupGateway.getGroupList())
//                .thenReturn(Single.error(FakeData.ioException))
//        navigationPresenter.getGroupsList()
//        verify(navigationView).showLoading(true)
//        verify(navigationView).showLoading(false)
//        verify(errorHandler).handle(FakeData.ioException)
//    }

}