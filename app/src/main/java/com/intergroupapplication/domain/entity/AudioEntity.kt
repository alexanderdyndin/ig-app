package com.intergroupapplication.domain.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AudioEntity(
    val id: Int,
    val urlFile: String,
    val isActive: Boolean,
    val description: String,
    val nameSong: String,
    val artist: String,
    val genre: String,
    val post: Int,
    val owner: Int,
    val duration: String = "00:00"
) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AudioEntity

        if (id != other.id) return false
        if (urlFile != other.urlFile) return false
        if (isActive != other.isActive) return false
        if (description != other.description) return false
        if (nameSong != other.nameSong) return false
        if (artist != other.artist) return false
        if (genre != other.genre) return false
        if (post != other.post) return false
        if (owner != other.owner) return false
        if (duration != other.duration) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + urlFile.hashCode()
        result = 31 * result + isActive.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + nameSong.hashCode()
        result = 31 * result + artist.hashCode()
        result = 31 * result + genre.hashCode()
        result = 31 * result + post
        result = 31 * result + owner
        result = 31 * result + duration.hashCode()
        return result
    }
}
