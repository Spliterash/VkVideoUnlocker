import bridge from "@vkontakte/vk-bridge";

export const APP_ID = 52483593
export const VK_API_VERSION = "5.199"

export interface HashInput {
    id: string
    video: VideoInfo
}

export interface VideoInfo {
    id: string
    name: string
    preview: string
}

export interface VideoSaveRequest {
    // ID запроса в моей системе
    id: string
    userId: number
    groupId?: number
    // Куда перелить
    uploadUrl: string
}

export function closeApp() {
    bridge.send("VKWebAppClose")
}