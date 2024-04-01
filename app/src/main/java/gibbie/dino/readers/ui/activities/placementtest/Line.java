package gibbie.dino.readers.ui.activities.placementtest;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Line implements Serializable, Comparable<Line> {
    @SerializedName("left")
    private double left;

    @SerializedName("top")
    private double top;

    @SerializedName("text")
    private String text;

    @SerializedName("lineHeight")
    private double lineHeight;

    @SerializedName("font")
    private String font;

    @SerializedName("fontSize")
    private double fontSize;

    @SerializedName("color")
    private String color;

    public double getLeft() {
        return left;
    }

    public void setLeft(double left) {
        this.left = left;
    }

    public double getTop() {
        return top;
    }

    public void setTop(double top) {
        this.top = top;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(double lineHeight) {
        this.lineHeight = lineHeight;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    public double getFontSize() {
        return fontSize;
    }

    public void setFontSize(double fontSize) {
        this.fontSize = fontSize;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    // Implement the compareTo method to define the custom comparison logic
    @Override
    public int compareTo(Line other) {
        int topComparison = Double.compare(this.top, other.top);

        if (this.top - other.top <= this.getLineHeight()/2 && this.top - other.top > - this.getLineHeight()/2) {
            return Double.compare(this.left, other.left);
        }
        return topComparison;
    }
}
