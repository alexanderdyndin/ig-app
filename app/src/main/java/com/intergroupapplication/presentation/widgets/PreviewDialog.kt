package com.intergroupapplication.presentation.widgets

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.intergroupapplication.R
import com.intergroupapplication.databinding.DialogPreviewBinding
import com.intergroupapplication.presentation.exstension.setResult
import com.intergroupapplication.presentation.exstension.show

class PreviewDialog : DialogFragment() {


    companion object {
        const val ADD_REQUEST_CODE = "add_request_code"
        const val ADD_URI_KEY = "add_uri_key"
        const val IS_PHOTO_KEY = "is_photo_key"
        const val IS_CHOOSE_KEY = "is_choose_key"
        const val VIDEO_PREVIEW_KEY = "video_preview_key"
    }

    private lateinit var videoView: VideoView
    private lateinit var gestureDetector: GestureDetector
    private val previewViewBinding by viewBinding(DialogPreviewBinding::bind)
    var url: String = ""
    var isPhoto = false
    var isChoose = false
    var urlPreview:String = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(previewViewBinding) {
            videoView = videoPlayer
            setupAllView()
        }
    }

    private fun setupAllView() {
        if (isPhoto) {
            setupImageView()
        } else {
            setupVideoView()
        }
        setupClosePreview()
        setupAddButton()
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setupImageView() {
        val imageView = previewViewBinding.imagePreview
        imageView.setImageURI(Uri.parse(url))
        imageView.show()
        imageView.setOnClickListener {
            this@PreviewDialog.dismiss()
        }
        imageView.setOnTouchListener { _, event ->
            return@setOnTouchListener gestureDetector.onTouchEvent(event)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupVideoView() {
        videoView.setVideoPath(url)
        videoView.requestFocus(0)
        videoView.setMediaController(MediaController(context))
        videoView.show()
        videoView.start()
        videoView.setOnTouchListener { _, event ->
            return@setOnTouchListener gestureDetector.onTouchEvent(event)
        }
    }


    private fun setupAddButton() {
        val addButton = previewViewBinding.addButton
        addButton.isActivated = isChoose
        addButton.setOnClickListener {
            isChoose = !it.isActivated
            it.isActivated = isChoose
            parentFragmentManager.setResult(
                ADD_REQUEST_CODE,
                    ADD_URI_KEY to url, IS_PHOTO_KEY to isPhoto,
                    IS_CHOOSE_KEY to isChoose, VIDEO_PREVIEW_KEY to urlPreview
            )
        }
    }

    private fun setupClosePreview() {
        val closePreview = previewViewBinding.closePreview
        closePreview.setOnClickListener {
            this@PreviewDialog.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        videoView.pause()
    }
}