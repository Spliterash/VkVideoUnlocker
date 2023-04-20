package ru.spliterash.vkVideoUnlocker.response;

import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.video.Video;

import java.util.Map;

public class RealVideoResponse extends Video {
    public Map<String, String> getFiles() {
        return files;
    }

    public void setFiles(Map<String, String> files) {
        this.files = files;
    }

    @SerializedName("files")
    private Map<String, String> files;
}
