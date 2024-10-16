package ru.spliterash.vkVideoUnlocker.message.api

import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.message.vkModels.request.Keyboard

interface Messages {
    suspend fun messageById(
        groupId: Long,
        messageId: String
    ): RootMessage

    suspend fun sendMessage(
        peerId: Long,
        message: String? = null,
        replyTo: Long? = null,
        attachments: String? = null,
        keyboard: Keyboard? = null,
    ): Long

    suspend fun editMessage(
        peerId: Long,
        conversationMessageId: Long,
        message: String? = null,
        attachments: String? = null,
        keyboard: Keyboard? = null,
    )
}