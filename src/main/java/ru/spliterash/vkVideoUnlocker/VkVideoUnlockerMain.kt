package ru.spliterash.vkVideoUnlocker

import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.client.actors.UserActor
import java.io.FileInputStream


fun main() {
    val splitRegEx = Regex("[\n\r]+")
    val groupAuthText = loadResource("group.txt")
        .split(splitRegEx)
    val groupActor = GroupActor(groupAuthText[0].trim().toInt(), groupAuthText[1].trim())
    val userAuthText = loadResource("user.txt")
        .split(splitRegEx)
    val userActor = UserActor(
        userAuthText[0].trim().toInt(),
        userAuthText[1].trim()
    )
    val app = VkVideoUnlocker(groupActor, userActor)
    app.start()
}

fun loadResource(name: String): String {
    return FileInputStream(name)
        .readAllBytes()
        .decodeToString()
}