package ru.spliterash.vkVideoUnlocker.message.editableMessage

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi

@Singleton
class EditableMessageService() {
    fun create(peerId: Long, conversationMessageId: Long, client: VkApi): EditableMessage {
        return EditableMessageImpl(peerId, conversationMessageId, client);
    }

    fun create(rootMessage: RootMessage, client: VkApi): EditableMessage {
        return create(rootMessage.peerId, rootMessage.conversationMessageId, client)
    }
}