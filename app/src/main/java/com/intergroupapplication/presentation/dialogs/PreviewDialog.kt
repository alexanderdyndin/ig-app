package com.intergroupapplication.presentation.dialogs

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
import kotlin.math.abs

class PreviewDialog : DialogFragment(), GestureDetector.OnGestureListener {


    companion object {
        const val ADD_REQUEST_CODE = "add_request_code"
        const val ADD_URI_KEY = "add_uri_key"
        const val IS_PHOTO_KEY = "is_photo_key"
        const val IS_CHOOSE_KEY = "is_choose_key"
        const val VIDEO_PREVIEW_KEY = "video_preview_key"
        private const val SWIPE_MIN_DISTANCE = 120
        private const val SWIPE_MAX_OFF_PATH = 250
        private const val SWIPE_THRESHOLD_VELOCITY = 100
    }

    private lateinit var videoView: VideoView
    private lateinit var gestureDetector: GestureDetector
    private val previewViewBinding by viewBinding(DialogPreviewBinding::bind)
    lateinit var url: String
    var isPhoto = false
    var isChoose = false
    var previewVideo:String = ""


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
            gestureDetector = GestureDetector(context, this@PreviewDialog)
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
                    IS_CHOOSE_KEY to isChoose, VIDEO_PREVIEW_KEY to previewVideo
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


    override fun onDown(e: MotionEvent?): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent?) {
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return false
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent?) {

    }

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        try {
            if (abs(e1.y - e2.y) > SWIPE_MAX_OFF_PATH) return false
            if (e1.x - e2.x > SWIPE_MIN_DISTANCE
                && abs(velocityX) > SWIPE_THRESHOLD_VELOCITY
            ) {
                //тут плюс position
            } else if (e2.x - e1.x > SWIPE_MIN_DISTANCE
                && abs(velocityX) > SWIPE_THRESHOLD_VELOCITY
            ) {
                //тут минус position

            }
        } catch (e: Exception) {
            return true
        }
        return true
    }
}