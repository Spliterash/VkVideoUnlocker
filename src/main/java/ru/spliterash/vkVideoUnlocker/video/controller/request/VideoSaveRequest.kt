package ru.spliterash.vkVideoUnlocker.video.controller.request

import java.util.*

data class VideoSaveRequest(
    val id: UUID,
    val uploadUrl: String,
)