package com.intergroupapplication.domain

import com.intergroupapplication.domain.entity.UserRole
import com.intergroupapplication.domain.gateway.UserProfileGateway
import com.intergroupapplication.domain.usecase.GroupUseCase
import com.intergroupapplication.testingutils.RxSchedulesRule
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Created by abakarmagomedov on 18/09/2018 at project InterGroupApplication.
 */
@RunWith(MockitoJUnitRunner::class)
class GroupUseCaseTest {
    @get:Rule
    val schedulerRule = RxSchedulesRule()

    private lateinit var groupUseCase: GroupUseCase
    private val userProfileGateway: UserProfileGateway = mock()

    @Before
    fun setUp() {
        groupUseCase = GroupUseCase(userProfileGateway)
    }

    @Test
    fun shouldReturnUserAdminRole() {
        whenever(userProfileGateway.getUserProfile())
                .thenReturn(Single.just(FakeData.getUserAdminRole()))
        groupUseCase.getUserRole(FakeData.getGroupForUserAdmin())
                .test()
                .assertOf {
                    it.assertNoErrors()
                    it.assertValue(UserRole.ADMIN)
                }
    }

    @Test
    fun shouldReturnUserFollowerRole() {
        whenever(userProfileGateway.getUserProfile())
                .thenReturn(Single.just(FakeData.getUserEntity()))
        groupUseCase.getUserRole(FakeData.getGroupForUserFollowing())
                .test()
                .assertOf {
                    it.assertNoErrors()
                    it.assertValue(UserRole.USER_FOLLOWER)
                }
    }

    @Test
    fun shouldReturnUserNotFollowerRole() {
        whenever(userProfileGateway.getUserProfile())
                .thenReturn(Single.just(FakeData.getUserEntity()))
        groupUseCase.getUserRole(FakeData.getGroupForUserNotFollowing())
                .test()
                .assertOf {
                    it.assertNoErrors()
                    it.assertValue(UserRole.USER_NOT_FOLLOWER)
                }
    }
}
