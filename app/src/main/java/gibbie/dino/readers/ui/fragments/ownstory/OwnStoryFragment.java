package gibbie.dino.readers.ui.fragments.ownstory;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.VolleyMultipartRequest;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.databinding.FragmentOwnStoryBinding;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;

public class OwnStoryFragment extends Fragment implements RecognitionListener  {
    private FragmentOwnStoryBinding binding;
    BottomNavigationView navBar;
//    AdView adView;
//    ImageView iv_own_story;
    SessionManager sessionManager;

    private List<BookTemplateModel> templateList;
    BookTemplateModel selectedTemplate;
    private RecyclerView recyclerView;
    StoryTemplateAdapter adapter;
    LinearLayout ll_button_group;
    AppCompatButton btn_yes, btn_no;
    LinearLayout ll_input_layout;
    TextView tv_story;
    EditText et_story;
    AppCompatButton btn_speak, btn_listen, btn_edit;
    LinearLayout outline_btn_speak, outline_btn_listen, outline_btn_edit;
    boolean isEditing;
    AppCompatButton btn_save, btn_next;

    private List<OwnStoryPageModel> myOwnStory;

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

    boolean isEditingCover = false;
    LinearLayout ll_story_book,ll_story_cover;
    String title = "";
    Bitmap cover;
    TextView tv_title;
    EditText et_title;
    AppCompatButton btn_save_cover, btn_upload;
    final static int SELECT_IMAGE_CODE = 666;
    SharedPreferences sp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOwnStoryBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        navBar = getActivity().findViewById(R.id.nav_view);
//        adView = getActivity().findViewById(R.id.adView);
//        navBar.setVisibility(View.GONE);
//        adView.setVisibility(View.GONE);
        View parent = binding.getRoot().getRootView();
//        iv_own_story = parent.findViewById(R.id.iv_own_story);
//        iv_own_story.setVisibility(view.GONE);

        sessionManager = new SessionManager(getActivity());

        recyclerView = view.findViewById(R.id.rv_template);
        recyclerView.setVisibility(View.VISIBLE);
        ll_button_group = view.findViewById(R.id.ll_button_group);
        btn_yes = view.findViewById(R.id.btn_yes);
        btn_no = view.findViewById(R.id.btn_no);
        ll_button_group.setVisibility(View.GONE);
        ll_input_layout = view.findViewById(R.id.ll_input_layout);
        ll_input_layout.setVisibility(View.GONE);

        btn_yes.setOnClickListener(v -> selectTemplate());
        btn_no.setOnClickListener(v -> ll_button_group.setVisibility(View.GONE));

        tv_story = view.findViewById(R.id.tv_story);
        et_story = view.findViewById(R.id.et_story);
        btn_speak = view.findViewById(R.id.btn_speak);
        btn_listen = view.findViewById(R.id.btn_listen);
        btn_edit = view.findViewById(R.id.btn_edit);
        outline_btn_speak = view.findViewById(R.id.outline_btn_speak);
        outline_btn_listen = view.findViewById(R.id.outline_btn_listen);
        outline_btn_edit = view.findViewById(R.id.outline_btn_edit);

        btn_speak.setOnClickListener(v -> handleAudioRecord());
        btn_listen.setOnClickListener(v -> handleAudioPlayer());
        btn_edit.setOnClickListener(v -> handleEditStory());

        btn_next = view.findViewById(R.id.btn_next);
        btn_save = view.findViewById(R.id.btn_save);
        btn_next.setOnClickListener(v -> saveAndOpenNewPage());
        btn_save.setOnClickListener(v -> editBookCover());

        btn_upload = view.findViewById(R.id.btn_upload);
        btn_save_cover = view.findViewById(R.id.btn_save_cover);
        btn_upload.setOnClickListener(v -> uploadCover());
        btn_save_cover.setOnClickListener(v -> saveOwnStoryBook());

        ll_story_book = view.findViewById(R.id.ll_story_book);
        ll_story_cover = view.findViewById(R.id.ll_story_cover);
        isEditingCover = false;
        ll_story_book.setVisibility(View.VISIBLE);
        ll_story_cover.setVisibility(View.GONE);

        tv_title = view.findViewById(R.id.tv_title);
        et_title = view.findViewById(R.id.et_title);

        sp = getActivity().getSharedPreferences("DinoReader", MODE_PRIVATE);

        myOwnStory = new ArrayList<>();
        initSpeechRecognizer();
        checker = SpellChecker.getInstance();
        loadTemplate();

        return view;
    }

    private void selectTemplate() {
        selectedTemplate = adapter.getSelectedTemplate();

        //TO DO: Change the view based selected template

        ll_input_layout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        ll_button_group.setVisibility(View.GONE);
    }

    private void saveAndOpenNewPage() {
        OwnStoryPageModel page = new OwnStoryPageModel(
            myOwnStory.size() + 1,
            myOwnStory.size() + 1,
            tv_story.getText().toString(),
            ""
        );
        page.setPageLocalAudioUrl(audioFilePath);
        myOwnStory.add(page);
        tv_story.setText("");
    }

    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp=Base64.encodeToString(b, Base64.DEFAULT);
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

    private void editBookCover(){
        // check if data is not empty
        if(audioFilePath == null)
            return;

        // to do : add check if already saved using next button
        saveAndOpenNewPage();

        isEditingCover = true;
        ll_story_book.setVisibility(View.GONE);
        ll_story_cover.setVisibility(View.VISIBLE);
    }

    private void uploadCover(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE_CODE);
    }


    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (resultCode == RESULT_OK) {
                    if (requestCode == SELECT_IMAGE_CODE) {
                        // Get the url from data
                        final Uri selectedImageUri = data.getData();
                        if (null != selectedImageUri) {
                            ll_story_cover.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        cover = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                                        Drawable d = new BitmapDrawable(getActivity().getResources(), cover);
                                        ll_story_cover.setBackground(d);
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
    private void saveOwnStoryBook() {


        if(title.length() <= 0 || cover == null){
            Toast.makeText(getActivity(), "Fill title and upload cover first!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<OwnStoryBookModel> userBooks = getUserStoryBook();
        OwnStoryBookModel book = new OwnStoryBookModel(
                userBooks.size() + 1,
                selectedTemplate.getId(),
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

    private void backToListOwnStory(){
        BottomNavigation.fm.popBackStack();
        Fragment fragment = new OwnStoryBookListFragment();
        BottomNavigation.fm
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void uploadOwnStoryBook(){
        final ProgressDialog loading = new ProgressDialog(getActivity(), R.style.ProgressBarTheme);
        String msgTitle = "Saving the created story";
        Log.e("TAG", "uploadOwnStoryBook: " + getActivity() );
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
                                backToListOwnStory();
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
                        backToListOwnStory();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("title", title);
                params.put("template_id", String.valueOf(selectedTemplate.getId()));
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
        Volley.newRequestQueue(getActivity()).add(volleyMultipartRequest);
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

    private void handleEditStory() {
        if(!isEditing)
            editStoryText();
        else
            saveStoryText();

        if(sessionManager.isAutoCorrectOwnStorySetting()){
            if(!isEditingCover)
                tv_story.setText(Html.fromHtml(checker.checkSentence(tv_story.getText().toString())));
            else
                tv_title.setText(Html.fromHtml(checker.checkSentence(tv_title.getText().toString())));
        }
    }

    private void editStoryText() {
        btn_speak.setEnabled(false);
        btn_listen.setEnabled(false);
        isEditing = true;
        outline_btn_edit.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.button_outline));
        if(!isEditingCover) {
            et_story.setText(tv_story.getText());
            tv_story.setVisibility(View.GONE);
            et_story.setVisibility(View.VISIBLE);
            et_story.requestFocus();
        }
        else{
            et_title.setText(tv_title.getText());
            title = tv_title.getText().toString();
            tv_title.setVisibility(View.GONE);
            et_title.setVisibility(View.VISIBLE);
            et_title.requestFocus();
        }
    }

    private void saveStoryText(){
        btn_speak.setEnabled(true);
        btn_listen.setEnabled(true);
        isEditing = false;
        outline_btn_edit.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.button_outline_null));
        if(!isEditingCover){
            tv_story.setText(et_story.getText());
            tv_story.setVisibility(View.VISIBLE);
            et_story.setVisibility(View.GONE);
        }
        else{
            tv_title.setText(et_title.getText());
            tv_title.setVisibility(View.VISIBLE);
            et_title.setVisibility(View.GONE);
        }
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

    private boolean checkPermissionFromDevice() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO);
        return  write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                record_audio_result == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{
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
                    Toast.makeText(getActivity(), "Permission Granted", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            break;
        }
    }

    private  void  loadTemplate(){
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        templateList = new ArrayList<>();
        String templateListJson = sp.getString("myOwnStoryTemplates", null) ;
        if(templateListJson != null){
            Gson gson = new Gson();
            Type listType = new TypeToken<List<BookTemplateModel>>() {}.getType();
            templateList =  gson.fromJson(templateListJson, listType);
        }

        adapter = new StoryTemplateAdapter(getActivity(), ll_button_group, templateList);
        recyclerView.setAdapter(adapter);
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
            if(isEditingCover)
                tv_title.setText(RECORD_MSG);
            else
                tv_story.setText(RECORD_MSG);
            outline_btn_speak.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.button_outline));
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
        if(isEditingCover)
            tv_title.setText("");
        else
            tv_story.setText("");

        // Stop recording audio
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder = null;
        outline_btn_speak.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.button_outline_null));

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
        outline_btn_listen.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.button_outline));

        if(isRecognizing) {
            resultText = "";
            startSpeechToText();
        }
        else{
            initMediaPlayer();
            mediaPlayer.start();
        }
    }

    private void stopMediaPlayer(){
        isPlayingFile = false;
        btn_listen.setEnabled(true);
        btn_speak.setEnabled(true);
        btn_edit.setEnabled(true);
        outline_btn_listen.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.button_outline_null));
        if(mediaPlayer != null){
            mediaPlayer.pause();
        }
    }

    private void initSpeechRecognizer(){
        // Create a SpeechRecognizer instance
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getActivity());
        speechRecognizer.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,this.getActivity().getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, delayBeforeStop);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, delayBeforeStop);
    }

    private void startSpeechToText()
    {
        if (!SpeechRecognizer.isRecognitionAvailable(getActivity())) {
            return;
        }
        resultText = "";
        if(!isEditingCover)
            tv_story.setText(resultText);
        else
            tv_title.setText(resultText);
        speechRecognizer.startListening(recognizerIntent);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
//        navBar.setVisibility(View.VISIBLE);
//        adView.setVisibility(View.VISIBLE);
//        iv_own_story.setVisibility(View.VISIBLE);
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
                // Stop listening for speech
                if(isRecognizing){
                    speechRecognizer.stopListening();
                    isRecognizing = false;
                }
            }
        });
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
        if(!isEditingCover)
            tv_story.setText(getErrorText(i));
        else
            tv_title.setText(getErrorText(i));
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

        if(!isEditingCover)
            tv_story.setText(result);
        else{
            tv_title.setText(result);
            title = result;
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
