package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.AlwaysNotifyException
import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class ContentNotFoundException : VkUnlockerException(),AlwaysNotifyException {
    override fun messageForUser(): String {
        return "Я не знаю каким образом, но при получении сообщения от пользователя, содержимое отличается"
    }
}
