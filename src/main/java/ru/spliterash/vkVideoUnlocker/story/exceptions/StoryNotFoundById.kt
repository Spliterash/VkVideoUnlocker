package ru.spliterash.vkVideoUnlocker.story.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

class StoryNotFoundById : VkUnlockerException() {
    override fun messageForUser(source: RootMessage?) = "Не удалось найти историю по ID. Попробуй переслать мне её в личные сообщения"

}
