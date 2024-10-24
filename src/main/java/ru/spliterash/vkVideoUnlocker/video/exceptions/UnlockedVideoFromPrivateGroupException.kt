package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

class UnlockedVideoFromPrivateGroupException : VkUnlockerException() {
    override fun messageForUser(source: RootMessage?): String {
        return "Это видео из закрытой группы, следовательно перезалив будет приватным, и никак кроме сообщения его не посмотреть"
    }
}