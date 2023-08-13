package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class WeDoNotWorkWithLockedUserVideosException : VkUnlockerException() {
    override fun messageForUser() = "Я не работаю с закрытыми видео пользователей"
}