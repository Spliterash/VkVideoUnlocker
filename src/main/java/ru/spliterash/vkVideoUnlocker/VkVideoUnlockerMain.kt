package ru.spliterash.vkVideoUnlocker

import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.client.actors.UserActor
import org.apache.commons.io.IOUtils
import ru.spliterash.utils.database.jdbc.types.mysql.hikari.HikariMySQLConnectionProvider
import ru.spliterash.utils.database.jdbc.types.mysql.hikari.HikariMySQLDatabase
import ru.spliterash.vkVideoUnlocker.storage.impl.MySQLVideoRepository
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.Executors

private const val CONFIG_FILENAME = "config.txt"

fun main() {
    val splitRegEx = Regex("[\n\r]+")
    val configText = loadResource(CONFIG_FILENAME)
    if (configText == null) {
        val stream = Thread
            .currentThread()
            .contextClassLoader
            .getResourceAsStream(CONFIG_FILENAME)

        IOUtils.copy(stream, FileOutputStream(CONFIG_FILENAME))
        return
    }
    val configTextSplit = configText.split(splitRegEx)
    val groupActor = GroupActor(
        configTextSplit[7].trim().toInt(),
        configTextSplit[8].trim()
    )
    val userActor = UserActor(
        configTextSplit[10].trim().toInt(),
        configTextSplit[11].trim()
    )
    val pokeUserActor = UserActor(
        -1,
        configTextSplit[13].trim()
    )
    Class.forName("org.mariadb.jdbc.Driver")
    val database = HikariMySQLDatabase(
        HikariMySQLConnectionProvider(
            "jdbc:mariadb://" + configTextSplit[1] + ":" + configTextSplit[2].toInt() + "/" + configTextSplit[5],
            configTextSplit[3],
            configTextSplit[4],
            "org.mariadb.jdbc.Driver"
        )
    )

    val app = VkVideoUnlocker(
        Executors.newFixedThreadPool(10),
        MySQLVideoRepository(database),
        groupActor,
        userActor,
        pokeUserActor
    )
    app.start()
}

fun loadResource(name: String): String? {
    val file = File(name)
    return if (!file.isFile)
        null
    else
        FileInputStream(name)
            .readAllBytes()
            .decodeToString()
}