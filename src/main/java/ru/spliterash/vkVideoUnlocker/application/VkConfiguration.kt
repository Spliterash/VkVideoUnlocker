package ru.spliterash.vkVideoUnlocker.application

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Value
import ru.spliterash.vkVideoUnlocker.vk.actor.GroupUser
import ru.spliterash.vkVideoUnlocker.vk.actor.types.Actor
import ru.spliterash.vkVideoUnlocker.vk.actor.types.DownloadUser
import ru.spliterash.vkVideoUnlocker.vk.actor.types.PokeUser
import ru.spliterash.vkVideoUnlocker.vk.actor.types.UploadUser
import ru.spliterash.vkVideoUnlocker.vk.api.VkApi
import ru.spliterash.vkVideoUnlocker.vk.api.VkApiImpl

@Factory
class VkConfiguration(
    private val context: ApplicationContext,
) {
    @Bean
    @DownloadUser
    fun downloadUserActor(
        @Value("\${vk-unlocker.download-user.token}") token: String,
    ): VkApi = context.createBean(VkApiImpl::class.java, Actor(-1, token))

    @Bean
    @UploadUser
    fun uploadUserActor(
        @Value("\${vk-unlocker.upload-user.token}") token: String,
    ): VkApi = context.createBean(VkApiImpl::class.java, Actor(-1, token))

    @Bean
    @PokeUser
    fun pokeUserActor(
        @Value("\${vk-unlocker.poke-user.token}") token: String
    ): VkApi = context.createBean(VkApiImpl::class.java, Actor(-1, token))

    @Bean
    @GroupUser
    fun groupActor(
        @Value("\${vk-unlocker.group.id}") id: Long,
        @Value("\${vk-unlocker.group.token}") token: String
    ): VkApi = context.createBean(VkApiImpl::class.java, Actor(id, token))
}