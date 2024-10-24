package ru.spliterash.vkVideoUnlocker.story.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

class CantSeeStoryException : VkUnlockerException() {
    override fun messageForUser(source: RootMessage?) = "Не могу посмотреть историю, извини"
}
