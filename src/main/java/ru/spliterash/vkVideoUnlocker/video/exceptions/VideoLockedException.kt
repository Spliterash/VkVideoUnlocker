package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class VideoLockedException : VkUnlockerException() {
    override fun messageForUser() = "Видео заблокированно. Если ты увидишь этот текст, напиши мне @spliterash"
}