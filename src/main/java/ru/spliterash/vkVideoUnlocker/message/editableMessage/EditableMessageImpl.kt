package ru.spliterash.vkVideoUnlocker.message.editableMessage

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.spliterash.vkVideoUnlocker.message.vkModels.request.Keyboard
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi

class EditableMessageImpl(
    private val peerId: Long,
    private val replyMessageConversationId: Long,
    private val client: VkApi
) : EditableMessage {
    private var existMessageId: Long? = null
    private val lock = Mutex()
    override suspend fun sendOrUpdate(text: String?, attachments: String?, keyboard: Keyboard?) = lock.withLock {
        val existId = existMessageId
        if (existId == null) {
            existMessageId =
                client.messages.sendMessage(peerId, text, replyMessageConversationId, attachments, keyboard)
            return@withLock
        } else
            client.messages.editMessage(peerId, existId, text, attachments, keyboard)
    }
}