package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class SelfVideoException : VkUnlockerException() {
    override fun messageForUser() = "Это наше видео"
}
