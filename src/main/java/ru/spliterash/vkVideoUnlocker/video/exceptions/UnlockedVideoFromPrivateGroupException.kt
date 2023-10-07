package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class UnlockedVideoFromPrivateGroupException : VkUnlockerException() {
    override fun messageForUser(): String {
        return "Это видео из закрытой группы, следовательно перезалив будет приватным, и никак кроме сообщения его не посмотреть"
    }
}