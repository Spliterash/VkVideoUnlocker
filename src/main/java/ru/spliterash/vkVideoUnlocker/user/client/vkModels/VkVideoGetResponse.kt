package ru.spliterash.vkVideoUnlocker.user.client.vkModels

data class VkVideoGetResponse(
    val count: Int,
    val items: List<VkVideo>
)