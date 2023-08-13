package ru.spliterash.vkVideoUnlocker.longpoll.message

import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.AttachmentContainer

interface Message : AttachmentContainer {
    val attachments: List<Attachment>
    val fwdMessages: List<FwdMessage>
    val replyMessage: FwdMessage?
    val text: String?

    override fun containers(): List<AttachmentContainer> {
        val list = ArrayList<AttachmentContainer>(fwdMessages.size + 1)
        list.addAll(fwdMessages)

        replyMessage?.let {
            list.add(it)
        }

        return list
    }

    override fun attachments(): List<Attachment> {
        return attachments
    }
}