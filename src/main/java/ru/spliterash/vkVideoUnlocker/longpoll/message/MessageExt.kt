package ru.spliterash.vkVideoUnlocker.longpoll.message

import ru.spliterash.vkVideoUnlocker.vk.api.VkApi

fun RootMessage.isGroupChat(): Boolean = peerId > 2000000000
fun RootMessage.isPersonalChat(): Boolean = peerId < 2000000000

fun RootMessage.hasPing(groupClient: VkApi): Boolean {
    if (text.isNullOrBlank()) return false
    return text.contains("[club${groupClient.id}|")
}