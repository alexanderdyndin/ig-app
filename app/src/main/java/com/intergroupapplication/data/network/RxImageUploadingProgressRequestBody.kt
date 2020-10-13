package com.intergroupapplication.data.network

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import java.io.File
import java.io.FileInputStream


class RxImageUploadingProgressRequestBody(private val file: File) : RequestBody() {

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
    }

    private val progressSubject = PublishSubject.create<Int>()

    fun getProgressObservable(): Observable<Int> = progressSubject

    override fun contentType(): MediaType? = MediaType.parse("image/*")

    override fun writeTo(sink: BufferedSink) {
        val fileLength = file.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val inStream = FileInputStream(file)
        var uploaded: Long = 0
        inStream.use {
            var read: Int
            while (true) {
                read = it.read(buffer)
                if (read == -1) {
                    progressSubject.onComplete()
                    break
                }
                progressSubject.onNext((100 * uploaded / fileLength).toInt())
                uploaded += read.toLong()
                sink.write(buffer, 0, read)
            }
        }
    }
}