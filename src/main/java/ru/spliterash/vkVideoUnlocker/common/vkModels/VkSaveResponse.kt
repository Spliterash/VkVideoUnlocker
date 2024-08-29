package ru.spliterash.vkVideoUnlocker.common.vkModels

import com.fasterxml.jackson.annotation.JsonProperty

data class VkSaveResponse(
    val id: Long,
    @JsonProperty("owner_id")
    val ownerId: Long,
    @JsonProperty("access_key")
    val accessKey: String?
)