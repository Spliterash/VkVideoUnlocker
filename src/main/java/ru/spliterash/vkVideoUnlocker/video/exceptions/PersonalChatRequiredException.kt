package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.AlwaysNotifyException
import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

class PersonalChatRequiredException : VkUnlockerException(), AlwaysNotifyException {
    override fun messageForUser(source: RootMessage?) =
        "Перешли видео в личные сообщения сообщества. Из за ограничений API я не могу сделать это в беседе"
}