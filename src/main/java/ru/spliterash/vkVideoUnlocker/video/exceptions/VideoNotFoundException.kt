package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

class VideoNotFoundException : VkUnlockerException() {
    override fun messageForUser(source: RootMessage?) = "Видео не найдено"
}