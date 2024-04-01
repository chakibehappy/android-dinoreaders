package gibbie.dino.readers.ui.fragments.ownstory;

import android.content.Context;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import gibbie.dino.readers.commonclasses.FileDownloader;
import gibbie.dino.readers.database.SessionManager;

public class OwnStoryBookModel {

    private int id;
    private int template_id;
    private String title;
    private String cover;
    private String cover_path;
    private List<OwnStoryPageModel> pages;
    private String audio_url;
    private String local_audio_url;

    public OwnStoryBookModel(int id, int template_id, String title, String cover,  List<OwnStoryPageModel> pages, String audio_url) {
        this.id = id;
        this.template_id = template_id;
        this.title = title;
        this.cover = cover;
        this.cover_path = null;
        this.pages = pages;
        this.audio_url = audio_url;
        this.local_audio_url = null;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTemplateId() {
        return template_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getCover() {
        return cover;
    }

    public void setCoverPath(String cover_path) {
        this.cover_path = cover_path;
    }

    public void setCoverFromImageString(String imageString){
        this.cover_path = saveImageStringToFile(imageString);
    }

    public String getCoverLocalPath() {
        return cover_path;
    }

    public OwnStoryPageModel getPage(int page){
        return pages.get(page);
    }

    public void setPages(List<OwnStoryPageModel> pages) {
        this.pages = pages;
    }

    public List<OwnStoryPageModel> getPages() {
        return pages;
    }

    public String getAudioUrl() {
        return audio_url;
    }

    public void setAudioUrl(String audio_url) {
        this.audio_url = audio_url;
    }

    public String getLocalAudioUrl() {
        return local_audio_url;
    }

    public void setLocalAudioUrl(String local_audio_url) {
        this.local_audio_url = local_audio_url;
    }

    public String saveImageStringToFile(String imageString) {
        try {
            byte[] imageByteArray = Base64.decode(imageString, Base64.DEFAULT);
            String uid = UUID.randomUUID().toString();

            String folderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            String appName = "DinoReader";
            folderPath = folderPath + File.separator + appName;

            File folder = new File(folderPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String filePath = folderPath + File.separator + uid + ".png";

            FileOutputStream fileOutputStream = new FileOutputStream(filePath);
            fileOutputStream.write(imageByteArray);
            fileOutputStream.close();
            return filePath;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
