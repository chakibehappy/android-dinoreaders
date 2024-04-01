package gibbie.dino.readers.ui.activities.placementtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.setting.ApplicationSettingActivity;
import gibbie.dino.readers.ui.activities.setting.SettingAvatarListAdapter;
import gibbie.dino.readers.ui.activities.setting.UserProfileSetting;

public class BookGradeSelectionActivity extends AppCompatActivity {

    SessionManager sessionManager;
    TextView txt_subtitle;
    int readingLevel;
    List<GradeBookData> gradeBooks;
    RecyclerView rc_book;

    GradeBookListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_grade_selection);
        sessionManager = new SessionManager(this);

        Intent intent = getIntent();
        readingLevel = intent.getIntExtra("readingLevel", 1);
        String readingLevelName =  sessionManager.listOfGrade.get(readingLevel - 1) + " Reading";
        txt_subtitle = findViewById(R.id.txt_subtitle);
        rc_book = findViewById(R.id.rc_book);

        txt_subtitle.setText(readingLevelName);
        getBookData();
    }

    private void getBookData(){
        if(Functions.checkInternet(this)){
            JSONObject request = new JSONObject();
            JsonObjectRequest jsArrayRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    WebUrl.BOOKBYREADINGLEVEL + readingLevel,
                    request,
                    response -> {
                        try {
                            if(response.getBoolean("success")){
                                JSONArray dataArray = response.getJSONArray("data");
                                String dataJson = dataArray.toString();
                                Gson gson = new Gson();
                                Type gradeBookTypeList = new TypeToken<List<GradeBookData>>() {}.getType();
                                gradeBooks = gson.fromJson(dataJson, gradeBookTypeList);
                                adapter = new GradeBookListAdapter(BookGradeSelectionActivity.this, gradeBooks, readingLevel);
                                rc_book.setAdapter(adapter);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    },
                    error -> Log.d("TAG", "onErrorResponse: " + error)
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", sessionManager.getAccesstoken());
                    headers.put("Accept", "application/json");
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
    }
}