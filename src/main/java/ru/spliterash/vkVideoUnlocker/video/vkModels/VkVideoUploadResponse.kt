package ru.spliterash.vkVideoUnlocker.video.vkModels

import com.fasterxml.jackson.annotation.JsonProperty

data class VkVideoUploadResponse(
    @JsonProperty("video_id") val videoId: String
)