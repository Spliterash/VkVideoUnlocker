package ru.spliterash.vkVideoUnlocker.video.service

import ru.spliterash.vkVideoUnlocker.group.dto.GroupStatus
import ru.spliterash.vkVideoUnlocker.video.VideoAccessor

/**
 * Вся информация, которая собралась по пути загрузки видео
 */
data class VideoDownloadInfo(
    val accessor: VideoAccessor,
    val groupStatus: GroupStatus
)