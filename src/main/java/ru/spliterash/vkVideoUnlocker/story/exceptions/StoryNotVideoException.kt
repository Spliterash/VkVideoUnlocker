package ru.spliterash.vkVideoUnlocker.story.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

class StoryNotVideoException : VkUnlockerException() {
    override fun messageForUser(source: RootMessage?) = "Эта история не видос"

}
