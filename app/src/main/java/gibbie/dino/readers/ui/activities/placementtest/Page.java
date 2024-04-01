package gibbie.dino.readers.ui.activities.placementtest;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Page implements Serializable {
    @SerializedName("pageNumber")
    private int pageNumber;

    @SerializedName("isStoryPage")
    private boolean isStoryPage = true;

    @SerializedName("playAudio")
    private boolean playAudio = true;

    @SerializedName("fullText")
    private String fullText = "";

    @SerializedName("audioUrl")
    private String audioUrl = "";

    @SerializedName("imgUrl")
    private String imgUrl;

    @SerializedName("width")
    private double width;

    @SerializedName("height")
    private double height;

    @SerializedName("fontSpace")
    private double fontSpace;

    @SerializedName("lines")
    private List<Line> lines;

    public boolean isStoryPage() {
        return isStoryPage;
    }

    public void setStoryPage(boolean storyPage) {
        isStoryPage = storyPage;
    }

    public boolean isPlayAudio() {
        return playAudio;
    }

    public void setPlayAudio(boolean playAudio) {
        this.playAudio = playAudio;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public String getImgOriUrl() {
        return imgUrl.replace("images","images_ori");
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getFontSpace() {
        return fontSpace;
    }

    public void setFontSpace(double fontSpace) {
        this.fontSpace = fontSpace;
    }

    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }
}
