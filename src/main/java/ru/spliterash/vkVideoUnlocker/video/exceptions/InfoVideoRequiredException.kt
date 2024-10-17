package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.AlwaysNotifyException
import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class InfoVideoRequiredException : VkUnlockerException(), AlwaysNotifyException {
    override fun messageForUser() =
        "Прикрепи видео как видео, а не как ссылку"
}