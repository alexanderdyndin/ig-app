package com.intergroupapplication.domain

import com.intergroupapplication.data.model.*
import com.intergroupapplication.domain.entity.*
import com.intergroupapplication.domain.exception.GroupAlreadyExistException
import com.intergroupapplication.domain.exception.InvalidCredentialsException
import java.io.IOException
import java.util.*


/**
 * Created by abakarmagomedov on 02/08/2018 at project InterGroupApplication.
 */
object
FakeData {

    const val CONFIRM_MAIL_CODE = "5321"
    const val STRING_TO_ENCRYPT = "THIS IS SPARTA"
    val invalidCredentialsException = InvalidCredentialsException()
    val groupAlreadyExistsException = GroupAlreadyExistException()
    val ioException = IOException()

//    fun getGroupList(): List<GroupEntity> =
//            Arrays.asList(GroupEntity("1", "15", "Cloud Nine",
//                    "cs go player group", false, "9", true, null),
//                    GroupEntity("2", "20", "Gamers two",
//                            "cs go player group", true, "9", false, null),
//                    GroupEntity("3", "1500", "Natus Wincere",
//                            "cs go player group", false, "15", true, null),
//                    GroupEntity("4", "15", "Cloud Nine",
//                            "cs go player group", false, "9", true, null),
//                    GroupEntity("5", "15", "Cloud Nine",
//                            "cs go player group", false, "9", true, null),
//                    GroupEntity("6", "15", "Cloud Nine",
//                            "cs go player group", false, "9", true, null))

    fun getCommentsList(): MutableList<CommentEntity> =
            Arrays.asList(CommentEntity("0", "New comment", "2018-03-15.45",
                    CommentUserEntity(0, "Fyodor", "Smolov", "2013-03-03", "male", null), "15"),
                    CommentEntity("0", "New comment", "2018-03-15.45",
                            CommentUserEntity(0, "Fyodor", "Smolov", "2013-03-03", "male", null), "15"),
                    CommentEntity("0", "New comment", "2018-03-15.45",
                            CommentUserEntity(0, "Fyodor", "Smolov", "2013-03-03", "male", null), "15"),
                    CommentEntity("0", "New comment", "2018-03-15.45",
                            CommentUserEntity(0, "Fyodor", "Smolov", "2013-03-03", "male", null), "15"),
                    CommentEntity("0", "New comment", "2018-03-15.45",
                            CommentUserEntity(0, "Fyodor", "Smolov", "2013-03-03", "male", null), "15"),
                    CommentEntity("0", "New comment", "2018-03-15.45",
                            CommentUserEntity(0, "Fyodor", "Smolov", "2013-03-03", "male", null), "15"))

    fun getGroupPostsList(): List<GroupPostEntity> =
            Arrays.asList(GroupPostEntity("1", "Простое описание поста",
                    "50", "2018-09-11T10:09:31.111435Z", getGroupInPostEntity(), null),
                    GroupPostEntity("2", "Простое описание поста",
                            "50", "2018-09-11T10:09:31.111435Z", getGroupInPostEntity(), null),
                    GroupPostEntity("3", "Простое описание поста",
                            "50", "2018-09-11T10:09:31.111435Z", getGroupInPostEntity(), null),
                    GroupPostEntity("4", "Простое описание поста",
                            "50", "2018-09-11T10:09:31.111435Z", getGroupInPostEntity(), null),
                    GroupPostEntity("5", "Простое описание поста",
                            "50", "2018-09-11T10:09:31.111435Z", getGroupInPostEntity(), null),
                    GroupPostEntity("6", "Простое описание поста",
                            "50", "2018-09-11T10:09:31.111435Z", getGroupInPostEntity(), null),
                    GroupPostEntity("7", "Простое описание поста",
                            "50", "2018-09-11T10:09:31.111435Z", getGroupInPostEntity(), null))

    fun getGroupInPostEntity() = GroupInPostEntity("1", "Cloud Nine", null)

    fun getUserAdminRole() = UserEntity("1", "dude", "duck", "2018-03-03", "male", "cool@mail.ru",
            false, false, null)

//    fun getGroupForUserAdmin(): GroupEntity = GroupEntity("2", "20", "Gamers two",
//            "cs go player group", false, "1", false, null)
//
//    fun getGroupForUserFollowing(): GroupEntity = GroupEntity("2", "20", "Gamers two",
//            "cs go player group", false, "4", true, null)
//
//    fun getGroupForUserNotFollowing(): GroupEntity = GroupEntity("2", "20", "Gamers two",
//            "cs go player group", false, "5", false, null)
//
//    fun getGroupPostEntity() = GroupPostEntity("1", "Простое описание поста",
//            "50", "2018-09-11T10:09:31.111435Z", getGroupInPostEntity(), null)
//
//
//    fun getGroupEntity(): GroupEntity = GroupEntity("3", "1500", "Natus Wincere",
//            "cs go player group", false, "15", true, null)

    fun getRegistrationEntity(): RegistrationEntity =
            RegistrationEntity("cool@mail.ru", "igjfduhg57hygu5i4","cool@mail.ru", "igjfduhg57hygu5i4")


    fun getRegistrationModel(): RegistrationModel =
            RegistrationModel("cool@mail.ru", "igjfduhg57hygu5i4","cool@mail.ru", "igjfduhg57hygu5i4")


    fun getCreateUserEntity(): CreateUserEntity =
            CreateUserEntity("dude", "duck", "2018-03-03", "male", null)


    fun getLoginEntity(): LoginEntity =
            LoginEntity("temp@mail.ru", "112358")

    fun getAnswerCommentEntity(): CommentEntity = CommentEntity("0", "New comment", "2018-03-15.45",
            CommentUserEntity(0, "Fyodor", "Smolov", "2013-03-03", "male", null), "15")

    fun getCommentEntity(): CommentEntity = CommentEntity("0", "New comment", "2018-03-15.45",
            CommentUserEntity(0, "Fyodor", "Smolov", "2013-03-03", "male", null), null)

    fun getLoginModel(): LoginModel =
            LoginModel("temp@mail.ru", "112358")

    fun getRegistrationResponseModel(): RegistrationResponseModel =
            RegistrationResponseModel(true, "temp@mail.ru")

    fun getTokenConfirmModel(): TokenConfirmModel =
            TokenConfirmModel(CONFIRM_MAIL_CODE)

    fun getUserEntity(): UserEntity =
            UserEntity("0", "dude", "duck", "2018-03-03", "male", "cool@mail.ru",
                    false, false, null)

    fun getCreateCommentEntity(): CreateCommentEntity =
            CreateCommentEntity("My  New Comment")

    fun getTokenEntity(): TokenEntity =
            TokenEntity("gkmfdjksgnsjkgmnfs324234", "fjknguj543gjierjgi9jegomdf")

    fun getTokenModel(): TokenModel =
            TokenModel("gkmfdjksgnsjkgmnfs324234", "fjknguj543gjierjgi9jegomdf")

    fun getCreateGroupEntity(): CreateGroupEntity =
            CreateGroupEntity("Natus Wincere", "Cs go Players", null)

    fun getCreateGroupPostEntity(): CreateGroupPostEntity =
            CreateGroupPostEntity("New post", null)
}

