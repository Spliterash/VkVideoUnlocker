package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class VideoSaveExpireException : VkUnlockerException() {
    override fun messageForUser(): String {
        return "Сохранение видео просрочено или уже завершено"
    }
}
