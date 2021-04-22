package com.intergroupapplication.presentation.feature.commentsdetails.view

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.intergroupapplication.R
import com.intergroupapplication.presentation.delegate.ImageLoadingDelegate
import com.intergroupapplication.presentation.feature.commentsdetails.adapter.MediaAdapter
import kotlinx.android.synthetic.main.fragment_bottom_sheet.*


class BottomSheetFragment(val imageLoadingDelegate: ImageLoadingDelegate): Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bottom_sheet,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mediaAdapter = MediaAdapter(imageLoadingDelegate)
        val listUrlImage = mutableListOf<String>()
        try {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = activity?.contentResolver?.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection,  // Which columns to return
                    null,  // Return all rows
                    null,
                    null)
            val size: Int = cursor?.count ?:0
            if (size!=0) {
                while (cursor?.moveToNext() == true) {
                    val fileColumnIndex: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    val path: String = cursor.getString(fileColumnIndex)
                    listUrlImage.add(path)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        listUrlImage.reverse()
        mediaAdapter.list.addAll(listUrlImage)
        mediaRecyclerView.apply {
            adapter = mediaAdapter
            layoutManager = GridLayoutManager(context,3)
        }
    }

    fun getRecView() = mediaRecyclerView

        /*fun changeState(state: Int){
            when(state){
                BottomSheetBehavior.STATE_EXPANDED -> {
                    panelAddFile.visibility = View.GONE
                    iconPanel.visibility = View.GONE
                    oldStateBottomSheet = State.EXPANDED
                }
                BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    panelAddFile.visibility = View.VISIBLE
                    iconPanel.visibility = View.VISIBLE
                    oldStateBottomSheet = State.HALF_EXPANDED
                }
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    oldStateBottomSheet = State.COLLAPSED
                }
                BottomSheetBehavior.STATE_HIDDEN -> {
                    Timber.tag("tut_state").d("STATE HIDDEN")
                }
                BottomSheetBehavior.STATE_DRAGGING -> {
                    Timber.tag("tut_state").d("STATE DRAGGING")
                }
                BottomSheetBehavior.STATE_SETTLING -> {
                    Timber.tag("tut_state").d("STATE SETTLING")
                }
            }
        }*/

    enum class State{
        EXPANDED,
        HALF_EXPANDED,
        COLLAPSED
    }
}