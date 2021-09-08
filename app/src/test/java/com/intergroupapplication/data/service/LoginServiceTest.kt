package com.intergroupapplication.data.service

import com.intergroupapplication.data.mappers.LoginMapper
import com.intergroupapplication.data.mappers.TokenMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.FakeData
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Created by abakarmagomedov on 23/08/2018 at project InterGroupApplication.
 */
@RunWith(MockitoJUnitRunner::class)
class LoginServiceTest {

    private lateinit var loginService: LoginService
    private val api: AppApi = mock()
    private val loginMapper = LoginMapper()
    private val tokenMapper = TokenMapper()
    private val sessionStorage: UserSession = mock()

    @Before
    fun setUp() {
        loginService = LoginService(api, loginMapper, tokenMapper, sessionStorage)
    }

    @Test
    fun shouldReturnTokenEntity() {
        whenever(api.loginUser(FakeData.getLoginModel())).thenReturn(Single.fromCallable
        { FakeData.getTokenModel() })

        loginService.performLogin(FakeData.getLoginEntity())
                .test()
                .assertNoErrors()
                .assertComplete()
                .assertValue(FakeData.getTokenEntity())
    }

}
