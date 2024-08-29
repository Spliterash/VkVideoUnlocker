package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class VideoNotAvailableException : VkUnlockerException() {
    override fun messageForUser(): String {
        return "Видео недоступно"
    }
}