package ru.spliterash.vkVideoUnlocker.common.vkModels

import com.fasterxml.jackson.annotation.JsonProperty

data class VkUploadUrlResponse(
    @JsonProperty("upload_url") val uploadUrl: String
)