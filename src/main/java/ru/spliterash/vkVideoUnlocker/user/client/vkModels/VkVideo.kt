package ru.spliterash.vkVideoUnlocker.user.client.vkModels

import com.fasterxml.jackson.annotation.JsonProperty

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
    /**
     * Video duration in seconds
     */
    @JsonProperty("duration") val duration: Int,
    /**
     * Video ID
     */
    @JsonProperty("id")
    val id: Int,
    @JsonProperty("content_restricted")
    val contentRestricted: Boolean = false,
    /**
     * Video owner ID
     */
    @JsonProperty("owner_id")
    val ownerId: Int,
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

    /**
     * External platform
     */
    @JsonProperty("platform")
    val platform: String?,
    @JsonProperty("files")
    val files: VideoFiles?
)

