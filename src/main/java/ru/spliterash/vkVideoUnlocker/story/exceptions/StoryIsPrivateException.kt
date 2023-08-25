package ru.spliterash.vkVideoUnlocker.story.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class StoryIsPrivateException : VkUnlockerException() {
    override fun messageForUser() = "Эта история закрыта"

}
