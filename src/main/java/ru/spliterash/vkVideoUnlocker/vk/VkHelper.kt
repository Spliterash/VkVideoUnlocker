package ru.spliterash.vkVideoUnlocker.vk

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.inject.Singleton
import okhttp3.Response
import org.apache.commons.logging.LogFactory
import ru.spliterash.vkVideoUnlocker.vk.exceptions.VkApiException
import ru.spliterash.vkVideoUnlocker.vk.exceptions.VkNetworkException

private val log = LogFactory.getLog(VkHelper::class.java)

@Singleton
class VkHelper(
    private val mapper: ObjectMapper
) {
    fun <T> readResponse(response: Response, type: Class<T>): T {
        val node = checkAndGetResponse(response)

        return mapper.convertValue(node, type)
    }

    fun <T> readResponse(response: Response, type: TypeReference<T>): T {
        val node = checkAndGetResponse(response)

        return try {
            mapper.convertValue(node, type)
        } catch (ex: IllegalArgumentException) {
            log.warn("Failed convert ${node.toPrettyString()} to ${type.type}")
            throw ex
        }
    }

    @Throws(VkApiException::class)
    private fun checkAndGetResponse(response: Response): JsonNode {
        val raw = response.body.string()

        if (!response.isSuccessful)
            throw VkNetworkException(response.code, raw)

        val node = mapper.readValue<ObjectNode>(raw)

        val errorNode = node.get("error")
        if (errorNode != null) {
            val error = mapper.convertValue<VkError>(errorNode)

            throw VkApiException(error.code, error.msg)
        }

        return node.get("response")
    }
}
