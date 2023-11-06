package ru.spliterash.vkVideoUnlocker.messageChain

import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.message.editableMessage.EditableMessage

abstract class ActivationMessageHandler(
    vararg words: String
) : MessageHandler {
    private val activationWords = setOf(*words)
    override suspend fun handle(message: RootMessage, editableMessage: EditableMessage): Boolean {
        message.text ?: return false

        val lowercase = message
            .text
            .trim()
            .lowercase()

        if (!activationWords.contains(lowercase))
            return false

        return handleAfterCheck(message, editableMessage)
    }

    abstract suspend fun handleAfterCheck(message: RootMessage, editableMessage: EditableMessage): Boolean
}