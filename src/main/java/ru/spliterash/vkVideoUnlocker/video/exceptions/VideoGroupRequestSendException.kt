package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

/**
 * Группа закрыта, но заявку мы уже отправили
 */
class VideoGroupRequestSendException : VkUnlockerException() {
    override fun messageForUser() =
        "Видео из закрытой группы. Заявку на вступление мы уже отправили, так что можешь попробовать ещё раз позже"
}