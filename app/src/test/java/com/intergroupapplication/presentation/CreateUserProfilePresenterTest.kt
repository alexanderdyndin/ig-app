package com.intergroupapplication.presentation

import com.intergroupapplication.domain.FakeData
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.domain.gateway.UserProfileGateway
import com.intergroupapplication.presentation.feature.createuserprofile.presenter.CreateUserProfilePresenter
import com.intergroupapplication.presentation.feature.createuserprofile.view.CreateUserProfileView
import com.intergroupapplication.testingutils.RxSchedulesRule
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.never
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
 * Created by abakarmagomedov on 20/08/2018 at project InterGroupApplication.
 */
@RunWith(MockitoJUnitRunner::class)
class CreateUserProfilePresenterTest {
    @get:Rule
    val schedulerRule = RxSchedulesRule()

    private lateinit var createUserProfilePresenter: CreateUserProfilePresenter
    private val router: Router = mock()
    private val userProfileGateway: UserProfileGateway = mock()
    private val photoGateway: PhotoGateway = mock()
    private val errorHandler: ErrorHandler = mock()
    private val createUserProfileView: CreateUserProfileView = mock()


    @Before
    fun setUp() {
        createUserProfilePresenter = CreateUserProfilePresenter(router, userProfileGateway, photoGateway, errorHandler)
        createUserProfilePresenter.attachView(createUserProfileView)
    }

    @Test
    fun shouldSuccessCreateProfile() {
        whenever(userProfileGateway.createUserProfile(FakeData.getCreateUserEntity()))
                .thenReturn(Single.fromCallable { FakeData.getUserEntity() })
        createUserProfilePresenter.createUserProfile(FakeData.getCreateUserEntity())
        verify(createUserProfileView).showLoading(true)
        verify(createUserProfileView).showLoading(false)
        verify(router).newRootScreen(Screens.NAVIGATION_SCREEN)
    }

    @Test
    fun shouldCallErrorHandler() {
        whenever(userProfileGateway.createUserProfile(FakeData.getCreateUserEntity()))
                .thenReturn(Single.error(FakeData.invalidCredentialsException))
        createUserProfilePresenter.createUserProfile(FakeData.getCreateUserEntity())
        verify(createUserProfileView).showLoading(true)
        verify(createUserProfileView).showLoading(false)
        verify(router, never()).newRootScreen(Screens.NAVIGATION_SCREEN)
    }

}
