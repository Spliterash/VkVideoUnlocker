package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.ReUploadRestrictionException
import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

class VideoTooLongException : VkUnlockerException(), ReUploadRestrictionException {
    override fun messageForUser(source: RootMessage?) = "Извини, но я не перезаливаю видео длинее 5 минут"
    override val restrictionName: String
        get() = "слишком длинное"
}
