package com.intergroupapplication.data.mapper

import com.intergroupapplication.data.model.CommentUserModel
import com.intergroupapplication.data.model.UserModel
import com.intergroupapplication.data.model.UserProfileModelRequest
import com.intergroupapplication.data.model.UserProfileModelResponse
import com.intergroupapplication.di.qualifier.DashDateFormatter
import com.intergroupapplication.di.qualifier.PointDateFormatter
import com.intergroupapplication.domain.entity.CommentUserEntity
import com.intergroupapplication.domain.entity.CreateUserEntity
import com.intergroupapplication.domain.entity.UserEntity
import java.text.DateFormat
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 06/08/2018 at project InterGroupApplication.
 */
class UserProfileMapper @Inject constructor(@DashDateFormatter private val dashFormatter: DateFormat,
                                            @PointDateFormatter private val pointFormatter: DateFormat) {

    fun mapToDataModel(from: UserEntity) =
            UserProfileModelRequest(
                    firstName = from.firstName,
                    surName = from.surName,
                    birthday = from.birthday,
                    gender = from.gender,
                    avatar = from.avatar)

    fun mapToDataModel(from: CreateUserEntity) =
            UserProfileModelRequest(
                    firstName = from.firstName,
                    surName = from.surName,
                    birthday = dashFormatter.format(pointFormatter.parse(from.birthday)),
                    gender = from.gender,
                    avatar = from.avatar)

    fun mapToDataModel(from: CommentUserEntity): CommentUserModel =
            CommentUserModel(
                    user = from.user,
                    firstName = from.firstName,
                    secondName = from.secondName,
                    birthday = from.birthday,
                    gender = from.gender,
                    avatar = from.avatar
            )

    fun mapToDomainEntity(from: CommentUserModel?): CommentUserEntity? =
            from?.let {
                CommentUserEntity(
                        user = from.user,
                        firstName = from.firstName,
                        secondName = from.secondName,
                        birthday = from.birthday,
                        gender = from.gender,
                        avatar = from.avatar
                )
            } ?: let { null }


    fun mapToDomainEntity(from: UserProfileModelResponse) =
            UserEntity(
                    id = from.userModel.id,
                    firstName = from.firstName,
                    surName = from.surName,
                    birthday = from.birthday,
                    gender = from.gender,
                    email = from.userModel.email,
                    isBlocked = from.userModel.isBlocked,
                    isActive = from.userModel.isActive,
                    avatar = from.avatar)

}
