package com.intergroupapplication.presentation.exstension

import android.content.Context
import android.net.Uri
import com.facebook.common.util.UriUtil
import com.facebook.drawee.view.SimpleDraweeView
import com.miguelbcr.ui.rx_paparazzo2.entities.FileData

/**
 * Created by abakarmagomedov on 02/08/2018 at project InterGroupApplication.
 */

fun SimpleDraweeView.loadImageFromFile(fileData: FileData, callerContext: Context) {
    this.setImageURI(Uri.fromFile(fileData.file), callerContext)
}

fun SimpleDraweeView.loadImageFromResources(resId: Int, callerContext: Context) {
    val uri = Uri.Builder()
            .scheme(UriUtil.LOCAL_RESOURCE_SCHEME)
            .path(resId.toString())
            .build()
    this.setImageURI(uri, callerContext)
}
