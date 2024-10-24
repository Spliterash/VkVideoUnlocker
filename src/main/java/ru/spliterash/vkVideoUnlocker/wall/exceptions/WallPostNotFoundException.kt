package ru.spliterash.vkVideoUnlocker.wall.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

class WallPostNotFoundException : VkUnlockerException() {
    override fun messageForUser(source: RootMessage?) = "Пост не найден"
}