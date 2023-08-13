package ru.spliterash.vkVideoUnlocker.video

import java.net.URL

data class Video(
    val id: String,
    val name: String,
    val platform: String?,
    val duration: Int,
    val url: URL?
)