package com.intergroupapplication.data.repository

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.intergroupapplication.data.model.AudioInAddFileModel
import com.intergroupapplication.data.model.GalleryModel
import com.intergroupapplication.data.model.VideoModel
import com.intergroupapplication.domain.gateway.AddLocalMediaGateway
import javax.inject.Inject

class AddLocalMediaRepository@Inject constructor(private val context: Context):AddLocalMediaGateway {

    override fun addGalleryUri(): MutableList<GalleryModel> {
        val listUrlImage = mutableListOf<GalleryModel>()
        val mediaConstants = arrayOf(MediaStore.Images.Media.DATA,
                if(Build.VERSION.SDK_INT > 28) MediaStore.Images.Media.DATE_MODIFIED else MediaStore.Images.Media.DATE_TAKEN)
        try {
            val cursor = context?.contentResolver?.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    mediaConstants,
                    null,
                    null,
                    null)
            val size: Int = cursor?.count ?: 0
            if (size != 0) {
                while (cursor?.moveToNext() == true) {
                    val fileColumnIndex: Int = cursor.getColumnIndexOrThrow(mediaConstants[0])
                    val dateIndex: Int = cursor.getColumnIndexOrThrow(mediaConstants[1])
                    val path: String = cursor.getString(fileColumnIndex)
                    val date = cursor.getLong(dateIndex)
                    listUrlImage.add(GalleryModel(path,date, false))
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        listUrlImage.sortByDescending { it.date }
        return listUrlImage
    }

    override fun addVideoUri(): MutableList<VideoModel> {
        val listUrlVideo = mutableListOf<VideoModel>()
        val mediaConstants = arrayOf(
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DURATION,
                if(Build.VERSION.SDK_INT > 28) MediaStore.Video.Media.DATE_MODIFIED else MediaStore.Video.Media.DATE_TAKEN
        )
        try {
            val cursor = context.contentResolver?.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    mediaConstants,
                    null,
                    null,
                    null)
            val size: Int = cursor?.count ?: 0
            if (size != 0) {
                while (cursor?.moveToNext() == true) {
                    val fileColumnIndex: Int = cursor.getColumnIndexOrThrow(mediaConstants[0])
                    val path: String = cursor.getString(fileColumnIndex)
                    val duration = cursor.getLong(cursor.getColumnIndexOrThrow(mediaConstants[1]))
                    val dateIndex =  cursor.getColumnIndex(mediaConstants[2])
                    val seconds = (duration/1000)%60
                    val minutes = ((duration/1000)/60)%60
                    val hours:Int = ((duration/1000)/3600).toInt()
                    val minute = if (minutes < 10) "0$minutes" else "$minutes"
                    val second = if (seconds < 10) "0$seconds" else "$seconds"
                    val hour = if(hours == 0) "" else if(hours < 10) "0$hours:" else "$hours:"
                    val date = cursor.getLong(dateIndex)
                    listUrlVideo.add(VideoModel(path, "$hour$minute:$second", date,false))
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        listUrlVideo.sortByDescending { it.date }

        return listUrlVideo
    }

    override fun addAudioUri(): MutableList<AudioInAddFileModel> {
        val listUrlAudio = mutableListOf<AudioInAddFileModel>()
        val mediaConstants = arrayOf(
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST
        )
        try {
            val cursor = context.contentResolver?.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    mediaConstants,
                    null,
                    null,
                    null)
            val size: Int = cursor?.count ?: 0
            if (size != 0) {
                while (cursor?.moveToNext() == true) {
                    val fileColumnIndex: Int = cursor.getColumnIndexOrThrow(mediaConstants[0])
                    val nameColumnIndex: Int = cursor.getColumnIndexOrThrow(mediaConstants[1])
                    val duration = cursor.getLong(cursor.getColumnIndexOrThrow(mediaConstants[2]))
                    val authorColumnIndex = cursor.getColumnIndexOrThrow(mediaConstants[3])
                    val path: String = cursor.getString(fileColumnIndex)
                    val name = (cursor.getString(nameColumnIndex) ?: "").replace(".mp3", "")
                    var author = cursor.getString(authorColumnIndex)?:""
                    if (author.contains("unknown")) author =""
                    else if (name.contains(author)) author = ""
                    val seconds = (duration/1000)%60
                    val minutes = ((duration/1000)/60)%60
                    val hours:Int = ((duration/1000)/3600).toInt()
                    val minute = if (minutes < 10) "0$minutes" else "$minutes"
                    val second = if (seconds < 10) "0$seconds" else "$seconds"
                    val hour = if(hours == 0) "" else if(hours < 10) "0$hours:" else "$hours:"
                    if (name != "")
                        listUrlAudio.add(AudioInAddFileModel(path, name, "$hour$minute:$second",
                                author,false))
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        listUrlAudio.reverse()
        return listUrlAudio
    }
}