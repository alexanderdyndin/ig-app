package com.intergroupapplication.presentation.provider

import com.intergroupapplication.data.model.ProgressMediaModel
import com.intergroupapplication.presentation.base.BaseDialogResolver
import com.intergroupapplication.presentation.dialogs.PreviewDialog
import com.intergroupapplication.presentation.dialogs.progress.view.ProgressDialog
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 02/08/2018 at project InterGroupApplication.
 */
class DialogProvider @Inject constructor() {

    fun newDialog(dialogLayout: Int, actionsMap: Map<Int, () -> Unit>): BaseDialogResolver {
        val fragment = BaseDialogResolver()
        fragment.actionsMap = actionsMap
        fragment.dialogLayout = dialogLayout
        return fragment
    }

    fun newPreviewDialog(isPhoto:Boolean,url:String,isChoose:Boolean, previewVideo:String): PreviewDialog {
        return PreviewDialog().apply {
            this.url = url
            this.isChoose = isChoose
            this.isPhoto = isPhoto
            this.previewVideo = previewVideo
        }
    }

    fun newProgressDialog():ProgressDialog = ProgressDialog()
}
