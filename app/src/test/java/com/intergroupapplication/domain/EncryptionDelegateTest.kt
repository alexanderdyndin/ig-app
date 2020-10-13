package com.intergroupapplication.domain

import com.intergroupapplication.domain.crypto.EncryptionDelegate
import com.intergroupapplication.domain.crypto.Encryptor
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Created by abakarmagomedov on 20/08/2018 at project InterGroupApplication.
 */
@RunWith(MockitoJUnitRunner::class)
class EncryptionDelegateTest {

    private lateinit var encryptorDelegate: Encryptor

    @Before
    fun setUp() {
        encryptorDelegate = EncryptionDelegate()
    }

    @Test
    fun shouldEncryptString() {
        assert(encryptorDelegate.encryptSha224Hex(FakeData.STRING_TO_ENCRYPT) != FakeData.STRING_TO_ENCRYPT)
    }

}
