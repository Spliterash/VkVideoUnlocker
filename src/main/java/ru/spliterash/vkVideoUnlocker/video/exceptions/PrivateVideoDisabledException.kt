package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class PrivateVideoDisabledException : VkUnlockerException() {
    override fun messageForUser() = "Видео из закрытых групп временно отключены"
}