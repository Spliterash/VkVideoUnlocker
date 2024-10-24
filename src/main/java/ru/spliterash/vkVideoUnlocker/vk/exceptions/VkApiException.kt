package ru.spliterash.vkVideoUnlocker.vk.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.AlwaysNotifyException
import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

class VkApiException(
    val code: Int,
    val info: String
) : VkUnlockerException("Vk request failed, code $code, info: $info"), AlwaysNotifyException {
    override val message = messageForUser()
    override fun messageForUser(source: RootMessage?) = "Произошла ошибка при выполнении метода VK. Код ошибки: $code, информация: $info"
}