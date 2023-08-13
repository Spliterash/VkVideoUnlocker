package ru.spliterash.vkVideoUnlocker.vk

import com.fasterxml.jackson.annotation.JsonProperty

data class VkError(
    @JsonProperty("error_code") val code: Int,
    @JsonProperty("error_msg") val msg: String)