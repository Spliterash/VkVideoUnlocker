package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class VideoPrivateException : VkUnlockerException() {
    override fun messageForUser() = "Извини, но я не могу ничего сделать с приватными видео"
}