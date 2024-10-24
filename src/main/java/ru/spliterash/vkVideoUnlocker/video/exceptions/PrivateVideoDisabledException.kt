package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

class PrivateVideoDisabledException : VkUnlockerException() {
    override fun messageForUser(source: RootMessage?) = "Видео из закрытых групп временно отключены"
}