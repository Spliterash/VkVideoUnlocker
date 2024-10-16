package ru.spliterash.vkVideoUnlocker.message.vkModels.request

import com.fasterxml.jackson.annotation.JsonProperty

data class Keyboard(
    val buttons: List<List<Button>>,
    val inline: Boolean = true
) {
    data class Button(
        val action: Action
    ) {
        sealed interface Action {
            val type: String
        }

        data class OpenAppAction(
            @JsonProperty("app_id")
            val appId: String,
            val label: String,
            val hash: String,
        ) : Action {
            override val type = "open_app"
        }
    }
}