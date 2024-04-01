package gibbie.dino.readers.ui.fragments.ownstory;

import android.graphics.Color;

public class BookTemplateModel {
    private int id;
    private String name;
    private String thumbnail;
    private int textColor;
    private int textShadowColor;

    public BookTemplateModel(int id, String name, String thumbnail, int textColor, int textShadowColor) {
        this.id = id;
        this.name = name;
        this.thumbnail = thumbnail;
        this.textColor = textColor;
        this.textShadowColor = textShadowColor;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public int getTextColor() {
        return textColor;
    }

    public int getTextShadowColor() {
        return textShadowColor;
    }
}
