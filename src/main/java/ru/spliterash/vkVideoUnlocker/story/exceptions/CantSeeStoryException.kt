package ru.spliterash.vkVideoUnlocker.story.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class CantSeeStoryException : VkUnlockerException() {
    override fun messageForUser() = "Не могу посмотреть историю, извини"
}
