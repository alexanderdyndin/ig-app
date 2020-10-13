package com.intergroupapplication.presentation

import com.intergroupapplication.domain.FakeData
import com.intergroupapplication.domain.gateway.ImeiGateway
import com.intergroupapplication.domain.gateway.RegistrationGateway
import com.intergroupapplication.presentation.feature.registration.presenter.RegistrationPresenter
import com.intergroupapplication.presentation.feature.registration.view.RegistrationView
import com.intergroupapplication.testingutils.RxSchedulesRule
import com.nhaarman.mockito_kotlin.*
import com.workable.errorhandler.ErrorHandler
import io.reactivex.Completable
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
class RegistrationPresenterTest {
    @get:Rule
    val schedulerRule = RxSchedulesRule()

    private lateinit var registrationPresenter: RegistrationPresenter
    private val router: Router = mock()
    private val registrationGateway: RegistrationGateway = mock()
    private val imeiGateway: ImeiGateway = mock()
    private val registrationView: RegistrationView = mock()
    private val errorHandler: ErrorHandler = spy(ErrorHandler.defaultErrorHandler())


    @Before
    fun setUp() {
        registrationPresenter = RegistrationPresenter(router, registrationGateway, imeiGateway, errorHandler)
        registrationPresenter.attachView(registrationView)
    }

    @Test
    fun shouldSuccessRegister() {
        whenever(registrationGateway.performRegistration(FakeData.getRegistrationEntity())).thenReturn(Completable.complete())
        registrationPresenter.performRegistration(FakeData.getRegistrationEntity())
        verify(registrationView).showLoading(true)
        verify(registrationView).showLoading(false)
        verify(router).newRootScreen(Screens.CONFIRMATION_MAIL_SCREEN)
    }

    @Test
    fun shouldCallErrorHandler() {
        whenever(registrationGateway.performRegistration(FakeData.getRegistrationEntity()))
                .thenReturn(Completable.error(FakeData.invalidCredentialsException))
        registrationPresenter.performRegistration(FakeData.getRegistrationEntity())
        verify(registrationView).showLoading(true)
        verify(registrationView).showLoading(false)
        verify(errorHandler).handle(FakeData.invalidCredentialsException)
        verify(router, never()).newRootScreen(Screens.CONFIRMATION_MAIL_SCREEN)
    }

}
