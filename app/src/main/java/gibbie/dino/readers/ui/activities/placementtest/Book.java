package gibbie.dino.readers.ui.activities.placementtest;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Book {
    @SerializedName("creator")
    private String creator;

    @SerializedName("pages")
    private List<Page> pages;

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }
}

