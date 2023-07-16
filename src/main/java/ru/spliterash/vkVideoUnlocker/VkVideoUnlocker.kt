package ru.spliterash.vkVideoUnlocker

import com.vk.api.sdk.client.AbstractQueryBuilder
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.client.actors.UserActor
import com.vk.api.sdk.exceptions.ApiException
import com.vk.api.sdk.httpclient.HttpTransportClient
import com.vk.api.sdk.objects.fave.GetItemType
import com.vk.api.sdk.objects.groups.GroupFull
import com.vk.api.sdk.objects.groups.GroupIsClosed
import com.vk.api.sdk.objects.messages.ForeignMessage
import com.vk.api.sdk.objects.messages.Forward
import com.vk.api.sdk.objects.messages.Message
import com.vk.api.sdk.objects.messages.MessageAttachment
import com.vk.api.sdk.objects.video.Video
import kotlinx.coroutines.*
import org.apache.commons.io.IOUtils
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.message.BasicHeader
import ru.spliterash.vkVideoUnlocker.exceptions.BookmarkNotFoundException
import ru.spliterash.vkVideoUnlocker.exceptions.SelfVideoException
import ru.spliterash.vkVideoUnlocker.exceptions.VideoFilesEmptyException
import ru.spliterash.vkVideoUnlocker.exceptions.VideoTooLongException
import ru.spliterash.vkVideoUnlocker.objects.VideoSearchResult
import ru.spliterash.vkVideoUnlocker.storage.VideoEntity
import ru.spliterash.vkVideoUnlocker.storage.VideoRepository
import ru.spliterash.vkVideoUnlocker.vkApiFix.FaveGetQueryWithFullVideo
import ru.spliterash.vkVideoUnlocker.vkApiFix.FixedLongPollApi
import java.io.InputStream
import java.nio.file.Files
import java.util.*
import java.util.concurrent.ExecutorService
import kotlin.random.Random
import kotlin.random.asJavaRandom

class VkVideoUnlocker(
    executor: ExecutorService,
    private val repository: VideoRepository,
    private val groupActor: GroupActor,
    private val userActor: UserActor,
    private val pokeUserActor: UserActor,
) {
    private val scope = CoroutineScope(
        executor.asCoroutineDispatcher() +
                CoroutineExceptionHandler { _, ex -> ex.printStackTrace() } +
                SupervisorJob()
    )
    private val client = VkApiClient(HttpTransportClient.getInstance())
    private val httpClient = HttpClients.custom()
        .setUserAgent("Java VK SDK/1.0")
        .build()
    private val groups = hashMapOf<Int, GroupFull>()
    private val random = Random.asJavaRandom()
    private val inProgress = Collections.synchronizedSet(hashSetOf<String>())

    fun refreshGroups() {
        val groupResponse = client
            .groups()
            .getObjectExtended(userActor)
            .count(1000)
            .extended(true)
            .execute()
        groups.clear()

        for (item in groupResponse.items) {
            groups[item.id] = item
        }

    }

    fun start() {
        refreshGroups()

        val poll = Poll(client)
        poll.run(groupActor)
    }

    private suspend fun findVideoUrl(id: String): VideoSearchResult {
        val split = id.split("_")
        val ownerId = split[0]
        val videoIdInt = split[1].toInt()
        val ownerIdInt = ownerId.toInt()
        var privateVideo = false
        if (ownerId.startsWith("-")) {
            val normalGroupId = -ownerIdInt
            if (normalGroupId == groupActor.groupId)
                throw SelfVideoException()
            val group = checkAndJoin(normalGroupId)
            if (group.isClosed != GroupIsClosed.OPEN)
                privateVideo = true
        }
        try {
            client
                .fave()
                .addVideo(userActor, ownerIdInt, videoIdInt)
                .execute()
        } catch (ex: ApiException) {
            if (ex.code != 800)
                throw ex
        }
        val myVideos = FaveGetQueryWithFullVideo(client, userActor)
            .itemType(GetItemType.VIDEO)
            .count(10)
            .imAsus()
            .execute()

        scope.launch {
            delay(500)
            client
                .fave()
                .removeVideo(userActor, ownerIdInt, videoIdInt)
                .execute()
        }

        val video = myVideos
            .items
            .firstOrNull { it.video != null && it.video.id == videoIdInt && it.video.ownerId == ownerIdInt }
            ?.video
            ?: throw BookmarkNotFoundException()
        if (video.duration > 60 * 5)
            throw VideoTooLongException()
        if (video.files == null)
            throw VideoFilesEmptyException()

        // Пошёл кринж
        val url = if (video.files.mp41080 != null)
            video.files.mp41080
        else if (video.files.mp4720 != null)
            video.files.mp4720
        else if (video.files.mp4480 != null)
            video.files.mp4480
        else if (video.files.mp4360 != null)
            video.files.mp4360
        else if (video.files.mp4240 != null)
            video.files.mp4240
        else if (video.files.mp4144 != null)
            video.files.mp4144
        else
            throw VideoFilesEmptyException()

        return VideoSearchResult(url.toString(), privateVideo);
    }

    private fun videoIsPrivate(videoId: String): Boolean {
        val response = client
            .videos()
            .get(pokeUserActor)
            .videos(videoId)
            .extended(true)
            .fields("privacy_view")
            .executeAsString()

        // Мне впдалу это парсить вручную
        return response.contains("\"content_restricted\":1");
    }

    private fun <T : AbstractQueryBuilder<*, *>> T.imAsus(): T {
        setHeaders(
            arrayOf(
                BasicHeader(
                    "user-agent",
                    "KateMobileAndroid/99 lite-535 (Android 11; SDK 30; arm64-v8a; asus Zenfone Max Pro M1; ru)"
                )
            )
        )
        return this
    }

    private fun checkAndJoin(group: Int): GroupFull {
        val joinedGroup = groups[group]
        if (joinedGroup != null)
            return joinedGroup

        client
            .groups()
            .join(userActor)
            .groupId(group)
            .execute()

        refreshGroups()


        return groups[group]!!
    }

    fun sendMessage(peerId: Int, text: String, replyTo: Int, vararg attachments: String) {
        val forward = Forward()

        forward.conversationMessageIds = listOf(replyTo)
        forward.peerId = peerId
        forward.isReply = true

        val request = client
            .messages()
            .send(groupActor)
            .message(text)
            .peerId(peerId)
            .randomId(random.nextInt())
            .forward(forward)
        if (attachments.isNotEmpty())
            request.attachment(attachments.joinToString(","))

        request.execute()
    }


    suspend fun reUploadAndSendIfNeed(peerId: Int, video: String, messageId: Int) = coroutineScope {
        val videoEntity = repository.findVideo(video)
        if (videoEntity != null) {
            if (videoEntity.status == VideoEntity.Status.UNLOCKED) {
                sendMessage(peerId, "Этот видос уже разблокирован", messageId, "video${videoEntity.unlockedId}")
                return@coroutineScope
            }
            // status OPEN, нам не надо запариваться
            return@coroutineScope
        }

        try {
            if (!videoIsPrivate(video)) {
                addAsOpen(video)
                return@coroutineScope
            }
        } catch (ex: Exception) {
            sendMessage(peerId, "Ошибка при получении статуса видео(${ex.javaClass}): ${ex.message}", messageId)
            ex.printStackTrace()
            return@coroutineScope
        }
        // Если видео закрытое и мы о нём не знаем, начинается гомоёбля

        if (!inProgress.add(video)) {
            sendMessage(
                peerId,
                "Видео уже обрабатывается в другой беседе, а делать уведомление о готовности мне крайне в падлу, поэтому отправь этот видос ещё раз через минуту, спасибо за понимание",
                peerId
            )
            return@coroutineScope
        }

        try {
            val videoResponse = try {
                findVideoUrl(video)
            } catch (ex: BookmarkNotFoundException) {
                sendMessage(
                    peerId,
                    "Не удалось найти видео в закладах, напишите автору бота об этой проблеме",
                    messageId
                )
                return@coroutineScope
            } catch (ex: SelfVideoException) {
                sendMessage(peerId, "Зачем ты мне мои же видосы кидаешь ?", messageId)
                return@coroutineScope
            } catch (ex: VideoTooLongException) {
                sendMessage(
                    peerId,
                    "Извини, но я не перезаливаю видео длиннее 5 минут",
                    messageId
                )
                return@coroutineScope
            } catch (ex: VideoFilesEmptyException) {
                sendMessage(
                    peerId,
                    "Видос получили, а файлов нет",
                    messageId
                )
                return@coroutineScope
            } catch (ex: Exception) {
                sendMessage(
                    peerId,
                    "Ошибка получения ссылки на видео(${ex.javaClass.simpleName}): ${ex.message}",
                    messageId
                )
                return@coroutineScope
            }
            val notifyJob = launch {
                delay(3000)
                sendMessage(peerId, "Видео обрабатывается дольше чем обычно, я не завис", messageId)
                delay(3000)
                sendMessage(peerId, "Да да, всё ещё обрабатывается, потерпи чуть чуть", messageId)
                delay(5000)
                sendMessage(peerId, "Я не знаю что ты туда положил, но оно всё ещё обрабатывается", messageId)
                delay(10000)
                sendMessage(peerId, "ТЫ ТАМ ЧТО, 99 ЧАСОВОЙ ВИДОС КРИПЕРА ПЕРЕЗАЛИВАЕШЬ ?!?!?!?!", messageId)
                delay(1000)
                while (true) {
                    sendMessage(peerId, "А может быть и завис....", messageId)
                    delay(2000)
                }
            }
            val uploadedId = httpClient.execute(HttpGet(videoResponse.url)).use { downloadResponse ->
                val downloadVideoStream = downloadResponse.entity.content

                upload(video, downloadVideoStream, videoResponse.privateVideo)
            }
            notifyJob.cancel()
            sendMessage(peerId, "Готово", messageId, "video$uploadedId")
            addAsUnlocked(video, uploadedId)
        } finally {
            inProgress.remove(video)
        }
    }

    private suspend fun addAsOpen(video: String) {
        val entity = VideoEntity(video, VideoEntity.Status.OPEN)

        repository.save(entity)
    }

    private suspend fun addAsUnlocked(originalId: String, unlockedId: String) {
        val entity = VideoEntity(originalId, VideoEntity.Status.UNLOCKED, unlockedId)

        repository.save(entity)
    }

    private fun upload(name: String, inputStream: InputStream, private: Boolean = false): String {
        val file = Files.createTempFile("suka", "blyat").toFile()
        try {
            val output = file.outputStream()
            IOUtils.copy(inputStream, output)
            output.close()

            val response = client.videos()
                .save(userActor)
                .groupId(groupActor.groupId)
                .name(name)
                .isPrivate(private)
                .execute()
            val url = response.uploadUrl


            val id = client
                .upload()
                .video(url.toString(), file)
                .execute()
                .videoId

            return "-${groupActor.groupId}_${id}"
        } finally {
            file.delete()
        }
    }

    private inner class Poll(
        client: VkApiClient,
    ) : FixedLongPollApi(
        client,
        15
    ) {
        override fun messageNew(groupId: Int, message: Message) {
            try {
                var video: Video? = null
                val attachments = message.attachments
                if (attachments != null) {
                    video = attachments.firstOrNull { it.video != null }?.video
                    val wallAttachment = attachments.firstOrNull { it.wall != null }?.wall
                    if (wallAttachment != null) {
                        video = wallAttachment.attachments.firstOrNull { it.video != null }?.video
                    }
                    if (video == null) {
                        val reply = attachments.firstOrNull { it.wallReply != null }?.wallReply
                        video = reply?.attachments?.firstOrNull { it.video != null }?.video
                    }
                }
                if (video == null && message.replyMessage != null)
                    video = scanForVideo(message.replyMessage)
                if (video == null)
                    video = run {
                        if (message.fwdMessages == null)
                            return

                        for (fwdMessage in message.fwdMessages) {
                            scanForVideo(fwdMessage)?.let {
                                return@run it
                            }
                        }
                        return@run null
                    }

                if (video == null)
                    return
                scope.launch {
                    try {
                        reUploadAndSendIfNeed(
                            message.peerId,
                            "${video.ownerId}_${video.id}",
                            message.conversationMessageId
                        )
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        private fun scanForVideo(foreignMessage: ForeignMessage): Video? {
            scanAttachment(foreignMessage.attachments)?.let { return it }

            scanAttachment(foreignMessage.replyMessage?.attachments)?.let { return it }

            if (foreignMessage.fwdMessages == null)
                return null
            for (fwdMessage in foreignMessage.fwdMessages) {
                val video = scanForVideo(fwdMessage)
                if (video != null)
                    return video
            }
            return null
        }

        private fun scanAttachment(attachments: List<MessageAttachment>?): Video? {
            if (attachments == null)
                return null
            for (attachment in attachments) {
                if (attachment.video != null)
                    return attachment.video
                if (attachment.wall != null) {
                    val video = attachment.wall.attachments.firstOrNull { it.video != null }?.video
                    if (video != null)
                        return video
                }
                if (attachment.wallReply != null) {
                    val video = attachment.wallReply.attachments?.firstOrNull { it.video != null }?.video
                    if (video != null)
                        return video
                }
            }
            return null
        }
    }
}