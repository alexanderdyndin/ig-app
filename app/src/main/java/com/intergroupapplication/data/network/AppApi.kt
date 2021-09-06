package com.intergroupapplication.data.network

import com.intergroupapplication.data.model.CreateCommentModel
import com.intergroupapplication.data.model.RegistrationResponse
import com.intergroupapplication.data.model.groupfollowers.GroupBanDto
import com.intergroupapplication.data.model.groupfollowers.UpdateGroupAdminDto
import com.intergroupapplication.data.network.dto.*
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.*

const val PAGE_SIZE = 20

interface AppApi {

    @POST("auth/registrations/")
    fun registerUser(@Body registrationDto: RegistrationDto): Single<RegistrationResponse>

    @POST("auth/login/")
    fun loginUser(@Body loginDto: LoginDto): Single<TokenAccessDto>

    @POST("auth/authorize/")
    fun confirmMail(@Body tokenConfirmDto: TokenConfirmDto): Single<TokenAccessDto>

    @POST("users/profiles/")
    fun createUserProfile(@Body userProfileDto: UserProfileDto)
            : Single<UserProfileResponseDto>

    @POST("groups/")
    fun createGroup(@Body createGroupDto: CreateGroupDto): Single<GroupDto>

    @GET("groups/{id}/")
    fun getGroupInformation(@Path("id") groupId: String): Single<GroupDto>

    @GET("groups/{group_pk}/posts/")
    fun getGroupPosts(@Path("group_pk") groupId: String, @Query("page") page: Int)
            : Single<GroupPostsDto>

    @GET("groups/news/")
    fun getNews(@Query("page") page: Int): Single<NewsDto>

    @GET("multimedia/audios/")
    fun getAudios(@Query("page") page: Int): Single<AudiosDto>

    @GET("multimedia/images/")
    fun getImages(@Query("page") page: Int): Single<ImagesDto>

    @GET("multimedia/videos/")
    fun getVideos(@Query("page") page: Int): Single<VideosDto>

    @GET("groups/")
    fun getSubscribedGroupList(
        @Query("page") page: Int, @Query("search") search: String,
        @Query("followed") followed: String = "true"/*,
                               @Query("ordering") ordering:String = "likes"*/
    ): Single<GroupsDto>

    @GET("groups/")
    fun getGroupList(
        @Query("page") page: Int,
        @Query("search") search: String/*,
                               @Query("ordering") ordering:String = "likes"*/
    ): Single<GroupsDto>

    @GET("groups/")
    fun getAdminGroupList(
        @Query("page") page: Int, @Query("search") search: String,
        @Query("owned") owned: String = "true"/*,
                               @Query("ordering") ordering:String = "likes"*/
    ): Single<GroupsDto>

    @GET("groups/posts/{post_pk}/comments/")
    fun getPostComments(
        @Path("post_pk") postId: String,
        @Query("page") page: Int
    ): Single<CommentsDto>

    @POST("groups/posts/{post_pk}/comments/")
    fun createComment(
        @Path("post_pk") postId: String,
        @Body createCommentModel: CreateCommentModel
    ): Single<CommentDto>

    @POST("groups/comments/{comment_pk}/answers/")
    fun createAnswerToComment(
        @Path("comment_pk") answerToCommentId: String,
        @Body createCommentModel: CreateCommentModel
    ): Single<CommentDto>

    @POST("groups/{group_pk}/posts/")
    fun createPost(
        @Body postDto: CreateGroupPostDto,
        @Path("group_pk") groupId: String
    ): Single<GroupPostDto>

    @POST("groups/posts/{post_pk}/reacts/")
    fun setReact(
        @Body data: ReactsRequestDto,
        @Path("post_pk") postId: String
    ): Single<ReactsDto>

    @GET("groups/posts/{id}/")
    fun getPostById(@Path("id") postId: String): Single<GroupPostDto>

    @PUT("groups/posts/{id}/")
    fun editPostById(
        @Path("id") postId: String,
        @Body createGroupPostDto: CreateGroupPostDto
    ): Single<GroupPostDto>

    @GET("users/profiles/me/")
    fun getUserProfile(): Single<UserProfileResponseDto>

    @POST("groups/follows/")
    fun followGroup(@Body followGroupDto: FollowGroupDto): Completable

    @POST("auth/app_status/")
    fun getAppStatus(@Body versionDto: VersionDto): Single<String>

    @DELETE("groups/follows/{group_id}/")
    fun unfollowGroup(@Path("group_id") groupId: String): Completable

    @GET("groups/follows/{group_id}/")
    fun followersGroup(@Path("group_id") groupId: String): Single<GroupFollowDto>

    @GET("s3/groups/posts/")
    fun uploadPostsMedia(
        @Query("ext") imageExt: String,
        @Query("id") groupId: String? = null
    ): Single<ImageUploadDto>

    @GET("s3/users/avatars/")
    fun uploadUserAvatar(
        @Query("ext") imageExt: String,
        @Query("id") groupId: String? = null
    ): Single<ImageUploadDto>

    @GET("s3/groups/avatars/")
    fun uploadGroupAvatar(
        @Query("ext") imageExt: String,
        @Query("id") groupId: String? = null
    ): Single<ImageUploadDto>

    @GET("s3/groups/comments/")
    fun uploadCommentsMedia(
        @Query("ext") imageExt: String,
        @Query("id") postId: String? = null
    ): Single<ImageUploadDto>

    @PATCH("users/profiles/{user-id}/")
    fun changeUserAvatar(@Path("user-id") userId: String, @Body avatar: UpdateAvatarDto)
            : Single<UserProfileResponseDto>

    @PATCH("groups/{id}/")
    fun changeGroupAvatar(
        @Path("id") groupId: String,
        @Body avatar: UpdateAvatarDto
    ): Single<GroupDto>

    @POST("users/push_token/")
    fun updateToken(@Body deviceDto: DeviceDto): Completable

    @GET("auth/device_status/")
    fun isBlocked(): Completable

    @POST("auth/registrations/resend/")
    fun resendCode(): Completable

    @POST("users/password_reset/")
    fun resetPassword(@Body emailDto: EmailDto): Completable

    @POST("users/password_reset/code/")
    fun resetPasswordCode(@Body codeDto: CodeDto): Single<TokenDto>

    @POST("users/password_reset/new_password/")
    fun resetPasswordNewPassword(@Body newPasswordDto: NewPasswordDto): Completable

    @POST("complaints/")
    fun complaints(@Body complaintDto: ComplaintDto): Completable

    @GET("admin/advertisement/")
    fun adCountInfo(): Single<AdDto>

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
        @Body groupBanDto: GroupBanDto
    ): Completable

    @DELETE("groups/bans/{id}/")
    fun deleteUserFromBansGroup(
        @Path("id") id: String
    ): Completable

    @PATCH("groups/followers/{id}/")
    fun updateGroupAdmin(
        @Path("id") id: String,
        @Body updateGroupAdminDto: UpdateGroupAdminDto
    ): Completable

    @DELETE("groups/posts/{id}/")
    fun deleteGroupPost(@Path("id") postId: String): Completable

    @DELETE("groups/news/{id}/")
    fun deleteNewsPost(@Path("id") postId: String): Completable

    @DELETE("groups/comments/{id}/")
    fun deleteComment(@Path("id") commentId: String): Completable

    @POST("groups/comments/{comment_pk}/reacts/")
    fun setCommentReact(
        @Body data: ReactsRequestDto,
        @Path("comment_pk") commentId: String
    ): Single<ReactsDto>

    @POST("groups/bells/")
    fun setBell(@Body bellFollowDto: BellFollowDto): Single<BellFollowDto>

    @GET("groups/bells/{post__id}/")
    fun getBell(@Path("post__id") postId: String): Single<BellFollowDto>

    @DELETE("groups/bells/{post__id}/")
    fun deleteBell(@Path("post__id") postId: String): Completable
}
