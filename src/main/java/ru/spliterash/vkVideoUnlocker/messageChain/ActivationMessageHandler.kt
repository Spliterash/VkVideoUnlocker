package ru.spliterash.vkVideoUnlocker.messageChain

import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage

abstract class ActivationMessageHandler(
    vararg words: String
) : MessageHandler {
    private val activationWords = setOf(*words)
    override suspend fun handle(message: RootMessage): Boolean {
        message.text ?: return false

        val lowercase = message
            .text
            .trim()
            .lowercase()

        if (!activationWords.contains(lowercase))
            return false

        handleAfterCheck(message)

        return true
    }

    abstract suspend fun handleAfterCheck(message: RootMessage)
}