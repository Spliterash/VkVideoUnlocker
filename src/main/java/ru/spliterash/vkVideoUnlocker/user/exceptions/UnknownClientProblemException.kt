package ru.spliterash.vkVideoUnlocker.user.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class UnknownClientProblemException(cause: Exception) : VkUnlockerException(cause)