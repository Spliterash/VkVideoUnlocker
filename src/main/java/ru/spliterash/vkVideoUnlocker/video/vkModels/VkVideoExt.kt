package ru.spliterash.vkVideoUnlocker.video.vkModels

fun VkVideo.publicId() = "${ownerId}_$id"
fun VkVideo.fullId(): String {
    var id = publicId()
    if (accessKey != null) id = id + "_" + accessKey

    return id
}