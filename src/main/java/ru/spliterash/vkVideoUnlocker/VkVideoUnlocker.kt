package ru.spliterash.vkVideoUnlocker

import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.client.actors.UserActor
import com.vk.api.sdk.events.longpoll.GroupLongPollApi
import com.vk.api.sdk.exceptions.ApiException
import com.vk.api.sdk.httpclient.HttpTransportClient
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
import ru.spliterash.vkVideoUnlocker.exceptions.AlreadyExistException
import ru.spliterash.vkVideoUnlocker.exceptions.SelfVideoException
import ru.spliterash.vkVideoUnlocker.exceptions.VideoTooLongException
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import kotlin.random.Random
import kotlin.random.asJavaRandom

private val CACHE_FILE = File("videos.txt")

class VkVideoUnlocker(
    private val groupActor: GroupActor,
    private val userActor: UserActor,
) {
    private val activateKeyword = listOf(
        "[club220040910|@unlockdostup]",
        "разблоч",
        "разблокируй",
        "unlock",
        "пошёл нахуй со своим доступом",
        "пошел нахуй со своим доступом"
    )
    private val scope = CoroutineScope(
        Dispatchers.IO +
                CoroutineExceptionHandler { _, ex -> ex.printStackTrace() } +
                SupervisorJob()
    )
    private val client = VkApiClient(HttpTransportClient.getInstance())
    private val httpClient = HttpClients.custom()
        .setUserAgent("Java VK SDK/1.0")
        .build()
    private val groups = hashSetOf<Int>()
    private val random = Random.asJavaRandom()
    private val videoCache = hashMapOf<String, String>()

    fun refreshGroups() {
        val groupResponse = client
            .groups()
            .get(userActor)
            .count(1000)
            .execute()
        groups += groupResponse.items

    }

    fun start() {
        loadCache()
        refreshGroups()

        val poll = Poll(client, groupActor)
        poll.run()
    }

    private fun loadCache() {
        if (!CACHE_FILE.isFile)
            CACHE_FILE.createNewFile()
        val fileInputStream = FileInputStream(CACHE_FILE)
        val db = IOUtils.toString(fileInputStream, StandardCharsets.UTF_8)
        for (line in db.split(Regex("[\n|\r]+"))) {
            if (line.isBlank())
                continue

            val row = line.split(":")

            val original = row[0]
            val reuploaded = row[1]

            videoCache[original] = reuploaded
        }
    }

    private fun addToCache(original: String, reupload: String) {
        videoCache[original] = reupload
        val line = "$original:$reupload\n"


        val fileOutputStream = FileOutputStream(CACHE_FILE, true)
        fileOutputStream.write(line.encodeToByteArray())
        fileOutputStream.close()
    }

    private fun findVideoUrl(id: String): String? {
        val cachedVideo = videoCache[id]
        if (cachedVideo != null)
            throw AlreadyExistException(cachedVideo)

        val split = id.split("_")
        val ownerId = split[0]
        val videoIdInt = split[1].toInt()
        val ownerIdInt = ownerId.toInt()
        if (ownerId.startsWith("-")) {
            val normalGroupId = -ownerIdInt
            if (normalGroupId == groupActor.groupId)
                throw SelfVideoException()
            checkAndJoin(normalGroupId)
        }
        try {
            client
                .videos()
                .add(userActor, videoIdInt, ownerIdInt)
                .execute()
        } catch (ex: ApiException) {
            if (ex.code != 800)
                throw ex
        }
        val myVideos = client
            .videos()
            .get(userActor)
            .count(10)
            .setHeaders(
                arrayOf(
                    BasicHeader(
                        "user-agent",
                        "KateMobileAndroid/99 lite-535 (Android 11; SDK 30; arm64-v8a; asus Zenfone Max Pro M1; ru)"
                    )
                )
            )
            .execute()

        scope.launch {
            client
                .videos()
                .delete(userActor, videoIdInt)
                .ownerId(ownerIdInt)
                .targetId(userActor.id)
                .execute()
        }

        val video = myVideos
            .items
            .firstOrNull { it.id == videoIdInt && it.ownerId == ownerIdInt }
            ?: return null
        if (video.duration > 60 * 5)
            throw VideoTooLongException()

        // Пошёл кринж
        val url = if (video.files.mp4720 != null)
            video.files.mp4720
        else if (video.files.mp4480 != null)
            video.files.mp4480
        else if (video.files.mp4360 != null)
            video.files.mp4360
        else if (video.files.mp4240 != null)
            video.files.mp4240
        else
            null

        return url.toString()
    }

    private fun checkAndJoin(group: Int) {
        if (groups.contains(group))
            return

        client
            .groups()
            .join(userActor)
            .groupId(group)
            .execute()

        groups += group
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


    fun reUploadAndSend(peerId: Int, video: String, messageId: Int) = scope.launch {
        val videoUrl: String? = try {
            findVideoUrl(video)
        } catch (ex: SelfVideoException) {
            sendMessage(peerId, "Зачем ты мне мои же видосы кидаешь ?", messageId)
            return@launch
        } catch (ex: AlreadyExistException) {
            sendMessage(peerId, "Этот видос уже разблокирован", messageId, "video${ex.cachedVideoId}")
            return@launch
        } catch (ex: VideoTooLongException) {
            sendMessage(
                peerId,
                "Извини, но я не перезаливаю видео длиннее 5 минут",
                messageId
            )
            return@launch
        } catch (ex: Exception) {
            sendMessage(
                peerId,
                "Ошибка получения ссылки на видео(${ex.javaClass.simpleName}): ${ex.message}",
                messageId
            )
            return@launch
        }
        if (videoUrl == null)
            sendMessage(peerId, "Невозможно получить видео", messageId)

        val notifyJob = launch {
            delay(3000)
            sendMessage(peerId, "Видео обрабатывается дольше чем обычно, я не завис", messageId)
        }
        val uploadedId = httpClient.execute(HttpGet(videoUrl)).use { downloadResponse ->
            val downloadVideoStream = downloadResponse.entity.content

            upload(video, downloadVideoStream)
        }
        notifyJob.cancel()
        addToCache(video, uploadedId)
        sendMessage(peerId, "Готово", messageId, "video$uploadedId")

    }

    private fun upload(name: String, inputStream: InputStream): String {
        val file = Files.createTempFile("suka", "blyat").toFile()
        try {
            val output = file.outputStream()
            IOUtils.copy(inputStream, output)
            output.close()

            val response = client.videos()
                .save(userActor)
                .groupId(groupActor.groupId)
                .name(name)
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
        actor: GroupActor
    ) : GroupLongPollApi(
        client,
        actor,
        15
    ) {
        override fun messageNew(groupId: Int, message: Message) {
            val doICare = if (message.peerId > 2000000000) {
                val trimmedText = message.text.trimStart().lowercase()
                activateKeyword.any { trimmedText.startsWith(it) }
            } else true
            if (!doICare)
                return
            var video: Video? = null
            val wallAttachment = message.attachments?.firstOrNull { it.wall != null }?.wall
            if (wallAttachment != null) {
                video = wallAttachment.attachments.firstOrNull { it.video != null }?.video
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

            reUploadAndSend(message.peerId, "${video.ownerId}_${video.id}", message.conversationMessageId)
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
            }
            return null
        }
    }
}