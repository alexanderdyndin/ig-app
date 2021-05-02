package com.intergroupapplication.presentation.manager

import com.danikula.videocache.HttpProxyCacheServer
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.base.BaseDialogResolver
import com.intergroupapplication.presentation.feature.bottomsheet.view.PreviewDialog
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

    fun newPreviewDialog(isPhoto:Boolean,url: String):PreviewDialog{
        val dialog = PreviewDialog()
        dialog.isPhoto = isPhoto
        dialog.url = url
        return dialog
    }
}
