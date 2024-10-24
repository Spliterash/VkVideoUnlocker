package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

class VideoSaveExpireException : VkUnlockerException() {
    override fun messageForUser(source: RootMessage?): String {
        return "Сохранение видео просрочено или уже завершено"
    }
}
