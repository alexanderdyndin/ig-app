package com.intergroupapplication.data.service

import com.intergroupapplication.data.mapper.RegistrationMapper
import com.intergroupapplication.data.network.AppApi
import com.intergroupapplication.domain.FakeData
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Created by abakarmagomedov on 23/08/2018 at project InterGroupApplication.
 */
@RunWith(MockitoJUnitRunner::class)
class RegistrationServiceTest {

    private lateinit var registrationService: RegistrationService
    private val api: AppApi = mock()
    private val registrationMapper = RegistrationMapper()


    @Before
    fun setUp() {
        registrationService = RegistrationService(api, registrationMapper)
    }

    @Test
    fun shouldRegisterUser() {
        whenever(api.registerUser(FakeData.getRegistrationModel()))
                .thenReturn(Single.fromCallable { FakeData.getRegistrationResponseModel() })

        registrationService.performRegistration(FakeData.getRegistrationEntity())
                .test()
                .assertNoErrors()
                .assertComplete()
    }

}
