package ru.spliterash.vkVideoUnlocker.longpoll

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
import jakarta.annotation.PreDestroy
import jakarta.inject.Singleton
import kotlinx.coroutines.*
import okhttp3.Request
import okhttp3.executeAsync
import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.common.okHttp.OkHttpFactory
import ru.spliterash.vkVideoUnlocker.longpoll.message.MessageNew
import ru.spliterash.vkVideoUnlocker.longpoll.message.reply
import ru.spliterash.vkVideoUnlocker.longpoll.vkModels.LongPollResponse
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi
import java.time.Duration

@Singleton
class LongPollService(
    @GroupUser private val vkApi: VkApi,
    private val mapper: ObjectMapper,
    private val messageChainService: MessageChainService,
    factory: OkHttpFactory
) : ApplicationEventListener<StartupEvent> {
    private val client = factory.create()
        .readTimeout(Duration.ofSeconds(30))
        .build()
    private var job: Job? = null
    override fun onApplicationEvent(event: StartupEvent) {
        job = CoroutineScope(Dispatchers.IO).launch { start() }
    }

    private suspend fun start() = coroutineScope {
        val supervisorScope = CoroutineScope(coroutineContext + SupervisorJob())

        while (true) {
            var (key, server, ts) = try {
                vkApi.groups.getLongPollServer()
            } catch (ex: Exception) {
                ex.printStackTrace()
                delay(10000)
                continue
            }

            while (true) {
                val url = "$server?act=a_check&key=$key&ts=$ts&wait=25"

                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                val raw = client
                    .newCall(request)
                    .executeAsync()
                    .body
                    .string()
                val mapped = mapper.readValue<LongPollResponse>(raw)

                for (update in mapped.updates) {
                    if (update.type != "message_new")
                        continue

                    val messageNew = mapper.convertValue<MessageNew>(update.body)
                    val message = messageNew.message

                    supervisorScope.launch {
                        try {
                            messageChainService.proceedMessage(message)
                        } catch (ex: VkUnlockerException) {
                            val info = ex.messageForUser()
                            message.reply(vkApi, info)
                        } catch (ex: Exception) {
                            message.reply(
                                vkApi,
                                "Произошла непредвиденная ошибка(${ex.javaClass.simpleName}): ${ex.message}"
                            )
                        }
                    }
                }

                if (mapped.ts == null)
                    break

                ts = mapped.ts
                ensureActive()
            }
            ensureActive()
        }
    }

    @PreDestroy
    fun destroy() {
        job?.cancel()
    }
}