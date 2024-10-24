package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

class VideoFromAnotherPlatformException : VkUnlockerException() {
    override fun messageForUser(source: RootMessage?) = "Это видео с другой платформы, иди смотри его там"

}
