package ru.spliterash.vkVideoUnlocker.messageChain.handlers

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.message.editableMessage.EditableMessage
import ru.spliterash.vkVideoUnlocker.message.utils.MessageUtils
import ru.spliterash.vkVideoUnlocker.messageChain.ActivationMessageHandler
import ru.spliterash.vkVideoUnlocker.video.DownloadUrlSupplier
import ru.spliterash.vkVideoUnlocker.video.holder.VideoHolder
import ru.spliterash.vkVideoUnlocker.video.vkModels.fullId

@Singleton
class DownloadVideoChain(
    private val downloadUrlSupplier: DownloadUrlSupplier,
    private val utils: MessageUtils,
) : ActivationMessageHandler(
    "скачать",
    "загрузить",
    "стянуть",
    "download"
) {

    override suspend fun handleAfterCheck(message: RootMessage, editableMessage: EditableMessage): Boolean {
        val videoHolder = utils.scanForVideoContent(message)

        if (videoHolder == null) {
            editableMessage.sendOrUpdate("Прикрепи видео к сообщению, ну или перешли его как обычно, чтобы я знал что тебе нужно")
        } else {
            val baseUrl = if (videoHolder is VideoHolder) {
                "video" + videoHolder.fullVideo().video.fullId()
            } else videoHolder.attachmentId

            val url = downloadUrlSupplier.downloadUrl(baseUrl)
            editableMessage.sendOrUpdate("Скачать: $url")
        }

        return true
    }
}