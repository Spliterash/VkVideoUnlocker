package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

class VideoEmptyUrlException : VkUnlockerException() {
    override fun messageForUser(source: RootMessage?) = "В ответе пришли пустые ссылки на загрузку"
}