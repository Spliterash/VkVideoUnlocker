package ru.spliterash.vkVideoUnlocker.application

import io.micronaut.context.annotation.Factory
import io.micronaut.runtime.Micronaut

object VkUnlockerApplication {
    @JvmStatic
    fun main(vararg args: String) {
        Micronaut.build(*args)
            .eagerInitSingletons(true)
            .start()
    }
}