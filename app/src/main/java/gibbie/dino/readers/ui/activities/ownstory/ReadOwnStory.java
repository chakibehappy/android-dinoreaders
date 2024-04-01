package gibbie.dino.readers.ui.activities.ownstory;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import gibbie.dino.readers.R;
import gibbie.dino.readers.customlayout.OutlineTextView;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.ui.activities.setting.ReadingTimeCounter;
import gibbie.dino.readers.ui.fragments.ownstory.BookTemplateModel;
import gibbie.dino.readers.ui.fragments.ownstory.OwnStoryBookModel;

public class ReadOwnStory extends AppCompatActivity {

    List<String> page_text;
    List<String> page_audio;
    SharedPreferences sp;
    ImageView iv_background, iv_cover;
    OutlineTextView tv_story;
    ImageView iv_btn_listen, iv_btn_next, iv_btn_prev;
    String cover_img;
    int template_id = 1;
    int currentPage = 0;
    boolean isPlayingFile = false;
    MediaPlayer mediaPlayer;

    List<CanvasData> canvasDataList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_own_story);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
        );

        loadCanvasData();
        init();
        loadStory();
    }

    private void init(){
        iv_background = findViewById(R.id.iv_background);
        iv_cover = findViewById(R.id.iv_cover);
        tv_story = findViewById(R.id.tv_story);
        iv_btn_listen = findViewById(R.id.iv_btn_listen);
        iv_btn_next = findViewById(R.id.iv_btn_next);
        iv_btn_prev = findViewById(R.id.iv_btn_prev);

        iv_btn_next.setOnClickListener(v -> goToNextPage());
        iv_btn_prev.setOnClickListener(v -> goToPrevPage());
        iv_btn_listen.setOnClickListener(v -> playPageAudio());

    }

    private void loadCanvasData() {
        canvasDataList = new ArrayList<>();
        canvasDataList.add(new CanvasData(1, "Design 1", "canvas_1", "full_canvas_1"));
        canvasDataList.add(new CanvasData(2, "Design 2", "canvas_2", "full_canvas_2"));
        canvasDataList.add(new CanvasData(3, "Design 3", "canvas_3", "full_canvas_3"));
    }

    private void goToNextPage(){
        currentPage++;
        setPage();
    }

    private  void goToPrevPage(){
        currentPage--;
        setPage();
    }

    private void playPageAudio(){
        if(!isPlayingFile)
            playMediaPlayer();
        else
            stopMediaPlayer();
    }

    private void playMediaPlayer(){
        if(page_audio.get(currentPage).length() == 0){
            return;
        }
        isPlayingFile = true;
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(page_audio.get(currentPage));
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopMediaPlayer();
            }
        });
        mediaPlayer.start();
    }

    private void stopMediaPlayer(){
        isPlayingFile = false;
        if(mediaPlayer != null){
            mediaPlayer.pause();
        }
    }

    private void loadStory(){
        page_text = new ArrayList<>();
        page_audio = new ArrayList<>();
        Intent intent = getIntent();
        int book_id = intent.getIntExtra("book_id", 0);
        if(book_id > 0){
            sp = getSharedPreferences("DinoReader", MODE_PRIVATE);
            String bookListJson = sp.getString("myOwnStoryBook", null) ;
            if(bookListJson != null){
                Gson gson = new Gson();
                Type listType = new TypeToken<List<OwnStoryBookModel>>() {}.getType();
                List<OwnStoryBookModel> bookList =  gson.fromJson(bookListJson, listType);

                OwnStoryBookModel book = getBookById(bookList, book_id);
                if(book != null){
                    page_text.add(book.getTitle());
                    page_audio.add(book.getLocalAudioUrl());
                    cover_img = book.getCover();
                    template_id = book.getTemplateId();
                    for (int i = 0; i < book.getPages().size(); i++) {
                        page_text.add(book.getPage(i).getStory());
                        page_audio.add(book.getPage(i).getLocalAudioUrl());
                    }
                }
            }
        }
        loadBook();
    }

    private void loadBook(){
        CanvasData canvas = getCanvasById(template_id);
        Picasso.get().load(cover_img).into(iv_cover);
        String bg_image = canvas.getThumbnail();
        int resourceImage = getResources().getIdentifier(bg_image, "drawable", getPackageName());
        iv_background.setImageResource(resourceImage);
        setPage();
    }

    private void setPage(){
        boolean isFirstPage = currentPage == 0;
        boolean isLastPage = currentPage == page_text.size() - 1;
        iv_btn_prev.setVisibility(isFirstPage ? View.GONE : View.VISIBLE);
        iv_btn_next.setVisibility(isLastPage ? View.GONE : View.VISIBLE);
        iv_cover.setVisibility(isFirstPage ? View.VISIBLE : View.GONE);
        tv_story.setText(page_text.get(currentPage));
    }

    private OwnStoryBookModel getBookById(List<OwnStoryBookModel> list, int targetId) {
        for (int i = 0; i < list.size(); i++) {
            OwnStoryBookModel obj = list.get(i);
            if (obj.getId() == targetId) {
                return obj;
            }
        }
        return null;
    }

    private CanvasData getCanvasById(int templateId) {
        for (int i = 0; i < canvasDataList.size(); i++) {
            CanvasData obj = canvasDataList.get(i);
            if (obj.getId() == templateId) {
                return obj;
            }
        }
        return canvasDataList.get(0);
    }


    @Override
    protected void onPause() {
        super.onPause();
        ReadingTimeCounter.stopTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ReadingTimeCounter.startTimer(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ReadingTimeCounter.stopTimer();
    }
}