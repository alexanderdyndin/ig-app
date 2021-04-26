package com.intergroupapplication.presentation

import com.intergroupapplication.domain.FakeData
import com.intergroupapplication.domain.gateway.UserProfileGateway
import com.intergroupapplication.presentation.base.ImageUploader
import com.intergroupapplication.presentation.feature.createuserprofile.presenter.CreateUserProfilePresenter
import com.intergroupapplication.presentation.feature.createuserprofile.view.CreateUserProfileView
import com.intergroupapplication.testingutils.RxSchedulesRule
import com.nhaarman.mockito_kotlin.mock
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
 * Created by abakarmagomedov on 20/08/2018 at project InterGroupApplication.
 */
@RunWith(MockitoJUnitRunner::class)
class CreateUserProfilePresenterTest {
    @get:Rule
    val schedulerRule = RxSchedulesRule()

    private lateinit var createUserProfilePresenter: CreateUserProfilePresenter
    private val userProfileGateway: UserProfileGateway = mock()
    private val imageUploader: ImageUploader = mock()
    private val errorHandler: ErrorHandler = mock()
    private val createUserProfileView: CreateUserProfileView = mock()


    @Before
    fun setUp() {
      //  createUserProfilePresenter = CreateUserProfilePresenter(userProfileGateway, imageUploader, errorHandler)
        createUserProfilePresenter.attachView(createUserProfileView)
    }

    @Test
    fun shouldSuccessCreateProfile() {
        whenever(userProfileGateway.createUserProfile(FakeData.getCreateUserEntity()))
                .thenReturn(Single.fromCallable { FakeData.getUserEntity() })
        createUserProfilePresenter.createUserProfile("dude", "duck", "2018-03-03", "male")
        verify(createUserProfileView).showLoading(true)
        verify(createUserProfileView).showLoading(false)
    }

    @Test
    fun shouldCallErrorHandler() {
        whenever(userProfileGateway.createUserProfile(FakeData.getCreateUserEntity()))
                .thenReturn(Single.error(FakeData.invalidCredentialsException))
        createUserProfilePresenter.createUserProfile("dude", "duck", "2018-03-03", "male")
        verify(createUserProfileView).showLoading(true)
        verify(createUserProfileView).showLoading(false)
    }

}
