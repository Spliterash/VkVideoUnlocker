package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

class VideoNotAvailableException : VkUnlockerException() {
    override fun messageForUser(source: RootMessage?): String {
        return "Видео недоступно"
    }
}