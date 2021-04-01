package com.intergroupapplication.data.mapper

import com.intergroupapplication.data.model.*
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
            FileRequestModel(from.file, from.description.orEmpty(), from.title.orEmpty())

    fun mapToDto(from: AudioRequestEntity): AudioRequestModel =
            AudioRequestModel(from.file, from.description.orEmpty(), from.song.orEmpty(), from.artist.orEmpty(), from.genre.orEmpty())

    fun mapToDomainEntity(from: FileRequestModel): FileRequestEntity =
            FileRequestEntity(from.file, from.description.orEmpty(), from.title.orEmpty())

    fun mapToDomainEntity(from: AudioRequestModel): AudioRequestEntity =
            AudioRequestEntity(from.file, from.description.orEmpty(), from.song.orEmpty(), from.artist.orEmpty(), from.genre.orEmpty())

    fun mapToDto(from: FileEntity): ImageVideoModel {
        return ImageVideoModel(from.id, from.file, from.isActive, from.description, from.title, from.post, from.owner)
    }

    fun mapToDomainEntity(from: ImageVideoModel): FileEntity {
        return FileEntity(from.id, from.file, from.isActive, from.description, from.title, from.post, from.owner)
    }

    fun mapToDto(from: AudioEntity): AudioModel {
        return AudioModel(from.id, from.file, from.isActive, from.description, from.song, from.artist, from.genre, from.post, from.owner)
    }

    fun mapToDomainEntity(from: AudioModel): AudioEntity {
        return AudioEntity(from.id, from.file, from.isActive, from.description, from.song, from.artist, from.genre, from.post, from.owner)
    }


}
