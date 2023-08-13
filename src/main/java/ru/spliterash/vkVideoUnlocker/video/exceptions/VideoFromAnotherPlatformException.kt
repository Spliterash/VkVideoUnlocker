package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class VideoFromAnotherPlatformException : VkUnlockerException() {
    override fun messageForUser() = "Это видео с другой платформы, иди смотри его там"

}
