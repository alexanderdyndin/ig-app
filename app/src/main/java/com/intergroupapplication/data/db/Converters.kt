package com.intergroupapplication.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intergroupapplication.data.model.AudioModel
import com.intergroupapplication.data.model.ImageVideoModel

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromImages(images: List<ImageVideoModel>): String {
        return gson.toJson(images)
    }

    @TypeConverter
    fun toImages(strImages: String): List<ImageVideoModel> {
        val type = object : TypeToken<List<ImageVideoModel>>(){}.type
        return gson.fromJson(strImages, type)
    }

    @TypeConverter
    fun fromAudio(audio: List<AudioModel>): String {
        return gson.toJson(audio)
    }

    @TypeConverter
    fun toAudio(strAudio: String): List<AudioModel> {
        val type = object : TypeToken<List<AudioModel>>(){}.type
        return gson.fromJson(strAudio, type)
    }

}