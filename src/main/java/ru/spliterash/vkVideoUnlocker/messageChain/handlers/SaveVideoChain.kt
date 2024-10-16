package ru.spliterash.vkVideoUnlocker.messageChain.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.message.editableMessage.EditableMessage
import ru.spliterash.vkVideoUnlocker.message.utils.MessageUtils
import ru.spliterash.vkVideoUnlocker.message.vkModels.request.Keyboard
import ru.spliterash.vkVideoUnlocker.messageChain.ActivationMessageHandler
import ru.spliterash.vkVideoUnlocker.video.service.VideoSaveService
import ru.spliterash.vkVideoUnlocker.video.vkModels.publicId

@Singleton
class SaveVideoChain(
    private val utils: MessageUtils,
    private val saveService: VideoSaveService,
    @Value("\${vk-unlocker.miniAppId}")
    private val appId: String,
    private val mapper: ObjectMapper,
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
                "Необходимо выдать доступ на сохранение.\n" +
                        "Токен не передаётся на сервер, а так же действителен только для твоего IP адреса.\n" +
                        "Всё что передаётся на сервер, это upload_url, полученный через метод https://dev.vk.com/ru/method/video.save",
                keyboard = Keyboard(
                    listOf(
                        listOf(
                            Keyboard.Button(
                                Keyboard.Button.OpenAppAction(
                                    appId,
                                    "Подтвердить",
                                    mapper.writeValueAsString(
                                        Payload(
                                            pending.id.toString(),
                                            Payload.Video(
                                                full.video.publicId(),
                                                full.video.title,
                                                full.video.preview().toString()
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        } // Вот это лесенка, прикольна

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