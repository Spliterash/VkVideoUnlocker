package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class VideoLockedException(val reason: String) : VkUnlockerException() {
    override fun messageForUser() = "Видео недоступно, причина: $reason"
}