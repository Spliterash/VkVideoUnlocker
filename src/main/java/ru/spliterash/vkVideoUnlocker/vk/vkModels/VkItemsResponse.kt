package ru.spliterash.vkVideoUnlocker.vk.vkModels

data class VkItemsResponse<T>(
    val count: Int,
    val items: List<T>
)