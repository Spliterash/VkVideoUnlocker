package ru.spliterash.vkVideoUnlocker.vk

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.inject.Singleton
import okhttp3.Response
import org.apache.commons.logging.LogFactory
import org.slf4j.Logger
import ru.spliterash.vkVideoUnlocker.vk.exceptions.VkApiException
import ru.spliterash.vkVideoUnlocker.vk.exceptions.VkNetworkException

private val log = LogFactory.getLog(VkHelper::class.java)

@Singleton
class VkHelper(
    private val mapper: ObjectMapper
) {
    @Throws(VkApiException::class)
    fun <T> readResponse(response: Response, type: Class<T>): Pair<T, String> {
        val raw = response.body.string()

        if (!response.isSuccessful)
            throw VkNetworkException(response.code, raw)

        val node = mapper.readValue<ObjectNode>(raw)

        val errorNode = node.get("error")
        if (errorNode != null) {
            val error = mapper.convertValue<VkError>(errorNode)

            throw VkApiException(error.code, error.msg)
        }
        val responseNode = node.get("response")
        try {
            val mapped = mapper.convertValue(responseNode, type)
            return mapped to raw
        } catch (ex: IllegalArgumentException) {
            log.warn("Failed to map ${type.name}, body:\n$raw", ex)
            throw ex
        }
    }
}
