package ru.spliterash.vkVideoUnlocker.video.controller.request

import java.util.*

data class VideoSaveRequest(
    val id: UUID,
    val userId: Long,
    val groupId: Long?,
    val uploadUrl: String,
)