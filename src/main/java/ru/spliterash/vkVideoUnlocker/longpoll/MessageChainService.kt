package ru.spliterash.vkVideoUnlocker.longpoll

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.message.editableMessage.EditableMessage
import ru.spliterash.vkVideoUnlocker.messageChain.MessageHandler

@Singleton
class MessageChainService(
    handlers: List<MessageHandler>
) {
    private val handlers = handlers
        .sortedBy { it.priority }


    suspend fun proceedMessage(message: RootMessage, editableMessage: EditableMessage) {
        for (handler in handlers) {
            val handle = handler.handle(message, editableMessage)
            if (handle) return
        }
    }
}