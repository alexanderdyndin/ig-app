package com.intergroupapplication.presentation.feature.commentsbottomsheet.view

import android.annotation.SuppressLint
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.intergroupapplication.R
import com.intergroupapplication.data.model.ChooseMedia
import com.intergroupapplication.presentation.exstension.show
import com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter.addChooseMedia
import com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter.chooseMedias
import com.intergroupapplication.presentation.feature.commentsbottomsheet.adapter.removeChooseMedia
import kotlinx.android.synthetic.main.dialog_preview.*
import kotlinx.android.synthetic.main.fragment_group.*
import timber.log.Timber
import kotlin.math.abs

class PreviewDialog:DialogFragment(),GestureDetector.OnGestureListener {


    companion object{
        const val ADD_REQUEST_CODE = "add_request_code"
        const val ADD_URI_KEY = "add_uri_key"
        const val IS_PHOTO_KEY = "is_photo_key"
        const val IS_CHOOSE_KEY = "is_choose_key"
        private const val SWIPE_MIN_DISTANCE = 120
        private const val SWIPE_MAX_OFF_PATH = 250
        private const val SWIPE_THRESHOLD_VELOCITY = 100
    }
    private lateinit var videoView: VideoView
    private lateinit var gestureDetector: GestureDetector
    lateinit var url:String
    var isPhoto = false
    var isChoose = false


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_preview, null, false)
        videoView = view.findViewById(R.id.videoPlayer)
        gestureDetector = GestureDetector(context,this)
        setupAllView(view)
        val builder = AlertDialog.Builder(requireContext())
        return builder.setView(view).create()
    }

   /* override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_preview, container, false)
        gestureDetector = GestureDetector(context,this)
        setupAllView(view)

        return view
    }*/

    private fun setupAllView(view: View) {
        if (isPhoto) {
            setupImageView(view)
        } else {
            setupVideoView()
        }
        setupClosePreview(view)
        setupAddButton(view)
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setupImageView(view: View) {
        val imageView = view.findViewById<ImageView>(R.id.imagePreview)
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


    private fun setupAddButton(view: View) {
        val addButton = view.findViewById<Button>(R.id.addButton)
        addButton.isActivated = isChoose
        addButton.setOnClickListener {
            if (it.isActivated) {
                isChoose = false
                it.isActivated = isChoose
                chooseMedias.removeChooseMedia(url)
                parentFragmentManager.setFragmentResult(ADD_REQUEST_CODE,
                        bundleOf(ADD_URI_KEY to url, IS_PHOTO_KEY to isPhoto,
                                IS_CHOOSE_KEY to isChoose))
            } else {
                isChoose = true
                it.isActivated = isChoose
                chooseMedias.addChooseMedia(ChooseMedia(url))
                parentFragmentManager.setFragmentResult(ADD_REQUEST_CODE,
                        bundleOf(ADD_URI_KEY to url, IS_PHOTO_KEY to isPhoto,
                                IS_CHOOSE_KEY to isChoose))
            }
        }
    }

    private fun setupClosePreview(view: View) {
        val closePreview = view.findViewById<ImageView>(R.id.closePreview)
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

    override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
       return false
    }

    override fun onLongPress(e: MotionEvent?) {

    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        try {
            if (abs(e1.y - e2.y) > SWIPE_MAX_OFF_PATH) return false
            if (e1.x - e2.x > SWIPE_MIN_DISTANCE
                    && abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        //тут плюс position
                Timber.tag("tut_swipe").d("toLeft")
            } else if (e2.x - e1.x > SWIPE_MIN_DISTANCE
                    && abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        //тут минус position
                Timber.tag("tut_swipe").d("toRight")

            }
        } catch (e: Exception) {
            return true
        }
        return true
    }
}