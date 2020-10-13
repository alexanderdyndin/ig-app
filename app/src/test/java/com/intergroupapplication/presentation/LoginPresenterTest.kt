package com.intergroupapplication.presentation

import com.intergroupapplication.domain.FakeData
import com.intergroupapplication.domain.gateway.ImeiGateway
import com.intergroupapplication.domain.gateway.LoginGateway
import com.intergroupapplication.presentation.feature.login.presenter.LoginPresenter
import com.intergroupapplication.presentation.feature.login.view.LoginView
import com.intergroupapplication.testingutils.RxSchedulesRule
import com.nhaarman.mockito_kotlin.*
import com.workable.errorhandler.ErrorHandler
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import ru.terrakok.cicerone.Router

/**
 * Created by abakarmagomedov on 20/08/2018 at project InterGroupApplication.
 */
@RunWith(MockitoJUnitRunner::class)
class LoginPresenterTest {
    @get:Rule
    val schedulerRule = RxSchedulesRule()

    private lateinit var loginPresenter: LoginPresenter
    private val router: Router = mock()
    private val loginGateway: LoginGateway = mock()
    private val imeiGateway: ImeiGateway = mock()
    private val errorHandler: ErrorHandler = spy(ErrorHandler.defaultErrorHandler())
    private val loginView: LoginView = mock()


    @Before
    fun setUp() {
        loginPresenter = LoginPresenter(router, loginGateway, imeiGateway, errorHandler)
        loginPresenter.attachView(loginView)
    }

    @Test
    fun shouldSuccessLogin() {
        whenever(imeiGateway.extractDeviceInfo()).thenReturn(Completable.complete())
        whenever(loginGateway.performLogin(FakeData.getLoginEntity())).thenReturn(Single.fromCallable { FakeData.getTokenEntity() })
        loginPresenter.extractDeviceInfo()
        loginPresenter.performLogin(FakeData.getLoginEntity())
        verify(loginView).deviceInfoExtracted()
        verify(loginView).showLoading(true)
        verify(loginView).showLoading(false)
        verify(loginView, never()).clearViewErrorState()
        verify(router).newRootScreen(Screens.NAVIGATION_SCREEN)
    }

    @Test
    fun shouldCallErrorHandler() {
        whenever(imeiGateway.extractDeviceInfo()).thenReturn(Completable.complete())
        whenever(loginGateway.performLogin(FakeData.getLoginEntity())).thenReturn(Single.error(FakeData.invalidCredentialsException))
        loginPresenter.extractDeviceInfo()
        loginPresenter.performLogin(FakeData.getLoginEntity())
        verify(loginView).showLoading(true)
        verify(loginView).showLoading(false)
        verify(router, never()).newRootScreen(Screens.NAVIGATION_SCREEN)
        //verify(errorHandler).handle(FakeData.invalidCredentialsException)
    }
}
