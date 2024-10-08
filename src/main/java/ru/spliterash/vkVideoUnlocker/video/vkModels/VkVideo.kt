package ru.spliterash.vkVideoUnlocker.video.vkModels

import com.fasterxml.jackson.annotation.JsonProperty
import ru.spliterash.vkVideoUnlocker.common.putIfNotNull
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.AttachmentContent
import ru.spliterash.vkVideoUnlocker.video.exceptions.VideoEmptyUrlException
import ru.spliterash.vkVideoUnlocker.video.exceptions.VideoNotFoundException
import java.net.URL

/**
 * Video object
 */
data class VkVideo(
    /**
     * 1 if video is private
     */
    @JsonProperty("is_private")
    val isPrivate: Boolean,
    /**
     * Video description
     */
    @JsonProperty(
        "description"
    ) val description: String?,
    @JsonProperty(
        "access_key"
    ) val accessKey: String?,
    /**
     * Video duration in seconds
     */
    @JsonProperty("duration") val duration: Int,
    /**
     * Video ID
     */
    @JsonProperty("id")
    val id: Long,
    @JsonProperty("content_restricted")
    val contentRestricted: Boolean = false,
    /**
     * Video owner ID
     */
    @JsonProperty("owner_id")
    val ownerId: Long,
    /**
     * Video title
     */
    @JsonProperty("title")
    val title: String,

    /**
     * Whether video is added to bookmarks
     */
    @JsonProperty(
        "is_favorite"
    )
    val isFavorite: Boolean,
    val image: List<VideoImage>?,

    /**
     * External platform
     */
    @JsonProperty("platform")
    val platform: String?,
    @JsonProperty("files")
    val files: VideoFiles?
) : AttachmentContent {

    fun preview(): URL = image?.lastOrNull()
        ?.url
        ?: URL("https://upload.wikimedia.org/wikipedia/commons/thumb/6/65/No-Image-Placeholder.svg/1665px-No-Image-Placeholder.svg.png") // IM TOO LAZY

    fun qualityUrl(quality: Int) = qualityMap()[quality] ?: throw VideoNotFoundException()

    fun qualityMap(): Map<Int, URL> {
        val map = linkedMapOf<Int, URL>()

        map.putIfNotNull(1080, files?.mp41080)
        map.putIfNotNull(720, files?.mp4720)
        map.putIfNotNull(480, files?.mp4480)
        map.putIfNotNull(360, files?.mp4360)
        map.putIfNotNull(240, files?.mp4240)
        map.putIfNotNull(144, files?.mp4144)

        return map
    }

    @Throws(VideoEmptyUrlException::class)
    fun maxQuality(): Pair<Int, URL> = files?.run {
        if (mp41080 != null)
            1080 to mp41080
        else if (mp4720 != null)
            720 to mp4720
        else if (mp4480 != null)
            480 to mp4480
        else if (mp4360 != null)
            360 to mp4360
        else if (mp4240 != null)
            240 to mp4240
        else if (mp4144 != null)
            144 to mp4144
        else
            throw VideoEmptyUrlException()

    } ?: throw VideoEmptyUrlException()
}

