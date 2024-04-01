package gibbie.dino.readers.ui.activities.readingbuddy;

public class ReadingBuddyAvatarModel {
    private int id;
    private String name;
    private String localPath;
    private String assetName;
    private String imageUrl;

    public ReadingBuddyAvatarModel (int id, String name, String localPath, String assetName, String imageUrl){
        this.id = id;
        this.name = name;
        this.localPath = localPath;
        this.assetName = assetName;
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocalPath() {
        return localPath;
    }

    public String getAssetName() {
        return assetName;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
