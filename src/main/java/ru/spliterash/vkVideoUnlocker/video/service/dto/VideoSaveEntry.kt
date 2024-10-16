package ru.spliterash.vkVideoUnlocker.video.service.dto

import ru.spliterash.vkVideoUnlocker.common.InputStreamSource
import ru.spliterash.vkVideoUnlocker.message.editableMessage.EditableMessage
import java.util.UUID

data class VideoSaveEntry(
    val id:UUID,
    val userId: Long,
    val message:EditableMessage,
    val accessor: InputStreamSource
)