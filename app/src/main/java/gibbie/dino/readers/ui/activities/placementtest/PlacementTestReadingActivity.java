package gibbie.dino.readers.ui.activities.placementtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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
import android.text.TextUtils;
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

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.AudioController;
import gibbie.dino.readers.commonclasses.OnSwipeTouchListener;
import gibbie.dino.readers.interfaces.AudioControllerCallback;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.fragments.ownstory.SpellChecker;

public class PlacementTestReadingActivity extends AppCompatActivity implements RecognitionListener {

    SpellChecker spellChecker;
    // GAME SETTING :
    private final static int minimal_correct_word = 2;
    private final static int auto_correct_trial_count = 3;
    int current_trial_count = 0;
    // ADDITION FOR CHECK PERCENTAGE
    private int total_word_count = 0;
    private int total_right_word_count = 0;

    String book_id;
    String uid;
    String book_title;
    int total_page, total_story_page;
    int current_page = 1;
    List<Page> pages;
    int current_sentence = 1;
    String[] sentences;

    ImageView pageImage;
    ImageButton btn_speaker, btn_record;
    ImageView btn_back, btn_next, img_checked;
    ConstraintLayout root_view, top_nav, bottom_nav;
    TextView page_number, page_text;

    TextToSpeech tts;
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
        }
    };

    private int reading_level;
    private long startTime;
    private long totalTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placement_test_reading);
        init();
        loadPageData();
        initTextToSpeech();
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

        pageImage = findViewById(R.id.img_view);
        btn_speaker = findViewById(R.id.btn_speaker);
        btn_record = findViewById(R.id.btn_record);
        btn_back = findViewById(R.id.btn_back);
        btn_back.setVisibility(View.GONE);

        btn_next = findViewById(R.id.btn_next);
        img_checked = findViewById(R.id.img_checked);
        top_nav = findViewById(R.id.top_navigation);
        bottom_nav = findViewById(R.id.bottom_navigation);
        page_number = findViewById(R.id.page_number);
        page_text = findViewById(R.id.page_text);

        spellChecker = SpellChecker.getInstance();

        root_view = findViewById(R.id.root_view);
        root_view.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeRight() { goToPreviousPage(); }
            @Override
            public void onSwipeLeft() { goToNextPage(); }
        });
    }

    private void loadPageData() {
        Intent intent = getIntent();
        book_id = intent.getExtras().getString("id");
        uid = intent.getExtras().getString("uid");
        reading_level = intent.getExtras().getInt("readingLevel");
        pages = (List<Page>) getIntent().getSerializableExtra("pagesList");

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

        total_page = pages.size();
        book_title = intent.getExtras().getString("title");
        setPageData();
        checkBookData();
        bottom_nav.setVisibility(View.GONE);
    }

    private void checkBookData(){
//        if(pages.get(0).getHeight() > pages.get(0).getWidth())
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        else
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private  void setPageData(){
        current_trial_count = 0;
        String pageNumberText = current_page + "/" + total_page;
        page_number.setText(pageNumberText);

        Page page = pages.get(current_page - 1);
        String imageUrl = WebUrl.AWSBOOKPATH + uid + "/" + page.getImgUrl();
        Log.d("TAG", "setPageData: " + imageUrl);
        Picasso.get().load(imageUrl).into(pageImage);

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
        bottom_nav.setVisibility(View.GONE);

        // Dynamically creating text:
        FrameLayout text_container;
        text_container = findViewById(R.id.text_container);
        text_container.removeAllViews();
        List<Line> lines = page.getLines();

        float scale = (float) (page.getWidth()/width);
        int space = page.getWidth () == page.getHeight() ?
                (int) ((width -  page.getWidth()/width) / 4) : 32;

        for (Line line : lines) {
            TextView textView = new TextView(this);
            String text = line.getText().
                    replaceAll("\\s+", " ").
                    replaceAll("\\r?\\n", "");
            textView.setText(text);
            textView.setTextSize((float) line.getFontSize() * scale);
            textView.setTextColor(Color.parseColor(line.getColor()));

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            int magicNumber = (int) ((line.getFontSize()/2) + page.getHeight()/height);
            int left = space + (int) ((int) line.getLeft() * width/page.getWidth());
            int top = (int) ((int)line.getTop() * (height/page.getHeight())) - magicNumber;
            layoutParams.setMargins(left, top, 0, 0);
            Log.d("TAG", "setPageData: " + line.getTop());
            textView.setLayoutParams(layoutParams);

            text_container.addView(textView);

            ViewTreeObserver observer = textView.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Layout layout = textView.getLayout();
                    if (layout != null) {
                        int lineCount = layout.getLineCount();
                        boolean isOverflowing = lineCount > 1;

                        if (isOverflowing) {
                            int newLeftMargin = left - (2 * space);
                            FrameLayout.LayoutParams updatedLayoutParams = (FrameLayout.LayoutParams) textView.getLayoutParams();
                            updatedLayoutParams.setMargins(newLeftMargin, updatedLayoutParams.topMargin, 0, 0);
                            textView.setLayoutParams(updatedLayoutParams);
                        }
                    }
                    textView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }

        isRecordingSession = false;

        if(page.isPlayAudio()){
            int wordCount = page.getFullText().split(" ").length;
            total_word_count += wordCount;
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
                        showBottomPanel();
                    }
                });
            }
        }
    }

    private void showBottomPanel(){
        Page page = pages.get(current_page -1);
        sentences = page.getFullText().split("[.!]");
        boolean hideBottomPanel = (current_page == 1) || !page.isPlayAudio();
        bottom_nav.setVisibility(hideBottomPanel ? View.GONE : View.VISIBLE);
        setPageText();
    }

    private void setPageText(){
        current_trial_count = 0;
        String textSentence = sentences[current_sentence - 1].trim();
        page_text.setText(textSentence + ".");

        originalWord = new ArrayList<>();
        originalWordToCheck = new ArrayList<>();
        String[] word =textSentence.split(" ");
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
        btn_speaker.setOnClickListener(v -> speakQuestion());
        btn_record.setOnClickListener(v -> {
            if(!isRecording)
                startRecording();
            else
                stopRecording();
        });
    }

    private void goToPreviousPage(){
        stopTextToSpeech();
        current_page--;
        if(current_page > 0)
            changePage();
        else
            finish();
    }

    private void goToNextPage(){
        stopTextToSpeech();
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

                    final ProgressDialog waiting = ProgressDialog.show(PlacementTestReadingActivity.this,
                            "Text to Speech is Loading", "Please wait...\nYou can close this dialog if loading is too long.",true,true);

                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onDone(String utteranceId) {
                            if(current_sentence < sentences.length){
                                if(!isRecordingSession){
                                    current_sentence++;
                                    textToSpeech();
                                }
                            }
                            else{
                                startRecordingSession();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        showBottomPanel();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onError(String utteranceId) {
                        }

                        @Override
                        public void onStart(String utteranceId) {
                            textToSpeechIsActive = true;
                            waiting.dismiss();
                        }
                    });

                    tts.setLanguage(Locale.ENGLISH);

                    // english US setting :
                    /*
                    Voice va = new Voice("en-us-x-gba-local", new Locale("en","US"),
                            400,200,false, null);
                    tts.setVoice(va);
                    tts.setPitch(1.3f);
                    tts.setSpeechRate(0.65f);
                     */

                    // english UK setting
                    Voice va = new Voice("en-gb-x-gba-local", new Locale("en","GB"),
                            400,200,false, null);
                    tts.setVoice(va);
                    tts.setPitch(1f);
                    tts.setSpeechRate(1f);

                    // CHECK IF VOICE OVER AUDIO for Book is exist,
                    // if it exist we simply close the waiting dialog, by
                    boolean haveAudio = false;
                    if(pages.size() > 0){
                        haveAudio = pages.get(current_page - 1).isPlayAudio()
                                && !pages.get(current_page - 1).getAudioUrl().equals("");
                    }

                    if(haveAudio){
                        waiting.dismiss();
                    }

                    // we run from again after init, if the text to speech not run because it haven't initialized
                    if(!textToSpeechIsActive && !haveAudio)
                        textToSpeech();
                }
                else{
                    loading.dismiss();
                    Toast.makeText(getApplicationContext(), "Text to speech error", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void startRecordingSession() {
        current_sentence = 1;
        isRecordingSession = true;
    }

    private void autoSpeak(Page page){
        sentences = page.getFullText().split("[.!]");
        current_sentence = 1;
        textToSpeech();
    }

    private void textToSpeech(){
        String text = "";
        if(pages.size() <= 0)
            return;
        sentences = pages.get(current_page-1).getFullText().split("[.!]");
        for(int i = 0; i < punctuation.length; i++){
            text = sentences[current_sentence - 1].toLowerCase().replaceAll(punctuation[i],"");
        }
        if(textToSpeechIsInitialized) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
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
        //request runtime permission
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
        setTotalTime();
        speech.stopListening();
        isRecording = false;
        btn_record.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.record));
        showControlButtons(true);
    }

    private void startRecording() {
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
        btn_speaker.setAlpha(isEnable? 1f : 0f);
        btn_speaker.setEnabled(isEnable);
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
        String normalColor = "#6d6d6d";
        String matchColor = "#accb74";
        String unmatchedColor = "#f06832";

        if(partialSpeechResult.trim().equals(""))
            return;

        String[] answerWord = partialSpeechResult.toLowerCase().trim().split(" ");

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
            }
            else{
                highlightWord[i] = "<font color=" + normalColor + ">" + originalWord.get(i) + "</font>";
            }
        }

        String textSentence = TextUtils.join(" ", highlightWord) + ".";
        page_text.setText( Html.fromHtml(textSentence));
//        Log.e("TAG", "pronunciationCheck: " + partialSpeechResult.toLowerCase().trim());
//        Log.e("TAG", "target: " + originalWordToCheck);

        int rightWord = 0;
        for (int i = 0; i < wordIsRight.length; i ++){
            if(wordIsRight[i])
                rightWord++;
        }

        if(answerWord.length >= originalWord.size()){
            savedAnswer(rightWord);
        }
    }

    private void savedAnswer(int rightWordCount){
        stopRecording();
        total_right_word_count += rightWordCount;
        Handler handler = new Handler();
        int delayInMillis = 750;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() { saveAndGoNext(); }
        }, delayInMillis);
    }

    private void saveAndGoNext(){
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
        Intent i = new Intent(this, PlacementTestResultActivity.class);
        i.putExtra("id", book_id);
        String imageUrl = WebUrl.AWSBOOKPATH + uid + "/" + pages.get(0).getImgUrl();
        i.putExtra("cover", imageUrl);
        i.putExtra("title", book_title);;
        i.putExtra("total_word_count", total_word_count);
        i.putExtra("total_right_word_count", total_right_word_count);
        i.putExtra("total_time", totalTime);
        i.putExtra("reading_level", reading_level);

        this.startActivity(i);
        this.finish();
    }

    private void setCompleteRecordedPage(){
//        app.setRecordingStatusPage(book_id, current_page - 1, true);
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
        setStartTime();
    }

    @Override
    protected void onPause() {
        super.onPause();
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