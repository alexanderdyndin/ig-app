package com.intergroupapplication.data.mappers

import com.intergroupapplication.data.model.AudioModel
import com.intergroupapplication.data.model.AudioRequestModel
import com.intergroupapplication.data.model.FileRequestModel
import com.intergroupapplication.data.model.ImageVideoModel
import com.intergroupapplication.data.network.dto.AudiosDto
import com.intergroupapplication.data.network.dto.ImagesDto
import com.intergroupapplication.data.network.dto.VideosDto
import com.intergroupapplication.domain.entity.*
import javax.inject.Inject

/**
 * Created by abakarmagomedov on 29/08/2018 at project InterGroupApplication.
 */
class MediaMapper @Inject constructor() {

    fun mapToDto(from: AudioListEntity): AudiosDto =
        AudiosDto(
            from.count,
            from.next,
            from.previous,
            from.audios.map { mapToDto(it) }
        )

    fun mapToDomainEntity(from: AudiosDto): AudioListEntity =
        AudioListEntity(
            from.count,
            from.next,
            from.previous,
            from.audios.map { mapToDomainEntity(it) }
        )

    fun mapToDto(from: ImageListEntity): ImagesDto =
        ImagesDto(
            from.count,
            from.next,
            from.previous,
            from.images.map { mapToDto(it) }
        )

    fun mapToDomainEntity(from: ImagesDto): ImageListEntity =
        ImageListEntity(
            from.count,
            from.next,
            from.previous,
            from.images.map { mapToDomainEntity(it) }
        )

    fun mapToDto(from: VideoListEntity): VideosDto =
        VideosDto(
            from.count,
            from.next,
            from.previous,
            from.videos.map { mapToDto(it) }
        )

    fun mapToDomainEntity(from: VideosDto): VideoListEntity =
        VideoListEntity(
            from.count,
            from.next,
            from.previous,
            from.videos.map { mapToDomainEntity(it) }
        )

    fun mapToDto(from: FileRequestEntity): FileRequestModel =
        FileRequestModel(
            from.file,
            from.description.orEmpty(),
            from.title.orEmpty(),
            from.preview,
            from.duration
        )

    fun mapToDto(from: AudioRequestEntity): AudioRequestModel =
        AudioRequestModel(
            from.urlFile,
            from.description.orEmpty(),
            from.nameSong.orEmpty(),
            from.artist.orEmpty(),
            from.genre.orEmpty(),
            from.duration.orEmpty()
        )

    fun mapToDomainEntity(from: FileRequestModel): FileRequestEntity =
        FileRequestEntity(from.file, from.description, from.title, from.preview, from.duration)

    fun mapToDomainEntity(from: AudioRequestModel): AudioRequestEntity =
        AudioRequestEntity(
            from.file,
            from.description,
            from.song,
            from.artist,
            from.genre,
            from.duration
        )

    fun mapToDto(from: FileEntity): ImageVideoModel {
        return ImageVideoModel(
            from.id,
            from.file,
            from.isActive,
            from.description,
            from.title,
            from.post,
            from.owner,
            from.preview,
            from.duration
        )
    }

    fun mapToDomainEntity(from: ImageVideoModel): FileEntity {
        return FileEntity(
            from.id,
            from.file,
            from.isActive,
            from.description,
            from.title,
            from.post,
            from.owner,
            from.preview ?: "",
            from.duration ?: "00:00"
        )
    }

    fun mapToDto(from: AudioEntity): AudioModel {
        return AudioModel(
            from.id,
            from.urlFile,
            from.isActive,
            from.description,
            from.nameSong,
            from.artist,
            from.genre,
            from.post,
            from.owner,
            from.duration
        )
    }

    fun mapToDomainEntity(from: AudioModel): AudioEntity {
        return AudioEntity(
            from.id,
            from.file,
            from.isActive,
            from.description,
            from.song,
            from.artist,
            from.genre,
            from.post,
            from.owner,
            from.duration ?: "00:00"
        )
    }
}
