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

    fun getGroupList(): List<GroupEntity.Group> =
            Arrays.asList(GroupEntity.Group("1", "15", "0",
                    "0", "0", "9", "",
                    "cs go player group","Cloud Nine",false,
                    "1",true,null,"no subject","no rules",
                    false,"12+"),
                    GroupEntity.Group("1", "15", "1",
                            "0", "0", "9", "",
                            "cs go player group","Cloud Nine",false,
                            "2",true,null,"no subject","no rules",
                            false,"12+"),
                    GroupEntity.Group("1", "15", "2",
                            "1", "1", "9", "",
                            "cs go player group","Cloud Nine",true,
                            "3",false,null,"no subject","no rules",
                            false,"12+"),
                    GroupEntity.Group("1", "15", "3333",
                            "2", "2", "9", "",
                            "cs go player group","Cloud Nine",false,
                            "4",true,null,"no subject","no rules",
                            true,"12+"),
                    GroupEntity.Group("1", "15", "0",
                            "3", "3", "9", "",
                            "cs go player group","Cloud Nine",false,
                            "5",true,null,"no subject","no rules",
                            false,"16+"),
                    GroupEntity.Group("1", "15", "0",
                            "1110", "44440", "9", "",
                            "cs go player group","Cloud Nine",false,
                            "6",true,null,"no subject","no rules",
                            false,"18+"))

    fun getCommentsList(): MutableList<CommentEntity.Comment> =
            Arrays.asList(CommentEntity.Comment("0", "New comment", "2018-03-15.45",
                    CommentUserEntity(0, "Fyodor", "Smolov", "2013-03-03", "male", null), "15"),
                    CommentEntity.Comment("0", "New comment", "2018-03-15.45",
                            CommentUserEntity(0, "Fyodor", "Smolov", "2013-03-03", "male", null), "15"),
                    CommentEntity.Comment("0", "New comment", "2018-03-15.45",
                            CommentUserEntity(0, "Fyodor", "Smolov", "2013-03-03", "male", null), "15"),
                    CommentEntity.Comment("0", "New comment", "2018-03-15.45",
                            CommentUserEntity(0, "Fyodor", "Smolov", "2013-03-03", "male", null), "15"),
                    CommentEntity.Comment("0", "New comment", "2018-03-15.45",
                            CommentUserEntity(0, "Fyodor", "Smolov", "2013-03-03", "male", null), "15"),
                    CommentEntity.Comment("0", "New comment", "2018-03-15.45",
                            CommentUserEntity(0, "Fyodor", "Smolov", "2013-03-03", "male", null), "15"))

//    fun getGroupPostsList(): List<GroupPostEntity> =
//            Arrays.asList(GroupPostEntity("1", "Простое описание поста",
//                    "50", "2018-09-11T10:09:31.111435Z", getGroupInPostEntity(), null),
//                    GroupPostEntity("2", "Простое описание поста",
//                            "50", "2018-09-11T10:09:31.111435Z", getGroupInPostEntity(), null),
//                    GroupPostEntity("3", "Простое описание поста",
//                            "50", "2018-09-11T10:09:31.111435Z", getGroupInPostEntity(), null),
//                    GroupPostEntity("4", "Простое описание поста",
//                            "50", "2018-09-11T10:09:31.111435Z", getGroupInPostEntity(), null),
//                    GroupPostEntity("5", "Простое описание поста",
//                            "50", "2018-09-11T10:09:31.111435Z", getGroupInPostEntity(), null),
//                    GroupPostEntity("6", "Простое описание поста",
//                            "50", "2018-09-11T10:09:31.111435Z", getGroupInPostEntity(), null),
//                    GroupPostEntity("7", "Простое описание поста",
//                            "50", "2018-09-11T10:09:31.111435Z", getGroupInPostEntity(), null))

    fun getGroupInPostEntity() = GroupInPostEntity("1", "Cloud Nine", null)

    fun getUserAdminRole() = UserEntity("1", "dude", "duck", "2018-03-03", "male", "cool@mail.ru",
            false, false, null)

    fun getGroupForUserAdmin(): GroupEntity.Group = GroupEntity.Group("1", "15", "0",
            "1110", "44440", "9", "",
            "cs go player group","Cloud Nine",false,
            "1",true,null,"no subject","no rules",
            false,"18+")

    fun getGroupForUserFollowing(): GroupEntity.Group = GroupEntity.Group("1", "15", "0",
            "1110", "44440", "9", "",
            "cs go player group","Cloud Nine",false,
            "6",true,null,"no subject","no rules",
            false,"18+")

    fun getGroupForUserNotFollowing(): GroupEntity.Group = GroupEntity.Group("1", "15", "0",
            "1110", "44440", "9", "",
            "cs go player group","Cloud Nine",false,
            "6",false,null,"no subject","no rules",
            false,"18+")

//    fun getGroupPostEntity() = GroupPostEntity("1", "Простое описание поста",
//            "50", "2018-09-11T10:09:31.111435Z", getGroupInPostEntity(), null)


    fun getGroupEntity(): GroupEntity.Group = GroupEntity.Group("1", "15", "2",
            "1", "1", "9", "",
            "cs go player group","Cloud Nine",true,
            "3",false,null,"no subject","no rules",
            false,"12+")

    fun getRegistrationEntity(): RegistrationEntity =
            RegistrationEntity("cool@mail.ru", "igjfduhg57hygu5i4","cool@mail.ru", "igjfduhg57hygu5i4")


    fun getRegistrationModel(): RegistrationModel =
            RegistrationModel("cool@mail.ru", "igjfduhg57hygu5i4","cool@mail.ru", "igjfduhg57hygu5i4")


    fun getCreateUserEntity(): CreateUserEntity =
            CreateUserEntity("dude", "duck", "2018-03-03", "male", null)


    fun getLoginEntity(): LoginEntity =
            LoginEntity("temp@mail.ru", "112358")

    fun getAnswerCommentEntity(): CommentEntity.Comment = CommentEntity.Comment("0", "New comment", "2018-03-15.45",
            CommentUserEntity(0, "Fyodor", "Smolov", "2013-03-03", "male", null), "15")

    fun getCommentEntity(): CommentEntity.Comment = CommentEntity.Comment("0", "New comment", "2018-03-15.45",
            CommentUserEntity(0, "Fyodor", "Smolov", "2013-03-03", "male", null), null)

    fun getLoginModel(): LoginModel =
            LoginModel("temp@mail.ru", "112358")

    fun getRegistrationResponseModel(): RegistrationResponseModel =
            RegistrationResponseModel(true, "temp@mail.ru")

    fun getRegistrationResponse(): RegistrationReesponce =
            RegistrationReesponce( "cool@mail.ru",
                    "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0aWQiOjU3NDk4MjExMiwidHlwZSI6InJlZnJlc2giLCJ1c2VyX2lkIjo2MDAsImlhdCI6MTYwNTA4NzU1NCwiZXhwIjoxNjA3Njc5NTU0fQ.gu8G-CfXhkhyDwkxa4Fmv6kHABrv291oWNUfrl-fIGE",
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0aWQiOjQ1NTMzNDcyMiwidHlwZSI6ImFjY2VzcyIsInVzZXJfaWQiOjYwMCwiaWF0IjoxNjA1MDg3NTU0LCJleHAiOjE2MDUxNzM5NTR9.WJW3YLfcjnb0y3X5zsZt38wgMzmPfY8gJspovRCGUIs")

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
            CreateGroupEntity("Natus Wincere", "Cs go Players", null,
                    "no subject","no rules", false,"12+")

    fun getCreateGroupPostEntity(): CreateGroupPostEntity =
            CreateGroupPostEntity("New post", emptyList(), emptyList(), emptyList(), false, null)
}

