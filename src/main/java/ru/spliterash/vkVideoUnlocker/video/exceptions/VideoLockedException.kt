package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

class VideoLockedException(val reason: String) : VkUnlockerException() {
    override fun messageForUser(source: RootMessage?) = "Видео недоступно, причина: $reason"
}