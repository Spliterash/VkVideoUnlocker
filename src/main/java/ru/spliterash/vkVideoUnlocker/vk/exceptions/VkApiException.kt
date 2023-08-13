package ru.spliterash.vkVideoUnlocker.vk.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

open class VkApiException(
    val code: Int,
    val info: String
) : VkUnlockerException("Vk request failed, code $code, info: $info")