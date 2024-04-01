package gibbie.dino.readers.ui.activities.ownstory;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.commonclasses.VolleyMultipartRequest;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.fragments.ownstory.OwnStoryBookModel;
import gibbie.dino.readers.ui.fragments.ownstory.OwnStoryPageModel;
import gibbie.dino.readers.ui.fragments.ownstory.SpellChecker;

public class CreateOwnStory extends AppCompatActivity implements RecognitionListener {

    SessionManager sessionManager;

    boolean isEditing;

    ImageView iv_canvas;
    AppCompatButton btn_speak, btn_listen, btn_edit;
    ImageView btn_add_cover, btn_next, btn_prev, btn_delete, btn_save;
    Bitmap cover;
    final static int SELECT_IMAGE_CODE = 666;
    TextView tv_story;
    EditText et_story;

    ImageView btn_close;

    private List<OwnStoryPageModel> myOwnStory;
    int currentPage = 0;
    CanvasData canvas;

    private SpeechRecognizer speechRecognizer = null;
    private Intent recognizerIntent;
    boolean isRecognizing = false;
    private String resultText = "";

    private final int delayBeforeStop = 5000;

    private MediaRecorder mediaRecorder;
    final int REQUEST_PERMISSION_CODE = 1000;
    final String RECORD_MSG = "Is recording...";
    String audioFilePath = "";
    boolean isRecording = false;

    MediaPlayer mediaPlayer;
    boolean isPlayingFile = false;
    SpellChecker checker;

    boolean isSettingTitle = false;
    String title = "";
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_own_story);
        Functions.enableFullscreen(this, true);

        sessionManager = new SessionManager(this);

        canvas = (CanvasData) getIntent().getSerializableExtra("canvas");

        iv_canvas = findViewById(R.id.iv_canvas);
        iv_canvas.setImageResource(getResources().getIdentifier(canvas.getCover(), "drawable", getPackageName()));
        tv_story = findViewById(R.id.tv_story);
        et_story = findViewById(R.id.et_story);
        et_story.setVisibility(View.VISIBLE);
        et_story.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showKeyboard();
            }
        });

        btn_speak = findViewById(R.id.btn_speak);
        btn_listen = findViewById(R.id.btn_listen);
        btn_edit = findViewById(R.id.btn_edit);
        btn_close = findViewById(R.id.btn_close);
        btn_add_cover = findViewById(R.id.btn_add_cover);
        btn_prev = findViewById(R.id.btn_prev);
        btn_next = findViewById(R.id.btn_next);
        btn_delete = findViewById(R.id.btn_delete);
        btn_save = findViewById(R.id.btn_save);

        btn_speak.setOnClickListener(v -> handleAudioRecord());
        btn_listen.setOnClickListener(v -> handleAudioPlayer());
        btn_edit.setOnClickListener(v -> handleEditStory());
        btn_close.setOnClickListener( v-> this.finish());
        btn_add_cover.setOnClickListener( v-> this.uploadCover());
        btn_next.setOnClickListener(v -> saveAndOpenNewPage());
        btn_prev.setOnClickListener(v -> showPreviousPage());
        btn_delete.setOnClickListener(v -> deletePage());
        btn_save.setOnClickListener(v -> saveBook());

        myOwnStory = new ArrayList<>();
        initSpeechRecognizer();
        checker = SpellChecker.getInstance();
        showAdditionalButton();

        sp = getSharedPreferences("DinoReader", MODE_PRIVATE);
    }

    private void showAdditionalButton(){
        btn_save.setVisibility(myOwnStory.size() > 0 ? View.VISIBLE : View.GONE);
        btn_prev.setVisibility(currentPage > 0 ? View.VISIBLE : View.GONE);
        btn_next.setVisibility(myOwnStory.size() > currentPage ? View.VISIBLE : View.GONE);
        btn_delete.setVisibility(myOwnStory.size() > currentPage ? View.VISIBLE : View.GONE);
        btn_edit.setVisibility(myOwnStory.size() > currentPage ? View.VISIBLE : View.GONE);
    }

    private void saveBook(){
        if(!isSettingTitle){
            isSettingTitle = true;
            hideAllAdditionalButton();
            audioFilePath = "";
            tv_story.setText("Record for title");
        }
        else
        {
            saveOwnStoryBook();
        }
    }

    private void saveOwnStoryBook() {
        if(title.length() <= 0 || cover == null){
            Toast.makeText(CreateOwnStory.this, "Fill title and upload cover first!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<OwnStoryBookModel> userBooks = getUserStoryBook();
        OwnStoryBookModel book = new OwnStoryBookModel(
                userBooks.size() + 1,
                canvas.getId(),
                title,
                "",
                myOwnStory,
                ""
        );

        book.setLocalAudioUrl(audioFilePath);
        book.setCoverFromImageString(BitMapToString(cover));
        userBooks.add(book);

        String myOwnStoryListJson = new Gson().toJson(userBooks);
        SharedPreferences.Editor Ed = sp.edit();
        Ed.putString("myOwnStoryBook", myOwnStoryListJson);
        Ed.apply();

        uploadOwnStoryBook();
    }

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    private List<OwnStoryBookModel> getUserStoryBook() {
        List<OwnStoryBookModel> books = new ArrayList<>();

        String bookListJson = sp.getString("myOwnStoryBook", null);
        if(bookListJson != null) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<OwnStoryBookModel>>() {}.getType();
            books = gson.fromJson(bookListJson, listType);
        }
        return books;
    }

    private void uploadOwnStoryBook(){
        final ProgressDialog loading = new ProgressDialog(CreateOwnStory.this, R.style.ProgressBarTheme);
        String msgTitle = "Saving the created story";
        Log.e("TAG", "uploadOwnStoryBook: " + CreateOwnStory.this);
        loading.setCancelable(false);
        loading.setMessage("Please Wait...");
        loading.setTitle(msgTitle);
        loading.show();

        //our custom volley request
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, WebUrl.UPLOADOWNSTORYURL,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));
                            boolean isSuccess = obj.getBoolean("success");
                            if(isSuccess)
                            {
                                loading.dismiss();
                                finish();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }},
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("TAG", "onErrorResponse: " + error );
                        loading.dismiss();
                        finish();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("title", title);
                params.put("template_id", String.valueOf(canvas.getId()));
                params.put("user_email", sessionManager.getEmail());
                // Uploading Own Story Page
                params.put("page_count", String.valueOf(myOwnStory.size()));
                for (int i = 0; i < myOwnStory.size(); i++) {
                    params.put("page_story_" + i, myOwnStory.get(i).getStory());
                }
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put("cover", new DataPart("cover.png", getFileDataFromDrawable(cover)));
                if(audioFilePath != null){
                    try {
                        params.put("audio", new DataPart("title.mp3",convertToBytes(audioFilePath)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // Uploading Own Story Page
                for (int i = 0; i < myOwnStory.size(); i++) {
                    String filename = "page_" + i + ".mp3";
                    try {
                        params.put(
                                "page_audio_url_" + i ,
                                new DataPart(filename,convertToBytes(myOwnStory.get(i).getLocalAudioUrl()))
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", sessionManager.getAccesstoken());
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        //adding the request to volley
        volleyMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(CreateOwnStory.this).add(volleyMultipartRequest);
    }

    private static byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static byte[] convertToBytes(String filePath) throws IOException {
        File file = new File(filePath);
        FileInputStream fis = new FileInputStream(file);
        byte[] fileBytes = new byte[(int) file.length()];
        fis.read(fileBytes);
        fis.close();
        return fileBytes;
    }


    private void hideAllAdditionalButton(){
        btn_prev.setVisibility(View.GONE);
        btn_next.setVisibility(View.GONE);
        btn_edit.setVisibility(View.GONE);
        btn_delete.setVisibility(View.GONE);
    }

    private void saveAndOpenNewPage() {
        if(audioFilePath.length() == 0)
            return;

        // check save or update
        if(myOwnStory.size() == currentPage){
            OwnStoryPageModel page = new OwnStoryPageModel(
                    myOwnStory.size() + 1,
                    myOwnStory.size() + 1,
                    tv_story.getText().toString(),
                    ""
            );
            page.setPageLocalAudioUrl(audioFilePath);
            myOwnStory.add(page);
            tv_story.setText("Speak to record and wait while system recognize the voice");
            audioFilePath = "";
        }
        else{
            myOwnStory.get(currentPage).setStory(tv_story.getText().toString());
            myOwnStory.get(currentPage).setPageLocalAudioUrl(audioFilePath);

            if(myOwnStory.size() > currentPage + 1){
                tv_story.setText(myOwnStory.get(currentPage + 1).getStory());
                audioFilePath = myOwnStory.get(currentPage + 1).getLocalAudioUrl();
            }
            else{
                tv_story.setText("Speak to record and wait while system recognize the voice");
                audioFilePath = "";
            }
        }
        currentPage ++;
        showAdditionalButton();
    }

    private void showPreviousPage(){
        currentPage--;
        tv_story.setText(myOwnStory.get(currentPage).getStory());
        audioFilePath = myOwnStory.get(currentPage).getLocalAudioUrl();
        showAdditionalButton();
    }

    private void deletePage(){
        myOwnStory.remove(currentPage);
        if(myOwnStory.size() > 0){
            if(currentPage > 0){
                showPreviousPage();
            }
            else{
                currentPage = 0;
                tv_story.setText(myOwnStory.get(currentPage).getStory());
                audioFilePath = myOwnStory.get(currentPage).getLocalAudioUrl();
                showAdditionalButton();
            }

        }
        else{
            currentPage = 0;
            tv_story.setText("Speak to record and wait while system recognize the voice");
            audioFilePath = "";
            showAdditionalButton();
        }
    }

    private void uploadCover(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE_CODE);
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (resultCode == RESULT_OK) {
                    if (requestCode == SELECT_IMAGE_CODE) {
                        // Get the url from data
                        final Uri selectedImageUri = data.getData();
                        if (null != selectedImageUri) {
                            btn_add_cover.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        cover = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                                        Drawable d = new BitmapDrawable(getResources(), cover);
                                        btn_add_cover.setImageBitmap(cover);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                }
            }
        }).start();

    }

    private void initSpeechRecognizer(){
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, delayBeforeStop);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, delayBeforeStop);
    }

    private void handleAudioRecord() {
        if(!isRecording)
            startRecording();
        else
            stopRecording();
    }

    private void handleAudioPlayer() {
        if(!isPlayingFile)
            playAudioFile(false);
        else
            stopMediaPlayer();
    }

    private void handleEditStory() {
        if(!isEditing)
            editStoryText();
        else
            saveStoryText();

        if(sessionManager.isAutoCorrectOwnStorySetting()){
                tv_story.setText(Html.fromHtml(checker.checkSentence(tv_story.getText().toString())));
        }
    }
    private void editStoryText() {
        showKeyboard();
        btn_speak.setEnabled(false);
        btn_listen.setEnabled(false);
        isEditing = true;
        et_story.setText(tv_story.getText());
        tv_story.setVisibility(View.GONE);
        et_story.setVisibility(View.VISIBLE);
        et_story.requestFocus();
    }

    private void saveStoryText(){
        hideKeyboard();
        btn_speak.setEnabled(true);
        btn_listen.setEnabled(true);
        isEditing = false;
        tv_story.setText(et_story.getText());
        tv_story.setVisibility(View.VISIBLE);
        et_story.setVisibility(View.GONE);
    }

    public void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(et_story, InputMethodManager.SHOW_IMPLICIT);
    }
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_story.getWindowToken(), 0);
    }


    private boolean checkPermissionFromDevice() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return  write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                record_audio_result == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        }, REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            break;
        }
    }

    private void startRecording() {
        // Request permissions
        if(!checkPermissionFromDevice())
            requestPermission();

        btn_listen.setEnabled(false);
        btn_edit.setEnabled(false);

        // Start recording audio
        String filename = "recorded_audio_" + System.currentTimeMillis() + ".mp3";
        String folderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        String appName = "DinoReader";
        folderPath = folderPath + File.separator + appName + File.separator + "audio";

        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        String pathSave =  folderPath + File.separator + filename;
        audioFilePath = pathSave;

        setupMediaRecorder(pathSave);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            tv_story.setText(RECORD_MSG);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupMediaRecorder(String pathSave) {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(128000);
        mediaRecorder.setAudioSamplingRate(44100);
        mediaRecorder.setOutputFile(pathSave);
    }

    private void stopRecording() {
        isRecording = false;
        tv_story.setText("");

        // Stop recording audio
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;

        // auto Start Listen when its stop
        playAudioFile(true);
    }


    private void playAudioFile(boolean isRecognizing){
        if(audioFilePath.length() == 0){
            return;
        }
        this.isRecognizing = isRecognizing;
        isPlayingFile = true;
        btn_speak.setEnabled(false);

        if(isRecognizing) {
            resultText = "";
            startSpeechToText();
        }
        else{
            initMediaPlayer();
            mediaPlayer.start();
        }
    }

    private void initMediaPlayer(){
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopMediaPlayer();
                if(isRecognizing){
                    speechRecognizer.stopListening();
                    isRecognizing = false;
                }
            }
        });
    }


    private void stopMediaPlayer(){
        isPlayingFile = false;
        btn_listen.setEnabled(true);
        btn_speak.setEnabled(true);
        btn_edit.setEnabled(true);
        if(mediaPlayer != null){
            mediaPlayer.pause();
        }
    }

    private void startSpeechToText()
    {
        if (!SpeechRecognizer.isRecognitionAvailable(this))
            return;

        resultText = "";
        tv_story.setText(resultText);
        speechRecognizer.startListening(recognizerIntent);
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        initMediaPlayer();
        mediaPlayer.start();
    }

    @Override
    public void onBeginningOfSpeech() {}
    @Override
    public void onRmsChanged(float v) {}
    @Override
    public void onBufferReceived(byte[] bytes) {}
    @Override
    public void onEndOfSpeech() {}

    @Override
    public void onError(int i) {
        tv_story.setText(getErrorText(i));
    }

    @Override
    public void onResults(Bundle results) {}

    @Override
    public void onPartialResults(Bundle results) {
        ArrayList<String> res = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if(res.size() <= 0)
            return;

        resultText = res.get(0);

        String result = String.valueOf(resultText.charAt(0)).toUpperCase() + resultText.substring(1, resultText.length()) + ".";
        tv_story.setText(result);
        if(isSettingTitle)
        {
            title = result;
        }
        else
        {
            btn_next.setVisibility(View.VISIBLE);
            btn_edit.setVisibility(View.VISIBLE);
        }
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
}