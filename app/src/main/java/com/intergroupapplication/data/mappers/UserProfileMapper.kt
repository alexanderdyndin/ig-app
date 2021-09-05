package com.intergroupapplication.data.mappers

import com.intergroupapplication.data.model.*
import com.intergroupapplication.di.qualifier.DashDateFormatter
import com.intergroupapplication.di.qualifier.PointDateFormatter
import com.intergroupapplication.domain.entity.*
import java.text.DateFormat
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 06/08/2018 at project InterGroupApplication.
 */
class UserProfileMapper @Inject constructor(
    @DashDateFormatter private val dashFormatter: DateFormat,
    @PointDateFormatter private val pointFormatter: DateFormat
) {

    fun mapToDataModel(from: UserEntity) =
        UserProfileModelRequest(
            firstName = from.firstName,
            surName = from.surName,
            birthday = from.birthday,
            gender = from.gender,
            avatar = from.avatar
        )

    fun mapToDataModel(from: UserProfileEntityRequest) =
        UserProfileModelRequest(
            firstName = from.firstName,
            surName = from.surName,
            birthday = from.birthday,
            gender = from.gender,
            avatar = from.avatar
        )

    fun mapToDomainEntity(from: UserProfileModelRequest) =
        UserProfileEntityRequest(
            firstName = from.firstName,
            surName = from.surName,
            birthday = from.birthday,
            gender = from.gender,
            avatar = from.avatar
        )

    fun mapToDataModel(from: AuthorEntity) =
        AuthorModel(
            from.id,
            from.email,
            from.isBlocked,
            from.isVerified,
            from.timeBlocked,
            mapToDataModel(from.profile)
        )

    fun mapToDomainEntity(from: AuthorModel) =
        AuthorEntity(
            from.id,
            from.email,
            from.isBlocked,
            from.isVerified,
            from.timeBlocked,
            mapToDomainEntity(from.profile)
        )

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun mapToDataModel(from: CreateUserEntity) =
        UserProfileModelRequest(
            firstName = from.firstName,
            surName = from.surName,
            birthday = dashFormatter.format(pointFormatter.parse(from.birthday)),
            gender = from.gender,
            avatar = from.avatar
        )

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
            avatar = from.avatar
        )

    fun mapToDomainEntity(from: AdModel) =
        AdEntity(
            from.limitOfAdsGroups,
            from.firstAdIndexGroups,
            from.noOfDataBetweenAdsGroups,
            from.limitOfAdsNews,
            from.firstAdIndexNews,
            from.noOfDataBetweenAdsNews,
            from.limitOfAdsComments,
            from.firstAdIndexComments,
            from.noOfDataBetweenAdsComments
        )

    fun mapToDataModel(from: AdEntity) =
        AdModel(
            from.limitOfAdsGroups,
            from.firstAdIndexGroups,
            from.noOfDataBetweenAdsGroups,
            from.limitOfAdsNews,
            from.firstAdIndexNews,
            from.noOfDataBetweenAdsNews,
            from.limitOfAdsComments,
            from.firstAdIndexComments,
            from.noOfDataBetweenAdsComments
        )
}
