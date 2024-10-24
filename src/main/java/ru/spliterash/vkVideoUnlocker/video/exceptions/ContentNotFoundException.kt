package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.AlwaysNotifyException
import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

class ContentNotFoundException : VkUnlockerException(),AlwaysNotifyException {
    override fun messageForUser(source: RootMessage?): String {
        return "Я не знаю каким образом, но при получении сообщения от пользователя, содержимое отличается"
    }
}
