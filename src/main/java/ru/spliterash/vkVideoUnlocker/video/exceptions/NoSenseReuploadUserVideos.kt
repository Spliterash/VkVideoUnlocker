package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

class NoSenseReuploadUserVideos : VkUnlockerException() {
    override fun messageForUser(source: RootMessage?): String {
        return "Мне нет смысла перезаливать пользовательские видео. Если тебе очень хочется, перешли его ещё раз с командой 'сохранить'"
    }
}