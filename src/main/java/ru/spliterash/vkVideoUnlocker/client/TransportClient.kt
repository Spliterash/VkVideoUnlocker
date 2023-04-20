package ru.spliterash.vkVideoUnlocker.client

import com.vk.api.sdk.client.ClientResponse
import com.vk.api.sdk.httpclient.HttpTransportClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.InputStreamBody
import java.io.InputStream

class TransportClient : HttpTransportClient() {

    fun post(url: String, fileName: String, stream: InputStream): ClientResponse {
        val request = HttpPost(url)
        val fileBody = InputStreamBody(stream, fileName)
        val entity = MultipartEntityBuilder
            .create()
            .addPart(fileName, fileBody).build()
        request.entity = entity
        return callWithStatusCheck(request)
    }
}