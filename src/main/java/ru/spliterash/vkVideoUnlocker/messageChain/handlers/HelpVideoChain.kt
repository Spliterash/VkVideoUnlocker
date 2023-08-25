package ru.spliterash.vkVideoUnlocker.messageChain.handlers

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.longpoll.message.isGroupChat
import ru.spliterash.vkVideoUnlocker.longpoll.message.reply
import ru.spliterash.vkVideoUnlocker.messageChain.ActivationMessageHandler
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi

@Singleton
class HelpVideoChain(
    @GroupUser private val client: VkApi
) : ActivationMessageHandler(
    "помощь",
    "начать",
    "/начать",
    "help",
    "старт",
    "start",
    "/start",
    ".",
    "как тобой пользоваться"
) {
    override suspend fun handleAfterCheck(message: RootMessage) {
        if (message.isGroupChat())
            return

        message.reply(
            client,
            "Привет. Чтобы разблокировать видео, просто перешли мне что то, что его содержит. " +
                    "Например ты можешь отправить мне пост из ленты, комментарий из поста, переслать сообщение которое содержит видео, и так далее." +
                    "Если видео уже открытое, то я скажу тебе об этом, но только в личных сообщениях, чтобы не засорять груповые чаты.\n\n" +
                    "Кстати насчёт них. Ты можешь добавить меня в любой чат. После добавления я сразу буду реагировать на упоминания через @unlock_video.\n" +
                    "Если же администратор чата выдаст мне доступ ко всей переписке, я смогу разблокировать видео в автоматическом режиме\n\n" +
                    "Дополнительные возможности:\n" +
                    "* Скачивание видео: перешли мне видео как обычно и напиши в сообщении 'скачать'(без кавычек)\n" +
                    "* Сохранение историй: перешли мне историю на нужном слайде. Работает ТОЛЬКО в личных сообщениях из за ограничений API"
        )
    }
}