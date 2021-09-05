package com.intergroupapplication.data.model

import android.os.Parcelable
import com.intergroupapplication.presentation.feature.commentsbottomsheet.presenter.CommentBottomSheetPresenter
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class CreateCommentDataModel(
    val textComment: String,
    var commentBottomSheetPresenter: @RawValue CommentBottomSheetPresenter,
    val finalNameMedia: List<String>
) : Parcelable