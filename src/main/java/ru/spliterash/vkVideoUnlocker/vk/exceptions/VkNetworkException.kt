package ru.spliterash.vkVideoUnlocker.vk.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class VkNetworkException(
    val code: Int,
    val body: String
) : VkUnlockerException("Failed execute request. http code $code, response $body") {
    override fun messageForUser(): String = "VK вернул некорректный ответ"
}