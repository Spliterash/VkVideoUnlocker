package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class VideoTooLongException : VkUnlockerException() {
    override fun messageForUser() = "Извини, но я не перезаливаю видео длинее 5 минут"
}
