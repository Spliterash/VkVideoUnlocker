package ru.spliterash.vkVideoUnlocker.message.api

interface Messages {
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