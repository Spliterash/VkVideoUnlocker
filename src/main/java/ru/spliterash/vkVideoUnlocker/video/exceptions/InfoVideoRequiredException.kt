package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.AlwaysNotifyException
import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

class InfoVideoRequiredException : VkUnlockerException(), AlwaysNotifyException {
    override fun messageForUser(source: RootMessage?) =
        "Прикрепи видео как видео, а не как ссылку"
}