package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

class VideoPrivateException : VkUnlockerException() {
    override fun messageForUser(source: RootMessage?) = "Извини, но я не могу ничего сделать с приватными видео"
}