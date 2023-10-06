package ru.spliterash.vkVideoUnlocker.video.controller.response

import java.net.URL

data class VideoResponse(
    val maxQuality: Int,
    val preview: URL,
    val url: URL
)