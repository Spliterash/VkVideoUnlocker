package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

class SelfVideoException : VkUnlockerException() {
    override fun messageForUser(source: RootMessage?) = "Это наше видео"
}
