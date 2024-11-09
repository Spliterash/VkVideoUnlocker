package ru.spliterash.vkVideoUnlocker.messageChain.handlers

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.message.editableMessage.EditableMessage
import ru.spliterash.vkVideoUnlocker.message.utils.MessageUtils
import ru.spliterash.vkVideoUnlocker.messageChain.ActivationMessageHandler
import ru.spliterash.vkVideoUnlocker.video.service.VideoSaveService

@Singleton
class SaveVideoChain(
    private val utils: MessageUtils,
    private val saveService: VideoSaveService,

    ) : ActivationMessageHandler(
    "сохранить",
    "save",
) {

    override suspend fun handleAfterCheck(message: RootMessage, editableMessage: EditableMessage): Boolean {
        val videoHolder = utils.scanForVideoContent(message)

        if (videoHolder == null) {
            editableMessage.sendOrUpdate("Прикрепи видео к сообщению, ну или перешли его как обычно, чтобы я знал что тебе нужно")
        } else {
            val full = videoHolder.fullVideo()
            val pending = saveService.createPendingVideo(full.toAccessor(), message, editableMessage)

            editableMessage.sendOrUpdate(
                "Для сохранения видео нажми на кнопку ниже",
                keyboard = saveService.createKeyboard(pending.id, full)
            )
        }

        return true
    }

    data class Payload(
        val id: String,
        val video: Video
    ) {
        data class Video(
            val id: String,
            val name: String,
            val preview: String,
        )
    }
}