package com.intergroupapplication.data.repository

import android.content.Context
import android.provider.MediaStore
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.rxjava2.flowable
import com.intergroupapplication.data.remotedatasource.BottomSheetRemoteRXDataSource
import com.intergroupapplication.domain.entity.BottomSheetEntity
import io.reactivex.Flowable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import javax.inject.Inject

class BottomSheetRepository @Inject constructor(private val context: Context) {

    @ExperimentalCoroutinesApi
    fun getMedia(key:Int): Flowable<PagingData<String>> {
        return Pager(
                config = PagingConfig(
                        pageSize = 30,
                        prefetchDistance = 5),
                pagingSourceFactory = { BottomSheetRemoteRXDataSource(createBottomSheetEntity(context, key),null) }
        ).flowable
    }

    fun createBottomSheetEntity(context: Context, key:Int):BottomSheetEntity{
        val listUrlMedia = mutableListOf<String>()
        try {
            when(key){
                0 -> addGalleryUri(context, listUrlMedia)
                1 -> addAudioUri(context,listUrlMedia)
                2 -> addVideoUri(context,listUrlMedia)
                3 -> addPlaylistUri(context,listUrlMedia)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        listUrlMedia.reverse()
        return BottomSheetEntity(listUrlMedia)
    }

    private fun addGalleryUri(context: Context, listUrlImage: MutableList<String>) {
        val projection = arrayOf(MediaStore.Images.ImageColumns.DATA)
        val cursor = context.contentResolver?.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null)
        val size: Int = cursor?.count ?: 0
        if (size != 0) {
            while (cursor?.moveToNext() == true) {
                val fileColumnIndex: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val path: String = cursor.getString(fileColumnIndex)
                listUrlImage.add(path)
            }
        }
    }

    private fun addAudioUri(context: Context, listUrlImage: MutableList<String>) {
        val projection = arrayOf(MediaStore.Audio.AudioColumns.DATA)
        val cursor = context.contentResolver?.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null)
        val size: Int = cursor?.count ?: 0
        if (size != 0) {
            while (cursor?.moveToNext() == true) {
                val fileColumnIndex: Int = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DATA)
                val path: String = cursor.getString(fileColumnIndex)
                listUrlImage.add(path)
            }
        }
    }

    private fun addVideoUri(context: Context, listUrlImage: MutableList<String>) {
        val projection = arrayOf(MediaStore.Video.VideoColumns.DATA)
        val cursor = context.contentResolver?.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null)
        val size: Int = cursor?.count ?: 0
        if (size != 0) {
            while (cursor?.moveToNext() == true) {
                val fileColumnIndex: Int = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA)
                val path: String = cursor.getString(fileColumnIndex)
                listUrlImage.add(path)
            }
        }
    }

    private fun addPlaylistUri(context: Context, listUrlImage: MutableList<String>) {
        val projection = arrayOf(MediaStore.Audio.PlaylistsColumns.DATA)
        val cursor = context.contentResolver?.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null)
        val size: Int = cursor?.count ?: 0
        if (size != 0) {
            while (cursor?.moveToNext() == true) {
                val fileColumnIndex: Int = cursor.getColumnIndexOrThrow(MediaStore.Audio.PlaylistsColumns.DATA)
                val path: String = cursor.getString(fileColumnIndex)
                listUrlImage.add(path)
            }
        }
    }
}