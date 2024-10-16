package ru.spliterash.vkVideoUnlocker.video.dto

import ru.spliterash.vkVideoUnlocker.group.WorkUserGroupService
import ru.spliterash.vkVideoUnlocker.group.dto.GroupStatus
import ru.spliterash.vkVideoUnlocker.video.accessor.AdvancedVideoAccessor
import ru.spliterash.vkVideoUnlocker.video.accessor.VideoAccessorFactory
import ru.spliterash.vkVideoUnlocker.video.vkModels.VkVideo

/**
 * Видео, доступное для просмотра, вместе со статусом группы
 */
class FullVideo(
    val video: VkVideo,
    val originalAttachmentId: String,
    private var status: GroupStatus?,
    private val factory: VideoAccessorFactory,
    private val groupService: WorkUserGroupService,
) {
    private var accessor: AdvancedVideoAccessor? = null

    suspend fun status(): GroupStatus {
        if (video.ownerId > 0)
            return GroupStatus.PUBLIC // Пользователь

        return status ?: groupService.joinGroup(-video.ownerId).also {
            status = it
        }
    }

    suspend fun shouldBeLocked(): Boolean {
        return status() != GroupStatus.PUBLIC
    }

    fun toAccessor(): AdvancedVideoAccessor {
        return accessor ?: factory.create(video).also {
            accessor = it
        }
    }
}