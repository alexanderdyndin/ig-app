package com.intergroupapplication.presentation

import com.intergroupapplication.domain.FakeData
import com.intergroupapplication.domain.gateway.CreateGroupGateway
import com.intergroupapplication.domain.gateway.PhotoGateway
import com.intergroupapplication.presentation.base.ImageUploader
import com.intergroupapplication.presentation.feature.creategroup.presenter.CreateGroupPresenter
import com.intergroupapplication.presentation.feature.creategroup.view.CreateGroupView
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
class CreateGroupPresenterTest {
    @get:Rule
    val schedulerRule = RxSchedulesRule()

    private lateinit var createGroupPresenter: CreateGroupPresenter
    //private val router: Router = mock()
    private val imageUploader: ImageUploader = mock()
    private val createGroupGateway: CreateGroupGateway = mock()
    private val errorHandler: ErrorHandler = spy(ErrorHandler.defaultErrorHandler())
    private val createGroupView: CreateGroupView = mock()

    @Before
    fun setUp() {
     //   createGroupPresenter = CreateGroupPresenter(router, imageUploader, createGroupGateway,
       //         errorHandler)
        createGroupPresenter.attachView(createGroupView)
    }

    @Test
    fun shouldCreateGroupSuccessfully() {
        whenever(createGroupGateway.createGroup(FakeData.getCreateGroupEntity()))
                .thenReturn(Single.just(FakeData.getGroupEntity()))
        createGroupPresenter.createGroup("Natus Wincere", "Cs go Players",
                "no subject","no rules", false,"12+")
     //   verify(router).exit()
    }

    @Test
    fun shouldHandleGroupAlreadyExistError() {
        whenever(createGroupGateway.createGroup(FakeData.getCreateGroupEntity()))
                .thenReturn(Single.error(FakeData.groupAlreadyExistsException))
        createGroupPresenter.createGroup("Natus Wincere", "Cs go Players",
                "no subject","no rules", false,"12+")
        verify(errorHandler).handle(FakeData.groupAlreadyExistsException)
       // verify(router, times(0)).exit()
    }
}