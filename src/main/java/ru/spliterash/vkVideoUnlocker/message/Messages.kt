package ru.spliterash.vkVideoUnlocker.message

interface Messages {
    suspend fun sendMessage(
        peerId: Int,
        message: String?,
        replyTo: Int? = null,
        attachments: String? = null
    ): Int
}