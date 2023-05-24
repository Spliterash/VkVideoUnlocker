package ru.spliterash.vkVideoUnlocker.vkApiFix;

import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.Validable;
import com.vk.api.sdk.objects.annotations.Required;
import com.vk.api.sdk.objects.base.Link;
import com.vk.api.sdk.objects.fave.BookmarkType;
import com.vk.api.sdk.objects.fave.Tag;
import com.vk.api.sdk.objects.market.MarketItem;
import com.vk.api.sdk.objects.video.VideoFull;
import com.vk.api.sdk.objects.wall.WallpostFull;

import java.util.List;

/**
 * Bookmark object
 */
public class BookmarkWithFullVideo implements Validable {
    /**
     * Timestamp, when this item was bookmarked
     */
    @SerializedName("added_date")
    private Integer addedDate;

    @SerializedName("link")
    private Link link;

    @SerializedName("post")
    private WallpostFull post;

    @SerializedName("product")
    private MarketItem product;

    /**
     * Has user seen this item
     */
    @SerializedName("seen")
    @Required
    private Boolean seen;

    @SerializedName("tags")
    @Required
    private List<Tag> tags;

    /**
     * Item type
     */
    @SerializedName("type")
    @Required
    private BookmarkType type;

    @SerializedName("video")
    private VideoFullFix video;

    public Integer getAddedDate() {
        return addedDate;
    }

    public BookmarkWithFullVideo setAddedDate(Integer addedDate) {
        this.addedDate = addedDate;
        return this;
    }

    public Link getLink() {
        return link;
    }

    public BookmarkWithFullVideo setLink(Link link) {
        this.link = link;
        return this;
    }

    public WallpostFull getPost() {
        return post;
    }

    public BookmarkWithFullVideo setPost(WallpostFull post) {
        this.post = post;
        return this;
    }

    public MarketItem getProduct() {
        return product;
    }

    public BookmarkWithFullVideo setProduct(MarketItem product) {
        this.product = product;
        return this;
    }

    public Boolean getSeen() {
        return seen;
    }

    public BookmarkWithFullVideo setSeen(Boolean seen) {
        this.seen = seen;
        return this;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public BookmarkWithFullVideo setTags(List<Tag> tags) {
        this.tags = tags;
        return this;
    }

    public BookmarkType getType() {
        return type;
    }

    public BookmarkWithFullVideo setType(BookmarkType type) {
        this.type = type;
        return this;
    }

    public VideoFullFix getVideo() {
        return video;
    }

    public BookmarkWithFullVideo setVideo(VideoFullFix video) {
        this.video = video;
        return this;
    }
}
