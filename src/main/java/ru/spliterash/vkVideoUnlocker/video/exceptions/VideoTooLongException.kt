package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

class VideoTooLongException : VkUnlockerException() {
    override fun messageForUser(source: RootMessage?) = "Извини, но я не перезаливаю видео длинее 5 минут"
}
