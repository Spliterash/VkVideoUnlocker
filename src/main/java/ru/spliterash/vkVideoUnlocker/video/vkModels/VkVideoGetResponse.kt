package ru.spliterash.vkVideoUnlocker.video.vkModels

data class VkVideoGetResponse(
    val count: Int,
    val items: List<VkVideo>
)