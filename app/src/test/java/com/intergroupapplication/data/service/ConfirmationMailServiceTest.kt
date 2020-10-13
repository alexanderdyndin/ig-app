package com.intergroupapplication.data.service

import com.intergroupapplication.data.mapper.TokenMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.FakeData
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
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
class ConfirmationMailServiceTest {

    private lateinit var confirmationMailService: ConfirmationMailService
    private val api: AppApi = mock()
    private val tokenMapper: TokenMapper = TokenMapper()
    private val sessionStorage: UserSession = mock()

    @Before
    fun setUp() {
        confirmationMailService = ConfirmationMailService(api, tokenMapper, sessionStorage)
    }

    @Test
    fun shouldWriteTokenToStorage() {
        whenever(api.confirmMail(FakeData.getTokenConfirmModel()))
                .thenReturn(Single.fromCallable { FakeData.getTokenModel() })

        confirmationMailService.confirmMail(FakeData.CONFIRM_MAIL_CODE)
                .test()
                .assertNoErrors()
                .assertComplete()

        verify(sessionStorage, times(1)).token = FakeData.getTokenEntity()
    }

}
