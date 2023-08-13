package ru.spliterash.vkVideoUnlocker.messageChain

import ru.spliterash.vkVideoUnlocker.longpoll.message.Message

interface MessageHandler {
    val priority: Int
        get() = 0

    /**
     * FALSE Не его остановка, спросим следующих
     * TRUE Да, это был его случай, дальше не кидаем
     */
    suspend fun handle(message: Message): Boolean
}