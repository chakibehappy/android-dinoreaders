package gibbie.dino.readers.ui.activities.ownstory;

import java.io.Serializable;

public class CanvasData implements Serializable {
    private int id;
    private String name;
    private String thumbnail;
    private String cover;

    public CanvasData(int id, String name, String thumbnail, String cover) {
        this.id = id;
        this.name = name;
        this.thumbnail = thumbnail;
        this.cover = cover;
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

    public String getCover() {
        return cover;
    }
}

