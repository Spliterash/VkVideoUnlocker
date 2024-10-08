package ru.spliterash.vkVideoUnlocker.message.api

import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

interface Messages {
    suspend fun messageById(
        groupId: Long,
        messageId: String
    ): RootMessage

    suspend fun sendMessage(
        peerId: Long,
        message: String? = null,
        replyTo: Long? = null,
        attachments: String? = null
    ): Long

    suspend fun editMessage(
        peerId: Long,
        conversationMessageId: Long,
        message: String? = null,
        attachments: String? = null
    )
}