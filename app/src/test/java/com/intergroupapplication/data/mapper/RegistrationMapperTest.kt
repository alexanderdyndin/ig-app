package com.intergroupapplication.data.mapper

import com.intergroupapplication.domain.FakeData
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Created by abakarmagomedov on 21/08/2018 at project InterGroupApplication.
 */
@RunWith(MockitoJUnitRunner::class)
class RegistrationMapperTest {

    private lateinit var registrationMapper: RegistrationMapper

    @Before
    fun setUp() {
        registrationMapper = RegistrationMapper()
    }

    @Test
    fun shouldMapRegistrationEntity() {
        val registrationModel = registrationMapper.mapToDataModel(FakeData.getRegistrationEntity())
        Assert.assertEquals(registrationModel, FakeData.getRegistrationModel())
    }

    @Test
    fun shouldMapRegistrationModel() {
        val registrationEntity = registrationMapper.mapToDomainEnity(FakeData.getRegistrationModel())
        Assert.assertEquals(registrationEntity, FakeData.getRegistrationEntity())
    }

}
