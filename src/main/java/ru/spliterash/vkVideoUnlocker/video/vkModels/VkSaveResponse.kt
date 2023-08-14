package ru.spliterash.vkVideoUnlocker.video.vkModels

import com.fasterxml.jackson.annotation.JsonProperty

data class VkSaveResponse(
    @JsonProperty("upload_url") val uploadUrl: String
)