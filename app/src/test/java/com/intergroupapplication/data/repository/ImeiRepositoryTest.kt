package com.intergroupapplication.data.repository

import com.intergroupapplication.data.session.UserSession
import com.intergroupapplication.domain.crypto.Encryptor
import com.intergroupapplication.domain.gateway.ImeiGateway
import com.intergroupapplication.presentation.manager.PhoneCharacteristicManager
import com.nhaarman.mockito_kotlin.mock
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Created by abakarmagomedov on 20/08/2018 at project InterGroupApplication.
 */
@RunWith(MockitoJUnitRunner::class)
class ImeiRepositoryTest {

    private lateinit var imeiRepository: ImeiGateway
    private val infoManager: PhoneCharacteristicManager = mock()
    private val session: UserSession = mock()
    private val encryptor: Encryptor = mock()

    @Before
    fun setUp() {
        imeiRepository = ImeiRepository(infoManager, session, encryptor)
    }

    @Test
    fun mockTest() {
        assertEquals(4, 2 + 2)
    }

}
