package ru.spliterash.vkVideoUnlocker.message.editableMessage

import ru.spliterash.vkVideoUnlocker.message.vkModels.request.Keyboard

interface EditableMessage {
    suspend fun sendOrUpdate(text: String? = null, attachments: String? = null, keyboard: Keyboard? = null)
}