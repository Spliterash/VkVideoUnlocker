package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class VideoEmptyUrlException : VkUnlockerException() {
    override fun messageForUser() = "В ответе пришли пустые ссылки на загрузку"
}