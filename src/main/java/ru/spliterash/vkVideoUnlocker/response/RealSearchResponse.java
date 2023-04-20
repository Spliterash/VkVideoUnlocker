package ru.spliterash.vkVideoUnlocker.response;

import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.Validable;
import com.vk.api.sdk.objects.annotations.Required;

import java.util.List;

public class RealSearchResponse implements Validable {
    /**
     * Total number
     */
    @SerializedName("count")
    @Required
    private Integer count;

    @SerializedName("items")
    @Required
    private List<RealVideoResponse> items;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<RealVideoResponse> getItems() {
        return items;
    }

    public void setItems(List<RealVideoResponse> items) {
        this.items = items;
    }
}