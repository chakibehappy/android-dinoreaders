package gibbie.dino.readers.ui.activities.readingbuddy;

public class AvatarFrameModel {
    private int id;
    private String localPath;
    private String assetName;
    private String imageUrl;

    public AvatarFrameModel (int id, String localPath, String assetName, String imageUrl){
        this.id = id;
        this.localPath = localPath;
        this.assetName = assetName;
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
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
