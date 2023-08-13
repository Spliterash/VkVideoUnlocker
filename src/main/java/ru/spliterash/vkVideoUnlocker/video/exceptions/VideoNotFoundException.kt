package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class VideoNotFoundException : VkUnlockerException() {
    override fun messageForUser() = "Видео не найдено"
}