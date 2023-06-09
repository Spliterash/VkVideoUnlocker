// Autogenerated from vk-api-schema. Please don't edit it manually.
package ru.spliterash.vkVideoUnlocker.vkApiFix;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.Validable;
import java.net.URI;
import java.util.Objects;

/**
 * VideoFilesFix object
 */
public class VideoFilesFix implements Validable {
    /**
     * URL of the external player
     */
    @SerializedName("external")
    private URI external;
    /**
     * URL of the mpeg4 file with 144p quality
     */
    @SerializedName("mp4_144")
    private URI mp4144;

    public URI getMp4144() {
        return mp4144;
    }

    public void setMp4144(URI mp4144) {
        this.mp4144 = mp4144;
    }

    /**
     * URL of the mpeg4 file with 240p quality
     */
    @SerializedName("mp4_240")
    private URI mp4240;

    /**
     * URL of the mpeg4 file with 360p quality
     */
    @SerializedName("mp4_360")
    private URI mp4360;

    /**
     * URL of the mpeg4 file with 480p quality
     */
    @SerializedName("mp4_480")
    private URI mp4480;

    /**
     * URL of the mpeg4 file with 720p quality
     */
    @SerializedName("mp4_720")
    private URI mp4720;

    /**
     * URL of the mpeg4 file with 1080p quality
     */
    @SerializedName("mp4_1080")
    private URI mp41080;

    /**
     * URL of the mpeg4 file with 2K quality
     */
    @SerializedName("mp4_1440")
    private URI mp41440;

    /**
     * URL of the mpeg4 file with 4K quality
     */
    @SerializedName("mp4_2160")
    private URI mp42160;

    /**
     * URL of the flv file with 320p quality
     */
    @SerializedName("flv_320")
    private URI flv320;

    public URI getExternal() {
        return external;
    }

    public VideoFilesFix setExternal(URI external) {
        this.external = external;
        return this;
    }

    public URI getMp4240() {
        return mp4240;
    }

    public VideoFilesFix setMp4240(URI mp4240) {
        this.mp4240 = mp4240;
        return this;
    }

    public URI getMp4360() {
        return mp4360;
    }

    public VideoFilesFix setMp4360(URI mp4360) {
        this.mp4360 = mp4360;
        return this;
    }

    public URI getMp4480() {
        return mp4480;
    }

    public VideoFilesFix setMp4480(URI mp4480) {
        this.mp4480 = mp4480;
        return this;
    }

    public URI getMp4720() {
        return mp4720;
    }

    public VideoFilesFix setMp4720(URI mp4720) {
        this.mp4720 = mp4720;
        return this;
    }

    public URI getMp41080() {
        return mp41080;
    }

    public VideoFilesFix setMp41080(URI mp41080) {
        this.mp41080 = mp41080;
        return this;
    }

    public URI getMp41440() {
        return mp41440;
    }

    public VideoFilesFix setMp41440(URI mp41440) {
        this.mp41440 = mp41440;
        return this;
    }

    public URI getMp42160() {
        return mp42160;
    }

    public VideoFilesFix setMp42160(URI mp42160) {
        this.mp42160 = mp42160;
        return this;
    }

    public URI getFlv320() {
        return flv320;
    }

    public VideoFilesFix setFlv320(URI flv320) {
        this.flv320 = flv320;
        return this;
    }
}
