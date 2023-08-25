package ru.spliterash.vkVideoUnlocker.story.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class StoryExpiredException : VkUnlockerException() {
    override fun messageForUser() = "Эта история просрочилась"

}
