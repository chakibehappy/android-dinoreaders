package gibbie.dino.readers.ui.activities.readingbuddy;

public class AvatarBackgroundModel {
    private int id;
    private String color;
    private String imageUrl;

    public AvatarBackgroundModel(int id, String color, String imageUrl){
        this.id = id;
        this.color = color;
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
    }

    public String getColor() {
        return color;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
