package com.intergroupapplication.presentation.dialogs.progress.view

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ProgressMediaModel
import com.intergroupapplication.presentation.base.BaseBottomSheetFragment
import com.intergroupapplication.presentation.dialogs.progress.adapter.MediaProgressAdapter
import timber.log.Timber

class ProgressDialog:DialogFragment() {

    private val mediaProgressAdapter by lazy { MediaProgressAdapter() }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_progress,null)
        parentFragmentManager.setFragmentResultListener(BaseBottomSheetFragment.PROGRESS_KEY
            ,this) {_,result ->
            result.getParcelable<ProgressMediaModel>(BaseBottomSheetFragment.PROGRESS_MODEL_KEY)
                ?.let {notifyAdapter(it)}
        }
        val recyclerView = view.findViewById<RecyclerView>(R.id.progressMedia)
        recyclerView.run {
            adapter = mediaProgressAdapter
            layoutManager = LinearLayoutManager(context)
        }
        return AlertDialog.Builder(requireContext()).apply {
            setPositiveButton(context.getString(R.string.close)) { _,_ ->
                dismiss()
            }
            setView(view)
            isCancelable = false
        }.create()
    }

    private fun notifyAdapter(data: ProgressMediaModel){
        mediaProgressAdapter.run {
            notifyItemChanged(progressMedia.addProgressModel(data))
            Timber.tag("tut_progressMedia").d(progressMedia.toString())
        }
    }

    private fun MutableList<ProgressMediaModel>.addProgressModel(data: ProgressMediaModel):Int{
        forEachIndexed { index,progressModel ->
            if (progressModel.url == data.url){
                this[index] = data
                return index
            }
        }
        add(data)
        return size - 1
    }
}