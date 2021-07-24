package com.intergroupapplication.presentation.widgets.progress.view

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.data.model.ProgressMediaModel
import com.intergroupapplication.presentation.base.BaseBottomSheetFragment
import com.intergroupapplication.presentation.widgets.progress.adapter.MediaProgressAdapter
import com.intergroupapplication.presentation.exstension.setResult

class ProgressDialog:DialogFragment(), MediaProgressAdapter.ProgressCallback {

    companion object{
        const val CALLBACK_METHOD_KEY = "callback_method_key"
        const val METHOD_KEY = "method_key"
        const val DATA_KEY = "data_key"
        const val RETRY_LOADING_CODE = 0
        const val CANCEL_UPLOADING_CODE = 1
        const val REMOVE_CONTENT_CODE = 2
        const val REMOVE_ALL_CONTENT_CODE = 3
    }

    private val mediaProgressAdapter by lazy { MediaProgressAdapter().apply {
            callback = this@ProgressDialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_progress,null)
        parentFragmentManager.setFragmentResultListener(BaseBottomSheetFragment.PROGRESS_KEY
            ,this) {_,result ->
            result.getParcelable<ProgressMediaModel>(BaseBottomSheetFragment.PROGRESS_MODEL_KEY)
                ?.let {notifyAdapter(it)}
        }
        val recyclerView = view.findViewById<RecyclerView>(R.id.progress_media)
        recyclerView.run {
            adapter = mediaProgressAdapter
            layoutManager = LinearLayoutManager(context)
        }
        val deleteAll = view.findViewById<Button>(R.id.delete_all)
        deleteAll.setOnClickListener {
            parentFragmentManager.setResult(
                CALLBACK_METHOD_KEY,
                METHOD_KEY to REMOVE_ALL_CONTENT_CODE, DATA_KEY to mediaProgressAdapter
                .progressMedia)
            mediaProgressAdapter.run {
                progressMedia.clear()
                notifyDataSetChanged()
            }
        }
        return AlertDialog.Builder(requireContext()).apply {
            setPositiveButton(context.getString(R.string.close)) { _,_ ->
                dismiss()
            }
            setView(view)
            isCancelable = false
        }.create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaProgressAdapter.callback = null
    }

    private fun notifyAdapter(data: ProgressMediaModel){
        mediaProgressAdapter.run {
            notifyItemChanged(progressMedia.addProgressModel(data))
        }
    }

    private fun MutableList<ProgressMediaModel>.addProgressModel(data: ProgressMediaModel):Int{
        forEachIndexed { index,progressModel ->
            if (progressModel.chooseMedia.url == data.chooseMedia.url){
                this[index] = data
                return index
            }
        }
        add(data)
        return size - 1
    }

    private fun MutableList<ProgressMediaModel>.removeProgressModel(url:String):Int?{
        forEachIndexed { index, progressMediaModel ->
            if (progressMediaModel.chooseMedia.url == url){
                this.remove(progressMediaModel)
                return index
            }
        }
        return null
    }

    override fun retryLoading(chooseMedia: ChooseMedia) {
        parentFragmentManager.setResult(
            CALLBACK_METHOD_KEY,
            METHOD_KEY to RETRY_LOADING_CODE, DATA_KEY to chooseMedia)
    }

    override fun cancelUploading(chooseMedia: ChooseMedia) {
        parentFragmentManager.setResult(
            CALLBACK_METHOD_KEY,
            METHOD_KEY to CANCEL_UPLOADING_CODE, DATA_KEY to chooseMedia)
        mediaProgressAdapter.run {
            progressMedia.removeProgressModel(chooseMedia.url)?.let { notifyItemRemoved(it) }
        }
    }

    override fun removeContent(chooseMedia: ChooseMedia) {
        parentFragmentManager.setResult(
            CALLBACK_METHOD_KEY,
            METHOD_KEY to REMOVE_CONTENT_CODE, DATA_KEY to chooseMedia)
        mediaProgressAdapter.run {
            progressMedia.removeProgressModel(chooseMedia.url)?.let { notifyItemRemoved(it) }
        }
    }

}