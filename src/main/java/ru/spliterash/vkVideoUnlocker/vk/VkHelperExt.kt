package ru.spliterash.vkVideoUnlocker.vk

import okhttp3.Response

fun <T> Response.readResponse(helper: VkHelper, type: Class<T>) = helper.readResponse(this, type)