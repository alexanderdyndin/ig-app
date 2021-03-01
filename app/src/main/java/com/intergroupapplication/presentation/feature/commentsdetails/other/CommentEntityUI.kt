package com.intergroupapplication.presentation.feature.commentsdetails.other

import com.appodeal.ads.NativeAd
import com.intergroupapplication.domain.entity.*

/**
 * Created by abakarmagomedov on 27/08/2018 at project InterGroupApplication.
 */

sealed class CommentEntityUI {
        data class CommentEntity(val id: String,
                                 val text: String,
                                 val date: String,
                                 val commentOwner: CommentUserEntity?,
                                 val answerTo: String?) : CommentEntityUI()

        data class AdEntity(val position: Int, val nativeAd: NativeAd?) : CommentEntityUI()
}