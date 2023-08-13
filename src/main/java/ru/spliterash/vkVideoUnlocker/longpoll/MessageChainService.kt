package ru.spliterash.vkVideoUnlocker.longpoll

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.messageChain.MessageHandler

@Singleton
class MessageChainService(
    handlers: List<MessageHandler>
) {
    private val handlers = handlers
        .sortedBy { it.priority }


    suspend fun proceedMessage(message: RootMessage) {
        for (handler in handlers) {
            val handle = handler.handle(message)
            if (handle) return
        }
    }
}