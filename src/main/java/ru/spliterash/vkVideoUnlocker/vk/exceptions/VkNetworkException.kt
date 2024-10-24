package ru.spliterash.vkVideoUnlocker.vk.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

class VkNetworkException(
    val code: Int,
    val body: String
) : VkUnlockerException("Failed execute request. http code $code, response $body") {
    override fun messageForUser(source: RootMessage?): String = "VK вернул некорректный ответ"
}