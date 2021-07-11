package com.intergroupapplication.data.model

import android.os.Parcelable
import com.intergroupapplication.presentation.feature.commentsbottomsheet.presenter.CommentBottomSheetPresenter
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class CreateCommentDataModel(val textComment:String,
    var commentBottomSheetPresenter:  @RawValue CommentBottomSheetPresenter,
   val finalNameMedia:List<String>) :Parcelable