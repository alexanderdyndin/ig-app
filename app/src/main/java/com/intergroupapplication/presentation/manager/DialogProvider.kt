package com.intergroupapplication.presentation.manager

import com.danikula.videocache.HttpProxyCacheServer
import com.intergroupapplication.data.model.GalleryModel
import com.intergroupapplication.data.model.VideoModel
import com.intergroupapplication.domain.entity.FileEntity
import com.intergroupapplication.presentation.base.BaseDialogResolver
import com.intergroupapplication.presentation.feature.bottomsheet.view.PreviewDialog
import com.intergroupapplication.presentation.feature.bottomsheet.view.ViewPagerDialog
import timber.log.Timber
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

   /* fun newPreviewDialog(isPhoto:Boolean,photos:MutableList<GalleryModel>,videos:MutableList<VideoModel>,
        position:Int):ViewPagerDialog{
        val dialog = ViewPagerDialog()
        dialog.isPhoto = isPhoto
        dialog.photos = photos
        dialog.videos = videos
        dialog.position = position
        return dialog
    }*/

    fun newPreviewDialog(isPhoto:Boolean,url:String,isChoose:Boolean):PreviewDialog{
        val dialog = PreviewDialog()
        dialog.url = url
        dialog.isChoose = isChoose
        dialog.isPhoto = isPhoto
        return dialog
    }
}
