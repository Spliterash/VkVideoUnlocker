package ru.spliterash.vkVideoUnlocker.vkApiFix;


import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.Validable;

import java.util.List;

/**
 * GetResponse object
 */
public class CustomGetResponse implements Validable {
    /**
     * Total number
     */
    @SerializedName("count")
    private Integer count;

    @SerializedName("items")
    private List<BookmarkWithFullVideo> items;

    public Integer getCount() {
        return count;
    }

    public CustomGetResponse setCount(Integer count) {
        this.count = count;
        return this;
    }

    public List<BookmarkWithFullVideo> getItems() {
        return items;
    }

    public CustomGetResponse setItems(List<BookmarkWithFullVideo> items) {
        this.items = items;
        return this;
    }
}
