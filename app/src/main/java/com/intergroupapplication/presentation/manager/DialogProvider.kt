package com.intergroupapplication.presentation.manager

import com.intergroupapplication.presentation.base.BaseDialogResolver
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
}
