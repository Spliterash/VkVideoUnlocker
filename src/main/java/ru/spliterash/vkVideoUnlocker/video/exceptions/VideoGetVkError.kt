package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class VideoGetVkError(val raw: String) : VkUnlockerException("Error get video:\n$raw")
