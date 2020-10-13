package com.intergroupapplication.presentation

import com.intergroupapplication.domain.FakeData
import com.intergroupapplication.domain.gateway.ConfirmationMailGateway
import com.intergroupapplication.presentation.feature.confirmationmail.presenter.ConfirmationMailPresenter
import com.intergroupapplication.presentation.feature.confirmationmail.view.ConfirmationMailView
import com.intergroupapplication.testingutils.RxSchedulesRule
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
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
class ConfirmationPresenterTest {
    @get:Rule
    val schedulerRule = RxSchedulesRule()

    private lateinit var confirmationMailPresenter: ConfirmationMailPresenter
    private val router: Router = mock()
    private val confirmationMailGateway: ConfirmationMailGateway = mock()
    private val errorHandler: ErrorHandler = mock()
    private val confirmationMailView: ConfirmationMailView = mock()

    @Before
    fun setUp() {
        confirmationMailPresenter = ConfirmationMailPresenter(router, confirmationMailGateway, errorHandler)
        confirmationMailPresenter.attachView(confirmationMailView)
    }

    @Test
    fun shouldSuccessConfirmMail() {
        whenever(confirmationMailGateway.confirmMail(FakeData.CONFIRM_MAIL_CODE)).thenReturn(Completable.complete())
        confirmationMailPresenter.confirmMail(FakeData.CONFIRM_MAIL_CODE)
        verify(confirmationMailView).showLoading(true)
        verify(confirmationMailView).showLoading(false)
        verify(router).newRootScreen(Screens.CREATE_USER_PROFILE_SCREEN)
    }

    @Test
    fun shouldCallErrorHandler() {
        whenever(confirmationMailGateway.confirmMail(FakeData.CONFIRM_MAIL_CODE)).thenReturn(Completable.error(FakeData.invalidCredentialsException))
        confirmationMailPresenter.confirmMail(FakeData.CONFIRM_MAIL_CODE)
        verify(confirmationMailView).showLoading(true)
        verify(confirmationMailView).showLoading(false)
        verify(router, never()).newRootScreen(Screens.CREATE_USER_PROFILE_SCREEN)
    }

}
