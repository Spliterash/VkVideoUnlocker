package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.AlwaysNotifyException
import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class NoSourceException : VkUnlockerException(), AlwaysNotifyException {
    override fun messageForUser() =
        "Перешли видео в личные сообщения сообщества. Из за ограничений API я не могу сделать это в беседе"
}