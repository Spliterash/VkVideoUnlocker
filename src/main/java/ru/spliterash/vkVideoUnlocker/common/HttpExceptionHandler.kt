package ru.spliterash.vkVideoUnlocker.common

import io.micronaut.core.io.ResourceLoader
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton
import ru.spliterash.vkVideoUnlocker.common.exceptions.VkUnlockerException

@Singleton
class HttpExceptionHandler(
    loader: ResourceLoader
) : ExceptionHandler<VkUnlockerException, HttpResponse<String>> {

    private val template = loader.getResourceAsStream("error.html")
        .orElseThrow()
        .readAllBytes()
        .decodeToString()

    override fun handle(request: HttpRequest<*>, exception: VkUnlockerException): HttpResponse<String> {
        val html = template.replace("{{error}}", exception.messageForUser())

        return HttpResponse
            .ok(html)
            .header("Content-Type", "text/html")
    }
}