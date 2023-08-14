package ru.spliterash.vkVideoUnlocker.wall.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class WallPostNotFoundException : VkUnlockerException() {
    override fun messageForUser() = "Пост не найден"
}