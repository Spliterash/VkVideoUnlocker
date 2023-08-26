package ru.spliterash.vkVideoUnlocker.story.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class StoryNotFoundById : VkUnlockerException() {
    override fun messageForUser() = "Не удалось найти историю по ID. Попробуй переслать мне её в личные сообщения"

}
