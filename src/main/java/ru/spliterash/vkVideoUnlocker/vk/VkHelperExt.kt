package ru.spliterash.vkVideoUnlocker.vk

import com.fasterxml.jackson.core.type.TypeReference
import okhttp3.Response

fun <T> Response.readResponse(helper: VkHelper, type: Class<T>) = helper.readResponse(this, type)
fun <T> Response.readResponse(helper: VkHelper, type: TypeReference<T>) = helper.readResponse(this, type)