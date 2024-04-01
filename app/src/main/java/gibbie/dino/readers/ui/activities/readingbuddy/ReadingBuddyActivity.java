package gibbie.dino.readers.ui.activities.readingbuddy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.Voice;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import gibbie.dino.readers.R;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.ui.fragments.ownstory.SpellChecker;

public class ReadingBuddyActivity extends AppCompatActivity
        implements RecognitionListener, MediaPlayer.OnCompletionListener  {

    SessionManager sessionManager;
    SpellChecker spellChecker;

    ImageView iv_avatar, iv_page_image;
    LinearLayout ll_dialog_balon, ll_page_image;
    TextView tv_page_text, tv_point, tv_buddy_message;

    AppCompatButton btn_speak, btn_listen;

    List<String> pageText;
    List<String> pageImage;
    List<String> pageAudio;
    List<String> pageAudioTranscript;

    private int currentPage = 0;
    Boolean isPlayingAudio = false;

    // media player variable :
    private MediaPlayer mediaPlayer;
    private Handler handlerMedia;
    private Runnable runnableMedia;
    private List<List<Object>> transcript;
    private int currentWordIndex = 0;

    // text to speech variable :
    TextToSpeech textToSpeech;
    String ttsLanguage = "en-UK";
    private boolean textToSpeechIsInitialized = false;
    String tts_sentence;

    // speech to text variable :
    private SpeechRecognizer speechRecognizer = null;
    final int REQUEST_PERMISSION_CODE = 1000;
    boolean isRecording = false;
    String partialSpeechResult = "";

    // word checking variable :
    List<String> originalWord;
    List<String> originalWordToCheck;
    String[] highlightWord;
    boolean[] wordIsRight;
    String[] punctuation = new String[]{",", "'", "\""};

    private Intent recognizerIntent;

    // Point variable
    ReadingBuddyPointHelper pointHelper = new ReadingBuddyPointHelper();
    int currentTrialCount = 0;
    boolean onWrongAnswer = false;
    List<String> wrongWords;
    private static final String BUDDY_MESSAGE = "Let's try again. Say ";

    private int delayBeforeStop = 4000; // startRecording stop if 4 second no voice input
    Handler handler = new Handler(Looper.getMainLooper());
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            stopRecording();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_buddy);

        setFullScreenLandscape();

        init();
        loadBookData();
        loadPageData();

        initTextToSpeech();
        initSpeechToText();
    }

    private void init(){
        sessionManager = new SessionManager(this);

        iv_avatar = findViewById(R.id.iv_avatar);
        btn_listen = findViewById(R.id.btn_listen);
        btn_speak = findViewById(R.id.btn_speak);

        btn_listen.setOnClickListener(v-> handlePlayingAudio());
        btn_speak.setOnClickListener(v -> handleRecord());

        if(!sessionManager.getReadingBuddy().equals("")){
            fillImageByAsetName(iv_avatar, sessionManager.getReadingBuddy().getAssetName());
        }

        tv_page_text = findViewById(R.id.tv_page_text);
        iv_page_image = findViewById(R.id.iv_page_image);
        ll_page_image = findViewById(R.id.ll_page_image);
        ll_dialog_balon = findViewById(R.id.ll_dialog_balon);
        ll_dialog_balon.setVisibility(View.GONE);
        tv_buddy_message = findViewById(R.id.tv_buddy_message);

        tv_point = findViewById(R.id.tv_reading_buddy_point);
        pointHelper.setTextBoxPoint(tv_point);

        spellChecker = SpellChecker.getInstance();
    }

    private void setFullScreenLandscape(){
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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
    }

    private void fillImageByAsetName(ImageView imageView, String name){
        String uri = "@drawable/" + name;  // where myresource (without the extension) is the file
        int imageResource = getResources().getIdentifier(uri, null, getPackageName());
        Drawable res = getResources().getDrawable(imageResource);
        imageView.setImageDrawable(res);
    }

    private void loadBookData(){
        try {
            // Get the JSON file from assets folder
            InputStream inputStream = getAssets().open("placeholder_book_list.json");

            // Create a byte array to hold the data from the input stream
            byte[] buffer = new byte[inputStream.available()];
            inputStream.read(buffer);
            inputStream.close();

            // Convert the byte array into a string
            String json = new String(buffer, "UTF-8");
            // Parse the JSON string
            JSONObject jsonObject = new JSONObject(json);
            // Get the book array from the JSON object
            JSONArray bookArray = jsonObject.getJSONArray("book");
            // Get the first book object from the book array
            int bookIndex = (int) (Math.random() * bookArray.length());
            JSONObject bookObject = bookArray.getJSONObject(bookIndex);

            pageText = new ArrayList<>();
            pageImage = new ArrayList<>();
            pageAudio = new ArrayList<>();
            pageAudioTranscript = new ArrayList<>();

            // Get the pages array from the book object
            JSONArray pagesArray = bookObject.getJSONArray("pages");

            for (int i = 0; i < pagesArray.length(); i++) {
                JSONObject pageObject = pagesArray.getJSONObject(i);
                if(pageObject.getBoolean("is_story_page") && !pageObject.getString("text").equals("")){
                    pageText.add(pageObject.getString("text"));
                    pageImage.add(pageObject.getString("image"));
                    pageAudio.add(pageObject.getString("audio_url"));
                    if(!pageObject.getString("audio_url").equals("")){
                        if(pageObject.has("transcript"))
                            pageAudioTranscript.add(pageObject.getString("transcript"));
                        else
                            pageAudioTranscript.add("");
                    }
                    else{
                        pageAudioTranscript.add("");
                    }
                }
            }

        } catch ( JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPageData(){
        if(pageText.size() <= 0)
            return;

        ll_page_image.setVisibility(View.VISIBLE);
        if(pageImage.get(currentPage).equals("")){
            ll_page_image.setVisibility(View.GONE);
        }

        setPageText();

        Picasso picasso = Picasso.get();
        picasso.load(pageImage.get(currentPage)).into(iv_page_image);
    }

    private void setPageText(){
        currentTrialCount = 0;
        onWrongAnswer = false;
        ll_dialog_balon.setVisibility(View.GONE);

        tv_page_text.setText(pageText.get(currentPage));

        originalWord = new ArrayList<>();
        originalWordToCheck = new ArrayList<>();
        String question = tv_page_text.getText().toString().trim();
        String[] word =question.split(" ");
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

    private void initTextToSpeech(){

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    textToSpeechIsInitialized = true;
                    setTextToSpeechLanguage();
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
                textToSpeech.setLanguage(Locale.UK);
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
        textToSpeech.setVoice(voice);
        textToSpeech.setPitch(pitch);
        textToSpeech.setSpeechRate(speechRate);
    }

    private void handlePlayingAudio(){
        if(!isPlayingAudio)
            playAudio();
        else
            stopAudio();
    }

    private void playAudio(){
        enableRecord(false);
        if(!onWrongAnswer){
            if(!pageAudio.get(currentPage).equals(""))
                playBookPageAudio();
            else
                startTextToSpeech(pageText.get(currentPage), tv_page_text, pageText.get(currentPage));
        }
        else{
            startTextToSpeech(
                    BUDDY_MESSAGE + "\n" + wrongWords.get(0),
                    tv_buddy_message,
                    BUDDY_MESSAGE + "`" + spellChecker.getSyllable(wrongWords.get(0)) + "`"
            );
        }
    }

    private void stopAudio(){
        enableRecord(true);
        if (mediaPlayer != null){
            if(mediaPlayer.isPlaying())
                stopMediaPlayer();
        }
        if(textToSpeech.isSpeaking())
            textToSpeech.stop();
        setPageText();
    }

    void enableRecord(Boolean isEnable){
        isPlayingAudio = !isEnable;
        btn_speak.setEnabled(isEnable);
    }

    private void playBookPageAudio(){
        // Initialize the media player and handler
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        handlerMedia = new Handler();

        // Set the audio URL and prepare the media player
        try {
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
            mediaPlayer.setDataSource(pageAudio.get(currentPage));
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Get the transcript from JSON and set it to the textview
        transcript = parseJson(pageAudioTranscript.get(currentPage));

        StringBuilder stringBuilder = new StringBuilder();
        for (List<Object> word : transcript) {
            stringBuilder.append(word.get(0));
            stringBuilder.append(" ");
        }
        tv_page_text.setText(stringBuilder.toString());

        mediaPlayer.start();
        highlightWordFromAudio(currentWordIndex);
        startTracking();
    }

    private void startTextToSpeech(String speakSentence, TextView textView, String displaySentence){

        if(!textToSpeechIsInitialized){
            Toast.makeText(getApplicationContext(), "Text to speech still not Ready", Toast.LENGTH_LONG).show();
            return;
        }

        String text = "";

        if(pageText.size() <= 0)
            return;

        for(int i = 0; i < punctuation.length; i++){
            text = speakSentence.toLowerCase().replaceAll(punctuation[i],"");
        }

        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onDone(String utteranceId) {
                textView.setText(displaySentence);
                enableRecord(true);
            }
            @Override
            public void onError(String utteranceId) {}
            @Override
            public void onStart(String utteranceId) {}
            @Override
            public void onRangeStart(String utteranceId, final int start, final int end, int frame) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Spannable textWithHighlights = new SpannableString(displaySentence);
                        int delimiterIndex = displaySentence.indexOf("-", start);
                        if (delimiterIndex >= 0 && delimiterIndex <= end && delimiterIndex > start) {
                            // If a delimiter is found within the range, adjust the indices
                            textWithHighlights.setSpan(new ForegroundColorSpan(Color.BLUE), start, delimiterIndex, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                            textWithHighlights.setSpan(new ForegroundColorSpan(Color.BLUE), delimiterIndex, end + 2, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        } else {
                            // Otherwise, apply the span to the entire range
                            textWithHighlights.setSpan(new ForegroundColorSpan(Color.BLUE), start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                        }
                        textView.setText(textWithHighlights);
                    }
                });
            }
        });

        if(textToSpeechIsInitialized) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "0");
        }
    }

    private void initSpeechToText(){
        //request runtime permission
        if(!checkPermissionFromDevice())
            requestPermission();

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(this);
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

    private void handleRecord(){
        if(!isRecording)
            startRecording();
        else
            stopRecording();
    }

    private void startRecording() {
        currentTrialCount++;
        partialSpeechResult = "";
        speechRecognizer.startListening(recognizerIntent);
        isRecording = true;
        btn_listen.setEnabled(false);
    }

    private void stopRecording() {
        speechRecognizer.stopListening();
        isRecording = false;
        btn_listen.setEnabled(true);
        if(onWrongAnswer){
            setBuddyMessage();
            ll_dialog_balon.setVisibility(View.VISIBLE);
        }
    }

    private void setBuddyMessage(){
        if(wrongWords.size() <= 0)
            return;
        String msg = BUDDY_MESSAGE + "`" + spellChecker.getSyllable(wrongWords.get(0)) + "`";
        tv_buddy_message.setText(msg);
        highlightWrongWords();
    }

    void highlightWrongWords(){
        for(int i = 0; i < originalWord.size(); i++){
            if(wrongWords.contains(originalWordToCheck.get(i))){
                highlightWord[i] = "<font color=#0000FF>" + originalWord.get(i) + "</font>";
            }
            else{
                highlightWord[i] = "<font color=#000000>" + originalWord.get(i) + "</font>";
            }
        }
        tv_page_text.setText(Html.fromHtml(TextUtils.join(" ", highlightWord)));
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {}
    @Override
    public void onRmsChanged(float v) {}
    @Override
    public void onBufferReceived(byte[] bytes) {}
    @Override
    public void onResults(Bundle bundle) {}
    @Override
    public void onEvent(int i, Bundle bundle) {}
    @Override
    public void onBeginningOfSpeech() {
        handler.removeCallbacks(runnable);
    }
    @Override
    public void onEndOfSpeech() {
        handler.postDelayed(runnable, delayBeforeStop);
    }
    @Override
    public void onError(int i) {
        Log.e("ERROR SPEECH", "onError: " + getErrorText(i));
        btn_speak.setEnabled(true);
        isRecording = false;
    }
    @Override
    public void onPartialResults(Bundle bundle) {
        ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if(data.size() <= 0 || !isRecording)
            return;
        partialSpeechResult = (String) data.get(data.size() - 1).replace("'","");
        pronunciationCheck();
    }

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
        String matchColor = onWrongAnswer ? "#000000" : "#00b250";
        String unmatchedColor = onWrongAnswer ? "#0000FF" : "#f06832";

        if(partialSpeechResult.trim().equals(""))
            return;

        String[] answerWord = partialSpeechResult.toLowerCase().trim().split(" ");

        if(!onWrongAnswer)
        {
            wrongWords = new ArrayList<>();

            for(int i = 0; i < originalWordToCheck.size(); i++){
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

                    //if(current_trial_count >= auto_correct_trial_count){
                    //isRight = true;
                    //}
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

            tv_page_text.setText(Html.fromHtml(TextUtils.join(" ", highlightWord)));
            Log.d("TAG", "pronunciationCheck: " + partialSpeechResult.toLowerCase().trim());
            Log.d("TAG", "target: " + originalWordToCheck);

            int rightWord = 0;
            for (int i = 0; i < wordIsRight.length; i ++){
                if(wordIsRight[i])
                    rightWord++;
            }

            if(rightWord >= wordIsRight.length){
                matchSentence();
            }
            else{
                if(answerWord.length >= originalWordToCheck.size()){
                    pointHelper.wrongAnswer();
                    onWrongAnswer = true;
                    Log.d("TAG", "pronunciationCheck: ");
                }
            }
        }
        else
        {
            Log.d("TAG", "pronunciationCheck: " + partialSpeechResult.toLowerCase().trim());
            Log.d("TAG", "target: " + wrongWords);
            if(wrongWords.size() > 0)
            {
                for (int i = 0; i < answerWord.length; i++) {
                    if(wrongWords.contains(answerWord[i])){
                        wrongWords.remove(answerWord[i]);
                        setBuddyMessage();
                        if(wrongWords.size() <= 0)
                            matchSentence();
                    }
                }
            }
            else{
                matchSentence();
            }
        }

    }

    private void matchSentence(){
        ll_dialog_balon.setVisibility(View.GONE);
        stopRecording();
        pointHelper.rightAnswer();
        if(currentPage < pageText.size()){
            currentPage++;
            setPageText();
        }
        else{
            // Finish the book
        }
    }

    // Parse the JSON transcript and return a list of lists containing the word, start time, and end time
    private List<List<Object>> parseJson(String json) {
        return new Gson().fromJson(json, new TypeToken<List<List<Object>>>() {}.getType());
    }

    // Highlight the current word in the textview
    private void highlightWordFromAudio(int index) {
        tv_page_text.setText("");
        String[] ori_words = pageText.get(currentPage).split("\\s+");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < ori_words.length; i++) {
            List<Object> word = transcript.get(i);
            String text = word.get(0).toString();
            if (i == index) {
                stringBuilder.append("<font color='#0000FF'>");
                stringBuilder.append(ori_words[i]);
                stringBuilder.append("</font> ");
            } else {
                stringBuilder.append(ori_words[i]);
                stringBuilder.append(" ");
            }
        }
        tv_page_text.setText(Html.fromHtml(stringBuilder.toString()));
    }

    // Start tracking the audio progress and update the highlighted word
    private void startTracking() {
        if (runnableMedia != null) {
            return;
        }
        runnableMedia = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer.isPlaying()) {
                    float currentTime = mediaPlayer.getCurrentPosition() / 1000f;
                    if(transcript.size() == currentWordIndex)
                        return;

                    List<Object> currentWord = transcript.get(currentWordIndex);
                    float startTime = Float.parseFloat(currentWord.get(1).toString());
                    float endTime = Float.parseFloat(currentWord.get(2).toString());
                    if (currentTime >= endTime) {
                        currentWordIndex++;
                        highlightWordFromAudio(currentWordIndex);
                    }
                }
                handlerMedia.postDelayed(this, 100);
            }
        };
        handlerMedia.postDelayed(runnableMedia, 100);
    }

    // Stop tracking the audio progress
    private void stopTracking() {
        handlerMedia.removeCallbacks(runnableMedia);
        runnableMedia = null;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopMediaPlayer();
        setPageText();
    }

    void stopMediaPlayer(){
        enableRecord(true);
        stopTracking();
        currentWordIndex = 0;
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAudio();
    }
}