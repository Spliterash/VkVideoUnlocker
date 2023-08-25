package ru.spliterash.vkVideoUnlocker.story.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class StoryNotVideoException : VkUnlockerException() {
    override fun messageForUser() = "Эта история не видос"

}
