package ru.spliterash.vkVideoUnlocker.video.dto

import ru.spliterash.vkVideoUnlocker.group.WorkUserGroupService
import ru.spliterash.vkVideoUnlocker.group.dto.GroupStatus
import ru.spliterash.vkVideoUnlocker.user.client.vkModels.VkVideo
import ru.spliterash.vkVideoUnlocker.video.VideoAccessor
import ru.spliterash.vkVideoUnlocker.video.impl.VideoAccessorFactory

/**
 * Видео, доступное для просмотра, вместе со статусом группы
 */
class FullVideo(
    val video: VkVideo,
    private var status: GroupStatus?,
    private val factory: VideoAccessorFactory,
    private val groupService: WorkUserGroupService,
) {
    private var accessor: VideoAccessor? = null

    suspend fun status(): GroupStatus {
        if (video.ownerId < 0)
            return GroupStatus.PUBLIC // Пользователь

        return status ?: groupService.joinGroup(-video.ownerId).also {
            status = it
        }
    }

    fun toAccessor(): VideoAccessor {
        return accessor ?: factory.create(video).also {
            accessor = it
        }
    }
}