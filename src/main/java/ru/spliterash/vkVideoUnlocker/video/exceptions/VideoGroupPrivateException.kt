package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

/**
 * Группа частная
 */
class VideoGroupPrivateException : VkUnlockerException() {
    override fun messageForUser(source: RootMessage?) = "Видео принадлежит частной группе"
}