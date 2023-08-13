package ru.spliterash.vkVideoUnlocker.longpoll.message

import com.fasterxml.jackson.annotation.JsonProperty
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.ISomethingWithAttachments
import ru.spliterash.vkVideoUnlocker.longpoll.message.attachments.SomethingWithAttachments

data class Message(
    @JsonProperty("attachments") val attachments: List<Attachment>,
    @JsonProperty("conversation_message_id") val conversationMessageId: Int,
    @JsonProperty("fwd_messages") val fwdMessages: List<SomethingWithAttachments> = listOf(),
    @JsonProperty("peer_id") val peerId: Int,
    @JsonProperty("reply_message") val replyMessage: SomethingWithAttachments?,
    @JsonProperty("text") val text: String?
) : ISomethingWithAttachments {
    override fun innerSomething(): List<ISomethingWithAttachments> {
        val list = ArrayList<SomethingWithAttachments>(fwdMessages.size + 1)
        list.addAll(fwdMessages)

        if (replyMessage != null)
            list.add(replyMessage)

        return list
    }

    override fun attachments(): List<Attachment> {
        return attachments
    }
}