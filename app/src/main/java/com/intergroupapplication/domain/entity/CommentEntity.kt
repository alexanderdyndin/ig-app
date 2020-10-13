package com.intergroupapplication.domain.entity

/**
 * Created by abakarmagomedov on 28/08/2018 at project InterGroupApplication.
 */
data class CommentEntity(val id: String,
                         val text: String,
                         val date: String,
                         val commentOwner: CommentUserEntity?,
                         val answerTo: String?)
