package ru.spliterash.vkVideoUnlocker.docs.vkModels

import com.fasterxml.jackson.annotation.JsonProperty
import ru.spliterash.vkVideoUnlocker.common.vkModels.VkSaveResponse

data class VkFileSaveResponse(
    val type: String,
    @JsonProperty("audio_message")
    val audioMessage: VkSaveResponse
)