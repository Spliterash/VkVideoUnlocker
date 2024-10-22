package ru.spliterash.vkVideoUnlocker.video.exceptions

import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

class NoSenseReuploadUserVideos : VkUnlockerException() {
    override fun messageForUser(): String {
        return "Мне нет смысла перезаливать пользовательские видео. Если тебе очень хочется, перешли его ещё раз с командой 'сохранить'"
    }
}