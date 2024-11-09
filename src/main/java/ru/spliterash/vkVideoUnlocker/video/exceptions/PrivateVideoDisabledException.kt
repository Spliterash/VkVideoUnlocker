package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.ReUploadRestrictionException
import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

class PrivateVideoDisabledException : VkUnlockerException(), ReUploadRestrictionException {
    override fun messageForUser(source: RootMessage?) = "Видео из закрытых групп временно отключены"
    override val restrictionName: String
        get() = "закрытая группа"
}