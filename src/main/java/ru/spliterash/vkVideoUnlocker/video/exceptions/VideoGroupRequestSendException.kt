package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.longpoll.message.isGroupChat

/**
 * Группа закрыта, но заявку мы уже отправили
 */
class VideoGroupRequestSendException : VkUnlockerException() {
    override fun messageForUser(source: RootMessage?): String {
        var base = "Видео из закрытой группы. " +
                "Заявку на вступление мы уже отправили, так что можешь попробовать ещё раз позже"
        if (source?.isGroupChat() == true) base += "\nТак же можешь попробовать отправить сообщение в личку сообщества, может быть получится сразу"

        return base
    }
}