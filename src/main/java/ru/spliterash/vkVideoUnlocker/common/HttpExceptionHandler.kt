package ru.spliterash.vkVideoUnlocker.common

import io.micronaut.core.io.ResourceLoader
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException
import ru.spliterash.vkVideoUnlocker.common.response.ErrorResponse

@Singleton
class HttpExceptionHandler(
    loader: ResourceLoader
) : ExceptionHandler<Exception, HttpResponse<*>> {

    private val template = loader.getResourceAsStream("error.html")
        .orElseThrow()
        .readAllBytes()
        .decodeToString()

    override fun handle(request: HttpRequest<*>, exception: Exception): HttpResponse<*> {
        val (code, message) = if (exception is VkUnlockerException)
            exception.javaClass.simpleName to exception.messageForUser()
        else {
            exception.printStackTrace()
            "UNKNOWN" to "(${exception.javaClass.name})${exception.message}"
        }

        return if (request.contentType.orElse(null)?.equals(MediaType.APPLICATION_JSON_TYPE) == true) {
            HttpResponse
                .badRequest(ErrorResponse(code, message))
                .contentType(MediaType.APPLICATION_JSON)
        } else {
            HttpResponse
                .badRequest(
                    template
                        .replace("{{code}}", code)
                        .replace("{{error}}", message)
                )
                .contentType(MediaType.TEXT_HTML)
        }
    }
}