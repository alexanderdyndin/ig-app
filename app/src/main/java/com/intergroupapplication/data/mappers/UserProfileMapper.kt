package com.intergroupapplication.data.mappers

import com.intergroupapplication.data.db.entity.AuthorDb
import com.intergroupapplication.data.db.entity.UserProfileDb
import com.intergroupapplication.data.model.CommentUserModel
import com.intergroupapplication.data.network.dto.*
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
        UserProfileDto(
            firstName = from.firstName,
            surName = from.surName,
            birthday = from.birthday,
            gender = from.gender,
            avatar = from.avatar
        )

    fun mapToDataModel(from: UserProfileEntityRequest) =
        UserProfileDto(
            firstName = from.firstName,
            surName = from.surName,
            birthday = from.birthday,
            gender = from.gender,
            avatar = from.avatar
        )

    fun mapToDomainEntity(from: UserProfileDto) =
        UserProfileEntityRequest(
            firstName = from.firstName,
            surName = from.surName,
            birthday = from.birthday,
            gender = from.gender,
            avatar = from.avatar
        )

    private fun mapDbToEntity(from: UserProfileDb) =
        UserProfileEntityRequest(
            firstName = from.firstName,
            surName = from.surName,
            birthday = from.birthday,
            gender = from.gender,
            avatar = from.avatar
        )

    private fun mapDtoToDb(from: UserProfileDto) =
        UserProfileDb(
            firstName = from.firstName,
            surName = from.surName,
            birthday = from.birthday,
            gender = from.gender,
            avatar = from.avatar
        )

    fun mapToDataModel(from: AuthorEntity) =
        AuthorDto(
            from.id,
            from.email,
            from.isBlocked,
            from.isVerified,
            from.timeBlocked,
            mapToDataModel(from.profile)
        )

    fun mapToDomainEntity(from: AuthorDto) =
        AuthorEntity(
            from.id,
            from.email,
            from.isBlocked,
            from.isVerified,
            from.timeBlocked,
            mapToDomainEntity(from.profile)
        )

    fun mapDtoToDb(from: AuthorDto) =
        AuthorDb(
            id = from.id,
            email = from.email,
            isBlocked = from.isBlocked,
            isVerified = from.isVerified,
            timeBlocked = from.timeBlocked,
            profile = mapDtoToDb(from.profile)
        )

    fun mapDbToEntity(from: AuthorDb) =
        AuthorEntity(
            id = from.id,
            email = from.email,
            isBlocked = from.isBlocked,
            isVerified = from.isVerified,
            timeBlocked = from.timeBlocked,
            profile = mapDbToEntity(from.profile)
        )

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun mapToDataModel(from: CreateUserEntity) =
        UserProfileDto(
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

    fun mapToDomainEntity(from: UserProfileResponseDto) =
        UserEntity(
            id = from.userDto.id,
            firstName = from.firstName,
            surName = from.surName,
            birthday = from.birthday,
            gender = from.gender,
            email = from.userDto.email,
            isBlocked = from.userDto.isBlocked,
            isActive = from.userDto.isActive,
            avatar = from.avatar,
            stats = mapToDomainEntity(from.stats)
        )

    fun mapToDomainEntity(from: AdDto) =
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
        AdDto(
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

    fun mapToDataModel(from: StatsEntity): StatsDto =
        StatsDto(
            from.posts,
            from.comments,
            from.likes,
            from.dislikes
        )

    fun mapToDomainEntity(from: StatsDto): StatsEntity =
        StatsEntity(
            from.posts,
            from.comments,
            from.likes,
            from.dislikes
        )
}
