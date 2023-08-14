package ru.spliterash.vkVideoUnlocker.video.vkModels

import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URL

data class VideoFiles(
    @JsonProperty("mp4_144") val mp4144: URL?,
    /**
     * URL of the mpeg4 file with 240p quality
     */
    @JsonProperty("mp4_240")
    val mp4240: URL?,

    /**
     * URL of the mpeg4 file with 360p quality
     */
    @JsonProperty("mp4_360")
    val mp4360: URL?,

    /**
     * URL of the mpeg4 file with 480p quality
     */
    @JsonProperty("mp4_480")
    val mp4480: URL?,

    /**
     * URL of the mpeg4 file with 720p quality
     */
    @JsonProperty("mp4_720")
    val mp4720: URL?,

    /**
     * URL of the mpeg4 file with 1080p quality
     */
    @JsonProperty("mp4_1080")
    val mp41080: URL?,

    /**
     * URL of the mpeg4 file with 2K quality
     */
    @JsonProperty("mp4_1440")
    val mp41440: URL?,

    /**
     * URL of the mpeg4 file with 4K quality
     */
    @JsonProperty("mp4_2160")
    val mp42160: URL?
)