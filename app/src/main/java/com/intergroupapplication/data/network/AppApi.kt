package com.intergroupapplication.data.network

import com.intergroupapplication.data.model.*
import com.intergroupapplication.data.model.group_followers.GroupBanBody
import com.intergroupapplication.data.model.group_followers.GroupUserBansDto
import com.intergroupapplication.data.model.group_followers.GroupUserFollowersDto
import com.intergroupapplication.data.model.group_followers.UpdateGroupAdmin
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

interface AppApi {

    @POST("auth/registrations/")
    fun registerUser(@Body registrationModel: RegistrationModel): Single<RegistrationReesponce>

    @POST("auth/login/")
    fun loginUser(@Body loginModel: LoginModel): Single<TokenModel>

    @POST("auth/authorize/")
    fun confirmMail(@Body tokenConfirmModel: TokenConfirmModel): Single<TokenModel>

    @POST("users/profiles/")
    fun createUserProfile(@Body userProfileModel: UserProfileModelRequest): Single<UserProfileModelResponse>

    @POST("groups/")
    fun createGroup(@Body createGroupModel: CreateGroupModel): Single<GroupModel>

    @GET("groups/{id}/")
    fun getGroupInformation(@Path("id") groupId: String): Single<GroupModel>

    @GET("groups/{group_pk}/posts/")
    fun getGroupPosts(@Path("group_pk") groupId: String, @Query("page") page: Int): Single<GroupPostsDto>

    @GET("groups/news/")
    fun getNews(@Query("page") page: Int): Single<NewsDto>

    @GET("groups/")
    fun getSubscribedGroupList(@Query("page") page: Int, @Query("search") search:String,
                               @Query("followed") followed:String = "true"): Single<GroupsDto>

    @GET("groups/")
    fun getGroupList(@Query("page") page: Int,
                     @Query("search") search:String): Single<GroupsDto>

    @GET("groups/")
    fun getAdminGroupList(@Query("page") page: Int, @Query("search") search:String,
                          @Query("owned") owned:String = "true"): Single<GroupsDto>

    @GET("groups/posts/{post_pk}/comments/")
    fun getPostComments(@Path("post_pk") postId: String,
                        @Query("page") page: Int): Single<CommentsDto>

    @POST("groups/posts/{post_pk}/comments/")
    fun createComment(@Path("post_pk") postId: String,
                      @Body createCommentModel: CreateCommentModel): Single<CommentModel>

    @POST("groups/comments/{comment_pk}/answers/")
    fun createAnswerToComment(@Path("comment_pk") answerToCommentId: String,
                              @Body createCommentModel: CreateCommentModel): Single<CommentModel>

    @POST("groups/{group_pk}/posts/")
    fun createPost(@Body postModel: CreateGroupPostModel,
                   @Path("group_pk") groupId: String): Single<GroupPostModel>

    @POST("groups/posts/{post_pk}/reacts/")
    fun setReact(@Body data: ReactsModelRequest,
                 @Path("post_pk") postId: String): Single<ReactsModel>

    @GET("groups/posts/{id}/")
    fun getPostById(@Path("id") postId: String): Single<GroupPostModel>

    @PATCH("groups/posts/{id}/")
    fun editPostById(@Path("id") postId: String,
                     @Body createGroupPostModel: CreateGroupPostModel): Single<GroupPostModel>

    @GET("users/profiles/me/")
    fun getUserProfile(): Single<UserProfileModelResponse>

    @POST("groups/follows/")
    fun followGroup(@Body followGroupModel: FollowGroupModel): Completable

    @POST("auth/app_status/")
    fun getAppStatus(@Body versionModel:VersionModel): Single<String>

    @DELETE("groups/follows/{group_id}/")
    fun unfollowGroup(@Path("group_id") groupId: String): Completable

    @GET("groups/follows/{group_id}/")
    fun followersGroup(@Path("group_id") groupId: String): Single<GroupFollowModel>

    @GET("s3/groups/posts/")
    fun uploadPhoto(@Query("ext") imageExt: String, @Query("id") groupId: String? = null): Single<ImageUploadDto>

    @GET("s3/users/")
    fun uploadUserAvatar(@Query("ext") imageExt: String): Single<ImageUploadDto>

    @GET("s3/groups/")
    fun uploadGroupAvatar(@Query("ext") imageExt: String): Single<ImageUploadDto>

    @PATCH("users/profiles/{user-id}/")
    fun changeUserAvatar(@Path("user-id") userId: String, @Body avatar: UpdateAvatarModel): Single<UserProfileModelResponse>

    @PATCH("groups/{id}/")
    fun changeGroupAvatar(@Path("id") groupId: String, @Body avatar: UpdateAvatarModel): Single<GroupModel>

    @POST("users/push_token/")
    fun updateToken(@Body deviceModel: DeviceModel): Completable

    @GET("auth/device_status/")
    fun isBlocked(): Completable

    @POST("auth/registrations/resend/")
    fun resendCode(): Completable

    @POST("users/password_reset/")
    fun resetPassword(@Body emailModel: EmailModel): Completable

    @POST("users/password_reset/code/")
    fun resetPasswordCode(@Body codeModel: CodeModel): Single<TokenDto>

    @POST("users/password_reset/new_password/")
    fun resetPasswordNewpassword(@Body newPasswordModel: NewPasswordModel): Completable

    @POST("complaints/")
    fun complaints(@Body complaintModel: ComplaintModel): Completable

    @GET("admin/advertisement/")
    fun adCountInfo(): Single<AdModel>

    @GET("groups/{group_id}/followers/")
    fun getGroupFollowers(
            @Path("group_id") groupId: String,
            @Query("page") page: Int,
            @Query("filter") filter: String = "",
            @Query("search") search: String
    ): Single<GroupUserFollowersDto>

    @GET("groups/{group_id}/bans/")
    fun getGroupBans(
            @Path("group_id") groupId: String,
            @Query("page") page: Int,
            @Query("search") search: String
    ): Single<GroupUserBansDto>

    @POST("groups/{group_id}/bans/")
    fun banUserInGroup(
            @Path("group_id") groupId: String,
            @Body groupBanBody: GroupBanBody
    ): Completable

    @DELETE("groups/bans/{id}/")
    fun deleteUserFromBansGroup(
            @Path("id") id: String
    ): Completable

    @PATCH("groups/followers/{id}/")
    fun updateGroupAdmin(
            @Path("id") id: String,
            @Body updateGroupAdmin: UpdateGroupAdmin
    ): Completable
}
