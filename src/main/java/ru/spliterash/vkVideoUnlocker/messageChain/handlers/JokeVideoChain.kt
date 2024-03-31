package ru.spliterash.vkVideoUnlocker.messageChain.handlers

import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.longpoll.message.RootMessage
import ru.spliterash.vkVideoUnlocker.longpoll.message.isPersonalChat
import ru.spliterash.vkVideoUnlocker.message.editableMessage.EditableMessage
import ru.spliterash.vkVideoUnlocker.message.utils.MessageUtils
import ru.spliterash.vkVideoUnlocker.messageChain.ActivationMessageHandler
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.actor.types.WorkUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi

@Singleton
class JokeVideoChain(
    @GroupUser private val group: VkApi,
    @WorkUser private val client: VkApi,
    private val utils: MessageUtils,
) : ActivationMessageHandler("блокировка", "заблокировать", "lock", "заблокируй") {
    override suspend fun handleAfterCheck(message: RootMessage, editableMessage: EditableMessage): Boolean {
        try {
            utils.scanForVideoContent(message)
        } catch (ex: VkUnlockerException) {
            handleException(ex, message)
            return false
        } ?: return false
        editableMessage.sendOrUpdate("Видео успешно заблокировано", "video-220040910_456259709")

        return true
    }

    private fun handleException(ex: VkUnlockerException, message: RootMessage) {
        if (message.isPersonalChat())
            throw ex
    }
}