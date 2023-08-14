package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class VideoOpenException : VkUnlockerException() {
    override fun messageForUser() = "Видео открыто"
}