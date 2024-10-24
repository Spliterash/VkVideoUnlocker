package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

class UploadFailedException(val response: String) : VkUnlockerException("Failed upload video:\n$response") {
    override fun messageForUser(source: RootMessage?) = "Не удалось загрузить видео"
}