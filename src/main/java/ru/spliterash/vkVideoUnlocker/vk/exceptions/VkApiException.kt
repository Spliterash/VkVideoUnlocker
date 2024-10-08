package ru.spliterash.vkVideoUnlocker.vk.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.AlwaysNotifyException
import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class VkApiException(
    val code: Int,
    val info: String
) : VkUnlockerException("Vk request failed, code $code, info: $info"), AlwaysNotifyException {
    override fun messageForUser() = "Произошла ошибка при выполнении метода VK. Код ошибки: $code, информация: $info"
}