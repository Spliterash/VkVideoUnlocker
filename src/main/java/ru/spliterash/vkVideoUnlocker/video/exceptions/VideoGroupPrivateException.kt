package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

/**
 * Группа частная
 */
class VideoGroupPrivateException : VkUnlockerException() {
    override fun messageForUser() = "Видео принадлежит частной группе"
}