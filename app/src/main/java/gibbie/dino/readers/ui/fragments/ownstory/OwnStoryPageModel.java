package gibbie.dino.readers.ui.fragments.ownstory;

public class OwnStoryPageModel {
    private int id;
    private int page_number;
    private String page_story;
    private String page_audio_url;
    private String page_local_audio_url;

    public OwnStoryPageModel(int id, int page_number, String page_story, String page_audio_url) {
        this.id = id;
        this.page_number = page_number;
        this.page_story = page_story;
        this.page_audio_url = page_audio_url;
    }

    public int getId() {
        return id;
    }

    public String getStory() {
        return page_story;
    }

    public void setStory(String page_story) {
        this.page_story = page_story;
    }

    public void setPageLocalAudioUrl(String page_local_audio_url) {
        this.page_local_audio_url = page_local_audio_url;
    }

    public String getLocalAudioUrl() {
        return page_local_audio_url;
    }

    public void setAudioUrl(String page_audio_url) {
        this.page_audio_url = page_audio_url;
    }

    public String getAudioUrl() {
        return page_audio_url;
    }
}
