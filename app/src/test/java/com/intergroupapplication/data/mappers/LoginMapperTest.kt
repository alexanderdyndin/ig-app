package com.intergroupapplication.data.mappers

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
class LoginMapperTest {

    private lateinit var loginMapper: LoginMapper

    @Before
    fun setUp() {
        loginMapper = LoginMapper()
    }

    @Test
    fun shouldMapEntity() {
        val loginModel = loginMapper.mapToDataModel(FakeData.getLoginEntity())
        Assert.assertEquals(loginModel, FakeData.getLoginModel())
    }

    @Test
    fun shouldMapModel() {
        val loginEntity = loginMapper.mapToDomainEntity(FakeData.getLoginModel())
        Assert.assertEquals(loginEntity, FakeData.getLoginEntity())
    }

}
