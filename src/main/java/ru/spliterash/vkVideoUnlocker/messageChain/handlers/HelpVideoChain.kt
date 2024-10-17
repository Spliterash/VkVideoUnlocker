package ru.spliterash.vkVideoUnlocker.messageChain.handlers

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.longpoll.message.isGroupChat
import ru.spliterash.vkVideoUnlocker.message.editableMessage.EditableMessage
import ru.spliterash.vkVideoUnlocker.messageChain.ActivationMessageHandler

@Singleton
class HelpVideoChain : ActivationMessageHandler(
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
    override suspend fun handleAfterCheck(message: RootMessage, editableMessage: EditableMessage): Boolean {
        if (message.isGroupChat())
            return false
        if (message.attachments.isNotEmpty())
            return false
        if (message.containers().isNotEmpty())
            return false

        editableMessage.sendOrUpdate(
            "Привет. Чтобы разблокировать видео, просто перешли мне что то, что его содержит. " +
                    "Например ты можешь отправить мне пост из ленты, комментарий из поста, переслать сообщение которое содержит видео, и так далее." +
                    "Если видео уже открытое, то я скажу тебе об этом, но только в личных сообщениях, чтобы не засорять груповые чаты.\n\n" +
                    "Кстати насчёт них. Ты можешь добавить меня в любой чат. После добавления я сразу буду реагировать на упоминания через @unlock_video.\n" +
                    "Если же администратор чата выдаст мне доступ ко всей переписке, я смогу разблокировать видео в автоматическом режиме\n\n" +
                    "Дополнительные возможности:\n" +
                    "* Скачивание видео: перешли мне видео как обычно и напиши в сообщении 'скачать'(без кавычек)\n" +
                    "* Сохранение историй: перешли мне историю на нужном слайде\n" +
                    "* Перезалив из тиктока: просто пришли ссылку\n" +
                    "* Сохранение видео на свой аккаунт: прикрепи видео и напиши 'сохранить'"
        )

        return true;
    }
}