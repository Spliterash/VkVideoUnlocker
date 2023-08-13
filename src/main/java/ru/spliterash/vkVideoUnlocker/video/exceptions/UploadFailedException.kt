package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class UploadFailedException(val response: String) : VkUnlockerException("Failed upload video:\n$response") {
}