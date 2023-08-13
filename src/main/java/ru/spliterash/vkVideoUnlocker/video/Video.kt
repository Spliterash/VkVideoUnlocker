package ru.spliterash.vkVideoUnlocker.video

import ru.spliterash.vkVideoUnlocker.user.client.vkModels.VkVideo
import java.net.URL

data class Video(
    val id: String,
    val name: String,
    val platform: String?,
    val duration: Int,
    val url: URL?,

    val original: VkVideo
)