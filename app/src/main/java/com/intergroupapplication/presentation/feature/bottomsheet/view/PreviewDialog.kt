package com.intergroupapplication.presentation.feature.bottomsheet.view

import android.app.AlertDialog
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import androidx.fragment.app.DialogFragment
import com.intergroupapplication.R
import com.intergroupapplication.presentation.exstension.show

class PreviewDialog:DialogFragment() {

    private lateinit var videoView: VideoView
    lateinit var url:String
    var isPhoto = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_preview,null,false)
        videoView = view.findViewById(R.id.videoPlayer)
        if (isPhoto){
            val imageView = view.findViewById<ImageView>(R.id.imagePreview)
            imageView.setImageURI(Uri.parse(url))
            imageView.show()
        }
        else{
            videoView.setVideoPath(url);
            videoView.setMediaController(MediaController(context));
            videoView.requestFocus(0);
            videoView.show()
            videoView.start()
        }

        val alertDialog = AlertDialog.Builder(context)
        return alertDialog.setView(view).create().apply {
            window?.attributes?.windowAnimations = R.style.DialogTheme
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        videoView.pause()
        videoView.setMediaController(null)
    }
}