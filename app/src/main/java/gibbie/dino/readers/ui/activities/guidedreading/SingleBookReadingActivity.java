package gibbie.dino.readers.ui.activities.guidedreading;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.AudioController;
import gibbie.dino.readers.commonclasses.OnSwipeTouchListener;
import gibbie.dino.readers.customlayout.OutlineTextView;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.interfaces.AudioControllerCallback;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;
import gibbie.dino.readers.ui.activities.placementtest.Line;
import gibbie.dino.readers.ui.activities.placementtest.Page;
import gibbie.dino.readers.ui.activities.quiz.Quiz;
import gibbie.dino.readers.ui.activities.quiz.QuizActivity;
import gibbie.dino.readers.ui.activities.readingbuddy.AvatarFrameModel;
import gibbie.dino.readers.ui.activities.readingbuddy.ReadingBuddyAvatarModel;
import gibbie.dino.readers.ui.activities.setting.ReadingTimeCounter;
import gibbie.dino.readers.ui.fragments.ownstory.SpellChecker;


public class SingleBookReadingActivity extends AppCompatActivity implements RecognitionListener {

    SessionManager sessionManager;
    SpellChecker spellChecker;
    // GAME SETTING :
    private final static int minimal_correct_word = 2;
    private final static int auto_correct_trial_count = 7;
    int current_trial_count = 0;
    // ADDITION FOR CHECK PERCENTAGE
    private int total_word_count = 0;
    private int total_right_word_count = 0;

    String book_id;
    String uid;
    String book_title;
    int total_page, total_story_page;
    int current_page = 1;
    Boolean isReadToMe;
    List<Page> pages;
    List<Quiz> quizList;
    Boolean[] isOpened;
    int current_sentence = 1;
    String[] sentences;

    ImageView pageImage;
    ImageView btn_record;
    ImageView iv_avatar, iv_frame;
    ImageView btn_back, btn_next, img_checked;
    ConstraintLayout root_view, top_nav, bottom_nav;
    TextView page_number;
    OutlineTextView page_text;
//    TextView page_text;

    TextToSpeech tts;
    String ttsLanguage = "en-UK";
    private boolean textToSpeechIsInitialized = false;
    long startSpeakDelay = 0;

    final int REQUEST_PERMISSION_CODE = 1000;
    boolean isRecordingSession = false;
    boolean isRecording = false;
    String partialSpeechResult = "";
    List<String> originalWord;
    List<String> originalWordToCheck;
    String[] highlightWord;
    boolean[] wordIsRight;
    String[] punctuation = new String[]{",", "'", "\""};

    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;

    private boolean textToSpeechIsActive = false;
    AudioController audioController = new AudioController();

    private int delayBeforeStop = 4000; // startRecording stop if 4 second no voice input
    Handler handler = new Handler(Looper.getMainLooper());

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            stopRecording();
            if(!isFinishTheSentence && !onSingleWordSession){
                retrySentence();
            }
        }
    };

    private int reading_level;
    private long startTime;
    private long totalTime;

    TextView tv_reading_buddy_point;

    ProgressDialog waitingDialogue;
    //List<TextView> activeTextViews;

    List<String> wrongWords;
    private static final String BUDDY_MESSAGE_RETRY_SENTENCE = "Repeat the sentence!";
    private static final String BUDDY_MESSAGE_RETRY_WORD = "Let's try again. Say ";
    boolean isFinishTheSentence = false;
    boolean onWrongSession = false;
    boolean onSingleWordSession = false;
    String targetWord = "";

    Boolean isSetBgColor = false;
    String defaultMenu = "Home";

    CardView cv_point;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_book_reading);
        init();
        loadPageData();
        if(isReadToMe){
            initTextToSpeech();
        }
        initSpeechToText();
        setButtonListener();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void init(){
        // Set fullscreen and hide status bar and navigation phone UI
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
        );

        sessionManager = new SessionManager(this);

        pageImage = findViewById(R.id.img_view);
        iv_avatar = findViewById(R.id.iv_avatar);
        iv_frame = findViewById(R.id.iv_frame);
        btn_record = findViewById(R.id.btn_record);
        btn_back = findViewById(R.id.btn_back);

        btn_next = findViewById(R.id.btn_next);
        img_checked = findViewById(R.id.img_checked);
        top_nav = findViewById(R.id.top_navigation);
        bottom_nav = findViewById(R.id.bottom_navigation);
        page_number = findViewById(R.id.page_number);
        page_text = findViewById(R.id.page_text);
        tv_reading_buddy_point = findViewById(R.id.tv_reading_buddy_point);
        tv_reading_buddy_point.setText(String.valueOf(sessionManager.getReadingPoints()));
        cv_point = findViewById(R.id.cv_point);

        spellChecker = SpellChecker.getInstance();
        showReadingBuddy();

        root_view = findViewById(R.id.root_view);
        root_view.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeRight() { goToPreviousPage(); }
            @Override
            public void onSwipeLeft() { goToNextPage(); }
        });

        page_text.setText("");
        showBottomPanelComponent(false);
    }

    private void showReadingBuddy(){
        setDefaultReadingBuddy();
        fillImageByAsetName(iv_avatar, sessionManager.getReadingBuddy().getAssetName());
        fillImageByAsetName(iv_frame, sessionManager.getReadingBuddyFrame().getAssetName());
    }

    private void fillImageByAsetName(ImageView imageView, String name){
        String uri = "@drawable/" + name;  // where myresource (without the extension) is the file
        int imageResource = getResources().getIdentifier(uri, null, getPackageName());
        Drawable res = getResources().getDrawable(imageResource);
        imageView.setImageDrawable(res);
    }

    private void setDefaultReadingBuddy(){
        if(sessionManager.getReadingBuddy() == null){
            sessionManager.setReadingBuddy(
                    new ReadingBuddyAvatarModel(1, "Cherie", "", "reader_buddy_1", "")
            );
        }
        if(sessionManager.getReadingBuddyFrame() == null){
            sessionManager.setReadingBuddyFrame(
                    new AvatarFrameModel(1, "", "avatar_frame_1", "")
            );
        }
    }

    private void loadPageData() {
        Intent intent = getIntent();
        book_id = intent.getExtras().getString("id");
        uid = intent.getExtras().getString("uid");
        reading_level = intent.getExtras().getInt("readingLevel");
        isReadToMe = intent.getExtras().getBoolean("isReadToMe");
        pages = (List<Page>) getIntent().getSerializableExtra("pagesList");
        defaultMenu = intent.getStringExtra("defaultMenu");

        for (int i = 0; i < pages.size(); i++) {
            List<Line> originalLines = new ArrayList<>();
            for (int j = 0; j < pages.get(i).getLines().size(); j++) {
                originalLines.add(pages.get(i).getLines().get(j));
            }

            if (originalLines.size() > 0)
            {
                // order top to bottom
                Collections.sort(pages.get(i).getLines());
                // get the first order, from that we ordering base original order
                int firstOrder = 0;
                for (int j = 0; j < originalLines.size(); j++) {
                    if(originalLines.get(j).getText().equals(pages.get(i).getLines().get(0).getText()))
                    {
                        firstOrder = j;
                        break;
                    }
                }

                if(firstOrder != 0){
                    List<Line> newOrderLine = new ArrayList<>();
                    for (int j = firstOrder; j < originalLines.size(); j++) {
                        newOrderLine.add(originalLines.get(j));
                    }
                    for (int j = 0; j < firstOrder; j++) {
                        newOrderLine.add(originalLines.get(j));
                    }
                    pages.get(i).setLines(newOrderLine);
                }
                else {
                    if(!originalLines.equals(pages.get(i).getLines())){
                        List<Line> newOrderLine = new ArrayList<>();
                        for (int j = firstOrder; j < originalLines.size(); j++) {
                            newOrderLine.add(originalLines.get(j));
                        }
                        pages.get(i).setLines(newOrderLine);
                    }
                }

                if(pages.get(i).getLines().size() > 0){
                    String fullText = "";
                    for (int j = 0; j < pages.get(i).getLines().size(); j++) {
                        Line line = pages.get(i).getLines().get(j);
                        fullText += line.getText();
                        if (j < pages.get(i).getLines().size() - 1) {
                            fullText += " ";
                        }
                    }
                    pages.get(i).setFullText(fullText.trim().replaceAll("\\s+", " "));
                }
            }
        }



        quizList = (List<Quiz>) getIntent().getSerializableExtra("quizList");

        isOpened = new Boolean[pages.size()];
        Arrays.fill(isOpened, false);
        total_page = pages.size();
        book_title = intent.getExtras().getString("title");
        setPageData();
        checkBookData();
    }

    private void checkBookData(){
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private  void setPageData(){
        showBottomPanelComponent(false);
        handler.removeCallbacks(runnable);
        onWrongSession = false;
        onSingleWordSession = false;
        isFinishTheSentence = true;
        current_trial_count = 0;
        String pageNumberText = current_page + "/" + total_page;
        page_number.setText(pageNumberText);

        Page page = pages.get(current_page - 1);
        String imageUrl = WebUrl.AWSBOOKPATH + uid + "/" + page.getImgOriUrl();
        Log.d("TAG", "setPageData: " + imageUrl);

        Picasso.get().load(imageUrl).into(pageImage);
        if(current_page <= 1)
        {
            Picasso.get().load(imageUrl).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    pageImage.setImageBitmap(bitmap);
                    Palette.from(bitmap).generate(palette -> {
                        // Get the main color from the palette
                        int mainColor = palette.getDominantColor( Color.parseColor("#FFFFFF"));
                        pageImage.setBackgroundColor(mainColor);
                    });
                }
                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {}
                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {}
            });
        }
        else
        {
            pageImage.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }


        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        if(page.getHeight() > page.getWidth()){
            pageImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }
        else
        {
            if(page.getWidth()/page.getHeight() >= width/height)
                pageImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            else
                pageImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }

        isRecordingSession = false;
        if(page.isPlayAudio() && isReadToMe){
            int wordCount = page.getFullText().split(" ").length;

            if(!isOpened[current_page - 1])
            {
                total_word_count += wordCount;
                isOpened[current_page - 1] = true;
            }

            if(page.getAudioUrl().equals("")) {
                new android.os.Handler(Looper.getMainLooper()).postDelayed(
                        new Runnable() {
                            public void run() {
                                autoSpeak(page);
                            }
                        }, startSpeakDelay
                );
            }
            else {
                audioController.playAudioFromURL(page.getAudioUrl(), new AudioControllerCallback() {
                    @Override
                    public void OnAudioEnd() {
                        startRecordingSession();
                        showBottomPanel(true);
                    }
                });
            }
        }
    }

    private void showBottomPanel(boolean isShow){
        Page page = pages.get(current_page -1);
//        sentences = page.getFullText().replace("\"","").replaceAll("[“”]", "").replaceAll("[?!]", ".").trim().split("[.]");
        String paragraph = page.getFullText().replace("\"","").replaceAll("[“”]", "").replaceAll("[?!]", ".").trim();
        sentences = getSentencesWithoutInvalidWord(paragraph);
        if((current_page == 1) || !page.isPlayAudio()){
            isShow = false;
        };
        showBottomPanelComponent(isShow);
        setPageText();
    }

    private void showBottomPanelComponent(boolean isShow){
        btn_record.setVisibility(!isShow ? View.GONE : View.VISIBLE);
        iv_frame.setVisibility(!isShow ? View.GONE : View.VISIBLE);
        iv_avatar.setVisibility(!isShow ? View.GONE : View.VISIBLE);
        cv_point.setVisibility(!isShow ? View.GONE : View.VISIBLE);
    }

    private void setPageText(){
        current_trial_count = 0;
        String textSentence = sentences[current_sentence - 1].trim();
        page_text.setText(textSentence + ".");
        page_text.post(page_text::checkAndHideOverlapShadow);

        originalWord = new ArrayList<>();
        originalWordToCheck = new ArrayList<>();
        String[] word = textSentence.split(" ");
        for (int i = 0; i < word.length; i++){

            if(word[i].trim().length() <= 0)
                continue;

            originalWord.add(word[i]);
            String wordToCheck = word[i].toLowerCase().trim();
            for(int j = 0; j < punctuation.length; j++)
                wordToCheck = wordToCheck.replaceAll(punctuation[j], "");
            originalWordToCheck.add(wordToCheck);
        }

        highlightWord = new String[originalWord.size()];
        wordIsRight = new boolean[originalWord.size()];
    }

    private void setButtonListener(){
        btn_back.setOnClickListener(v -> goToPreviousPage());
        btn_next.setOnClickListener(v -> goToNextPage());
        iv_avatar.setOnClickListener(v -> speakQuestion());
        btn_record.setOnClickListener(v -> {
            if(!isRecording)
                startRecording();
            else
                stopRecording();
        });
    }

    private void goToPreviousPage(){
        stopTextToSpeech();
        page_text.setText("");
        showBottomPanelComponent(false);
        current_page--;
        if(current_page > 0)
            changePage();
        else
            finish();
    }

    private void goToNextPage(){
        stopTextToSpeech();
        page_text.setText("");
        showBottomPanelComponent(false);
        current_page++;
        if(current_page <= pages.size())
            changePage();
        else
            goToResultScreen();
    }

    private void changePage(){
        audioController.pauseAudio();
        startSpeakDelay = 500;
        playPageFlippingSFX();
        setPageData();
    }

    private void playPageFlippingSFX(){
        int soundIndex = new Random().nextInt(6 - 1) + 1;;
        audioController.playSFX(this, "flip_page_" + soundIndex);
    }

    private void initTextToSpeech(){
        final ProgressDialog loading;
        loading = ProgressDialog.show(this, "Initialize Text to Speech", "Please wait...",true,false);
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    loading.dismiss();
                    textToSpeechIsInitialized = true;
                    setTextToSpeechLanguage();

                    // CHECK IF VOICE OVER AUDIO for Book is exist,
                    boolean haveAudio = false;
                    if(pages.size() > 0){
                        haveAudio = pages.get(current_page - 1).isPlayAudio()
                                && !pages.get(current_page - 1).getAudioUrl().equals("");
                    }

                    if(haveAudio){
                        waitingDialogue.dismiss();
                    }

                    // we run from again after init, if the text to speech not run because it haven't initialized
                    if(!textToSpeechIsActive && !haveAudio)
                        tts.speak(book_title, TextToSpeech.QUEUE_FLUSH, null, null);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Text to speech error", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private void setTextToSpeechLanguage(){
        switch(ttsLanguage) {
            case "en-UK":
                // english UK setting
                tts.setLanguage(Locale.UK);
                Voice voice_uk = new Voice("en-gb-x-gba-local", new Locale("en","GB"),
                        400,200,false, null);
                SetVoice(voice_uk, 1f, 1f);
                break;
            case "en-US":
                // english US setting
                Voice voice_us = new Voice("en-us-x-gba-local", new Locale("en","US"),
                        400,200,false, null);
                SetVoice(voice_us, 1.3f, 0.65f);
                break;
            default:
                Log.d("VOICE_TTS", "Default Voice");
                break;
        }
    }

    private  void SetVoice(Voice voice, float pitch, float speechRate){
        tts.setVoice(voice);
        tts.setPitch(pitch);
        tts.setSpeechRate(speechRate);
    }

    private void startRecordingSession() {
        current_sentence = 1;
        isRecordingSession = true;
    }

    private void autoSpeak(Page page){
        sentences =  getSentencesWithoutInvalidWord(page.getFullText());
        current_sentence = 1;
        startTextToSpeech();
    }

    private String[] getSentencesWithoutInvalidWord(String paragraph){
        String[] tempSentence = paragraph.split("[.!?]");
        List<String> tempString = new ArrayList<>();
        for (int i = 0; i < tempSentence.length; i++) {
            Boolean isValid = true;
            String[] text = tempSentence[i].trim().split(" ");
            if(text.length == 1){
                if(!spellChecker.wordIsExist(text[0]))
                    isValid = false;
            }
            if(isValid)
                tempString.add(tempSentence[i]);
        }
        return tempString.toArray(new String[0]);
    }

    private void startTextToSpeech(){
        if(pages.size() <= 0)
            return;

//        String textSentence = pages.get(current_page - 1).getFullText();
        String textSentence = sentences[current_sentence - 1] + ".";
        page_text.setText(textSentence);
        page_text.post(page_text::checkAndHideOverlapShadow);

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {

            }

            @Override
            public void onDone(String utteranceId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(isRecordingSession)
                            return;

                        if(current_sentence < sentences.length){
                            current_sentence++;
                            startTextToSpeech();
                        }
                        else {
                            startRecordingSession();
                            showBottomPanel(true);
                        }
                    }
                });
            }

            @Override
            public void onError(String s) {

            }

            @Override
            public void onRangeStart(String utteranceId, final int start, final int end, int frame) {

                if(onWrongSession || onSingleWordSession)
                    return;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Spannable textWithHighlights = new SpannableString(textSentence);
                        textWithHighlights.setSpan(new ForegroundColorSpan(Color.RED), 0, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        page_text.setHighlightText(textWithHighlights);
                    }
                });
            }
        });

        if(textToSpeechIsInitialized) {
            tts.speak(textSentence, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
        }
    }

    private void speakQuestion(){
        String text = "";
        for(int i = 0; i < punctuation.length; i++){
            text = sentences[current_sentence - 1].toLowerCase().replaceAll(punctuation[i],"");
        }
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void stopTextToSpeech(){
        if(tts !=null){
            tts.stop();
        }
    }


    private void initSpeechToText(){
        if(!checkPermissionFromDevice())
            requestPermission();

        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"en-GB");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, delayBeforeStop);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, delayBeforeStop);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        }, REQUEST_PERMISSION_CODE);
    }

    private boolean checkPermissionFromDevice() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return  write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                record_audio_result == PackageManager.PERMISSION_GRANTED;
    }

    private void stopRecording() {
        handler.removeCallbacks(runnable);
        setTotalTime();
        speech.stopListening();
        isRecording = false;
        btn_record.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.record));
        showControlButtons(true);
    }

    private void startRecording() {
        isFinishTheSentence = false;
        setStartTime();
        current_trial_count++;
        partialSpeechResult = "";
        speech.startListening(recognizerIntent);
        isRecording = true;
        btn_record.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.record_active));
        showControlButtons(false);
    }

    private void showControlButtons(boolean isEnable){
        top_nav.setVisibility(isEnable? View.VISIBLE : View.GONE);
//        btn_speaker.setVisibility(isEnable? View.VISIBLE : View.GONE);
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {}
    @Override
    public void onBeginningOfSpeech() {
        handler.removeCallbacks(runnable);
    }
    @Override
    public void onRmsChanged(float v) {}
    @Override
    public void onBufferReceived(byte[] bytes) {}
    @Override
    public void onEndOfSpeech() {
        handler.postDelayed(runnable, delayBeforeStop);
    }
    @Override
    public void onError(int i) {
        Log.e("ERROR SPEECH", "onError: " + getErrorText(i));
        btn_record.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.record));
        isRecording = false;
    }
    @Override
    public void onResults(Bundle bundle) {}
    @Override
    public void onPartialResults(Bundle results) {
        ArrayList<String> data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if(data.size() <= 0 || !isRecording)
            return;
        partialSpeechResult = (String) data.get(data.size() - 1).replace("'","");
        pronunciationCheck();
    }
    @Override
    public void onEvent(int i, Bundle bundle) {}

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No voice recognized";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    private void pronunciationCheck(){
        String normalColor = "#000000";
        String matchColor = "#8eb4fa";
        String unmatchedColor = "#f06832";

        if(partialSpeechResult.trim().equals(""))
            return;

        String[] answerWord = partialSpeechResult.toLowerCase().trim().split(" ");

        if(!onSingleWordSession){
            wrongWords = new ArrayList<>();
            for(int i = 0; i < originalWord.size(); i++){
                if(wordIsRight[i])
                    continue;

                if(answerWord.length > i){
                    String wordToCheck = originalWordToCheck.get(i);
                    boolean isRight = false;

                    for(int j = 0; j < answerWord.length; j++){
                        isRight = answerWord[j].equals(wordToCheck);
                        if(isRight)
                            break;
                    }
                    if(!isRight){
                        if(!spellChecker.wordIsExist(wordToCheck.toUpperCase()))
                            isRight = true;
                    }

                    if(current_trial_count >= auto_correct_trial_count){
                        isRight = true;
                    }
                    String col = isRight ? matchColor : unmatchedColor;
                    highlightWord[i] = "<font color=" + col + ">" + originalWord.get(i) + "</font>";
                    wordIsRight[i] = isRight;
                    if(!isRight && !wrongWords.contains(wordToCheck)){
                        wrongWords.add(wordToCheck);
                    }
                }
                else{
                    highlightWord[i] = "<font color=" + normalColor + ">" + originalWord.get(i) + "</font>";
                }
            }
            String textSentence = TextUtils.join(" ", highlightWord) + ".";
            page_text.setHighlightText(Html.fromHtml(textSentence));
            Log.e("TAG", "pronunciationCheck: " + partialSpeechResult.toLowerCase().trim());
            Log.e("TAG", "target: " + originalWordToCheck);

            int rightWord = 0;
            for (int i = 0; i < wordIsRight.length; i ++){
                if(wordIsRight[i])
                    rightWord++;
            }

            if(answerWord.length >= originalWord.size()){
                if(rightWord >= originalWord.size()){
                    savedAnswer(rightWord);
                }
                else{
                    onWrongAnswer(wrongWords);
                }
            }
        }
        else{
            boolean isRight = false;
            for(int j = 0; j < answerWord.length; j++){
                if(answerWord[j].equals(targetWord)){
                    isRight = true;
                    break;
                }
            }
            if(isRight){
                page_text.setHighlightText(Html.fromHtml("<font color=" + matchColor + ">" + sentences[current_sentence - 1].trim() + "</font>"));
                savedAnswer(originalWord.size());
            }
        }
    }

    private void onWrongAnswer(List<String> incorrectWords){
        handler.removeCallbacks(runnable);
        onWrongSession = true;
        isFinishTheSentence = false;
        stopRecording();
        Handler handler = new Handler();
        int delayInMillis = 500;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() { setBuddyMessage(incorrectWords); }
        }, delayInMillis);
    }

    private void setBuddyMessage(List<String> incorrectWords){
        if(incorrectWords.size() == 1)
            retrySingleWords(incorrectWords.get(0));
        else
            retrySentence();
    }

    private void retrySingleWords(String word){
        onSingleWordSession = true;
        targetWord = word;
        tts.speak(BUDDY_MESSAGE_RETRY_WORD + word, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
    }

    private void retrySentence(){
        onWrongSession = true;
        tts.speak(BUDDY_MESSAGE_RETRY_SENTENCE, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
    }

    private void savedAnswer(int rightWordCount){
        handler.removeCallbacks(runnable);
        onWrongSession = false;
        onSingleWordSession = false;
        isFinishTheSentence = true;
        stopRecording();
        total_right_word_count += rightWordCount;
        tv_reading_buddy_point.setText(String.valueOf(sessionManager.getReadingPoints() + total_right_word_count));
        Handler handler = new Handler();
        int delayInMillis = 750;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                saveAndGoNext();
            }
        }, delayInMillis);
    }

    private void saveAndGoNext(){
        page_text.setText("");
        audioController.playSFX(this, "success");
        if(current_sentence < sentences.length){
            current_sentence++;
            setPageText();
        }
        else{
            setCompleteRecordedPage();
            playPageFlippingSFX();
            setPageData();
        }
    }

    private void goToResultScreen(){
        setTotalTime();
        ReadingTimeCounter.stopTimer();
//        int point = Math.round(((float) total_right_word_count / total_word_count) * 10);
        int point = total_right_word_count;

        // Sending the reading progress here :
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("user_id", sessionManager.getUserId());
            requestBody.put("profile_id", sessionManager.getProfileId());
            requestBody.put("book_id", book_id);
            requestBody.put("reading_count", 1);
            requestBody.put("reading_time", sessionManager.getTempReadingTime());
            requestBody.put("reading_score", point);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsArrayRequest = new JsonObjectRequest(
                Request.Method.POST,
                WebUrl.SAVEREADINGHISTORYURL,
                requestBody,
                response -> Log.d("TAG", "reading history saved successfully"),
                error -> Log.e("TAG", "Failed to save reading history: " + error.getMessage())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", sessionManager.getAccesstoken());
                return headers;
            }
        };

        RequestQueue mRequestQueue = Volley.newRequestQueue(this);
        String TAG_JSON = "json_obj_req";
        jsArrayRequest.setTag(TAG_JSON);
        jsArrayRequest.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        mRequestQueue.add(jsArrayRequest);
        goToQuiz();
        this.finish();
    }

    private void goToQuiz(){
        if(quizList.size() > 0) {
            Intent intent = new Intent(SingleBookReadingActivity.this, QuizActivity.class);
            intent.putExtra("quizList", (Serializable) quizList);
            intent.putExtra("defaultMenu", defaultMenu);
            startActivity(intent);
        }
        else{

            Intent intents = new Intent(this, BottomNavigation.class);
            intents.putExtra("defaultMenu", defaultMenu);
            intents.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intents);
            finish();
        }
    }

    private void setCompleteRecordedPage(){
        current_sentence = 1;
        current_page++;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        stopAudio();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ReadingTimeCounter.startTimer(this);
        setStartTime();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ReadingTimeCounter.stopTimer();
        audioController.pauseAudio();
        stopTextToSpeech();
        setTotalTime();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAudio();
    }

    private void stopAudio(){
        audioController.pauseAudio();
        stopTextToSpeech();
        if(tts != null)
            tts.shutdown();
    }

    private void setStartTime(){
        startTime = SystemClock.elapsedRealtime();
    }

    private void setTotalTime(){
        long endTime = SystemClock.elapsedRealtime();
        totalTime += endTime - startTime;
        Log.d("TAG", String.valueOf(totalTime));
    }
}