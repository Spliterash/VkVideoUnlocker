package ru.spliterash.vkVideoUnlocker.longpoll.message

import ru.spliterash.vkVideoUnlocker.vk.api.VkApi

suspend inline fun RootMessage.reply(client: VkApi, text: String? = null, attachments: String? = null) =
    client.messages.sendMessage(peerId, text, conversationMessageId, attachments)

fun RootMessage.isGroupChat(): Boolean = peerId > 2000000000
fun RootMessage.isPersonalChat(): Boolean = peerId < 2000000000