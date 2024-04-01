package gibbie.dino.readers.ui.activities.placementtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import gibbie.dino.readers.R;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;

public class PlacementTestResultActivity extends AppCompatActivity {
    SessionManager sessionManager;
    ImageView book_image, medal_image;
    TextView txt_title, txt_detail;
    Button btn_next;

    String bookID;
    int total_word_count, total_right_word_count;
    boolean bookIsCompleted = false;

    private final static String messageText = "\nYour Reading Level is ";

    boolean isGradingTest = false;
    int reading_level = 1;
    int reading_level_result = 1;
    long totalTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placement_test_result);
        init();
    }

    private void init(){

        sessionManager = new SessionManager(this);
        // Set landscape screen and hide status bar and navigation phone UI
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LOW_PROFILE
        );

        Intent intent = getIntent();
        bookID = intent.getExtras().getString("id");
        String cover = intent.getExtras().getString("cover");
        String title = intent.getExtras().getString("title");
        reading_level = intent.getExtras().getInt("reading_level");
        totalTime = intent.getExtras().getLong("total_time", 0);
        total_word_count = intent.getExtras().getInt("total_word_count");
        total_right_word_count = intent.getExtras().getInt("total_right_word_count");

        book_image = findViewById(R.id.book_image);
        txt_title = findViewById(R.id.txt_title);
        txt_detail = findViewById(R.id.txt_detail);
        medal_image = findViewById(R.id.medal_image);
        btn_next = findViewById(R.id.btn_next);
        btn_next.setOnClickListener(v -> onClickNextButton());

        Picasso.get().load(cover).into(book_image);
        txt_title.setText(title);

        btn_next.setText("Back to menu");

        checkReadingLevel();
    }

    private void checkReadingLevel() {
        int percentage = 0;
        if (total_right_word_count > 0) {
            percentage = Math.round(((float) total_right_word_count / total_word_count) * 100);
        }

        if (percentage >= 90) {
            reading_level_result = Math.min(reading_level + 1, sessionManager.listOfGrade.size());
        } else if (percentage >= 51 && percentage <= 89) {
            reading_level_result = reading_level;
        } else if (percentage <= 50) {
            reading_level_result = Math.max(reading_level - 1, 0);
        }

        String readingLevelName = sessionManager.listOfGrade.get(reading_level_result - 1) + " Reading";
        String msg = "The result is " + String.valueOf(percentage) + " %" + messageText + readingLevelName;
        txt_detail.setText(msg);
        sessionManager.setReadingLevel(String.valueOf(reading_level_result));
        sendPlacementTestResult();
    }

    private void sendPlacementTestResult(){
        // Sending the reading progress here :
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("user_id", sessionManager.getUserId());
            requestBody.put("profile_id", sessionManager.getProfileId());
            requestBody.put("book_id", bookID);
            requestBody.put("total_word_count", total_word_count);
            requestBody.put("total_right_word_count", total_right_word_count);
            requestBody.put("reading_time", totalTime);
            requestBody.put("reading_level_result", reading_level_result);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsArrayRequest = new JsonObjectRequest(
                Request.Method.POST,
                WebUrl.SAVEPLACEMENTTESTRESULT,
                requestBody,
                response -> Log.d("TAG", "Placement Test Result saved successfully"),
                error -> Log.e("TAG", "Failed to save placement test result: " + error.getMessage())
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
    }

    private void onClickNextButton() {
        Intent i = new Intent(this, BottomNavigation.class);
        this.startActivity(i);
        this.finish();
    }
}