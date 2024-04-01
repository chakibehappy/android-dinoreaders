package gibbie.dino.readers.ui.activities.setting;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.placementtest.PlacementTestActivity;

public class ApplicationSettingActivity extends AppCompatActivity implements SettingAvatarListAdapter.OnSelectionChangeListener {

    LinearLayout ll_auto_correct, ll_book_based_reading_level, ll_recommend_based_reading_level;
    LinearLayout ll_activated_time, ll_reading_time;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch switch_auto_correct, switch_book_based_reading_level, switch_recommend_based_reading_level, switch_reading_time;

    private Spinner spinner_reading_day, spinner_calculated_by, spinner_reading_time;
    TextView tv_label_reading_day, tv_label_calculated_by, tv_label_reading_time;

    RecyclerView rv_avatar;
    List<UserProfileSetting> profileSettings;

    boolean isAutoCorrect, isBookBasedReadingLevel, isRecommendBasedReadingLevel, isActiveReadingTime;
    SessionManager sessionManager;

    SettingAvatarListAdapter adapter;
    UserProfileSetting selectedProfile;

    CustomSpinnerAdapter daysAdapter;
    List<ComboBoxItem> dayItems;
    boolean[] selectedDays;

    List<ComboBoxItem> calculatedByItems;
    List<ComboBoxItem> readingTimeItems;

    LinearLayout ll_placement_test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application_setting);
        init();
    }

    private void init(){
        sessionManager = new SessionManager(this);

        ll_auto_correct = findViewById(R.id.ll_auto_correct);
        ll_book_based_reading_level = findViewById(R.id.ll_book_based_reading_level);
        ll_recommend_based_reading_level = findViewById(R.id.ll_recommend_based_reading_level);
        ll_activated_time = findViewById(R.id.ll_activated_time);
        ll_reading_time = findViewById(R.id.ll_reading_time);
        switch_auto_correct = findViewById(R.id.switch_auto_correct);
        switch_book_based_reading_level = findViewById(R.id.switch_book_based_reading_level);
        switch_recommend_based_reading_level = findViewById(R.id.switch_recommend_based_reading_level);
        switch_reading_time = findViewById(R.id.switch_reading_time);
        tv_label_reading_day = findViewById(R.id.tv_label_reading_day);
        tv_label_calculated_by = findViewById(R.id.tv_label_calculated_by);
        tv_label_reading_time = findViewById(R.id.tv_label_reading_time);

        ll_auto_correct.setOnClickListener(v -> handleAutoCorrect());
        switch_auto_correct.setOnClickListener(v-> handleAutoCorrect());
        ll_book_based_reading_level.setOnClickListener(v -> handleBookBasedReadingLevel());
        switch_book_based_reading_level.setOnClickListener(v-> handleBookBasedReadingLevel());
        ll_recommend_based_reading_level.setOnClickListener(v -> handleRecommendBasedReadingLevel());
        switch_recommend_based_reading_level.setOnClickListener(v-> handleRecommendBasedReadingLevel());

        ll_activated_time.setOnClickListener(v -> handleActivatedTime());
        switch_reading_time.setOnClickListener(v-> handleActivatedTime());

        spinner_reading_day = findViewById(R.id.spinner_reading_day);
        dayItems = new ArrayList<>();
        dayItems.add(new ComboBoxItem("Monday", "Mon"));
        dayItems.add(new ComboBoxItem("Tuesday", "Tue"));
        dayItems.add(new ComboBoxItem("Wednesday", "Wed"));
        dayItems.add(new ComboBoxItem("Thursday", "Thu"));
        dayItems.add(new ComboBoxItem("Friday", "Fri"));
        dayItems.add(new ComboBoxItem("Saturday", "Sat"));
        dayItems.add(new ComboBoxItem("Sunday", "Sun"));

        setSelectedDays("");

        spinner_calculated_by = findViewById(R.id.spinner_calculated_by);
        calculatedByItems = new ArrayList<>();
        calculatedByItems.add(new ComboBoxItem("Each Login For", "Each Login"));
        calculatedByItems.add(new ComboBoxItem("Total Logins For", "Total Logins"));
        ArrayAdapter<ComboBoxItem> adapterCalculatedBy = new ArrayAdapter<>(this, R.layout.spinner_item, calculatedByItems);
        adapterCalculatedBy.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_calculated_by.setAdapter(adapterCalculatedBy);
        spinner_calculated_by.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ComboBoxItem selectedItem = (ComboBoxItem) parent.getItemAtPosition(position);
                saveCalculatedBy(selectedItem.getValue());
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        spinner_reading_time = findViewById(R.id.spinner_reading_time);
        readingTimeItems = new ArrayList<>();
        readingTimeItems.add(new ComboBoxItem("30", "30 Minutes"));
        readingTimeItems.add(new ComboBoxItem("60", "1 Hour"));
        readingTimeItems.add(new ComboBoxItem("120", "2 Hours"));
        readingTimeItems.add(new ComboBoxItem("180", "3 Hours"));
        readingTimeItems.add(new ComboBoxItem("240", "4 Hours"));
        readingTimeItems.add(new ComboBoxItem("300", "5 Hours"));
        readingTimeItems.add(new ComboBoxItem("360", "6 Hours"));
        ArrayAdapter<ComboBoxItem> spinerReadingTime = new ArrayAdapter<>(this, R.layout.spinner_item, readingTimeItems);
        spinerReadingTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_reading_time.setAdapter(spinerReadingTime);
        spinner_reading_time.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ComboBoxItem selectedItem = (ComboBoxItem) parent.getItemAtPosition(position);
                saveReadingTime(Integer.parseInt(selectedItem.getValue()));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        rv_avatar = findViewById(R.id.rv_avatar);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rv_avatar.setLayoutManager(layoutManager);
        profileSettings = new ArrayList<>();

        ll_placement_test = findViewById(R.id.ll_placement_test);
        ll_placement_test.setOnClickListener(view -> {
            Intent intent = new Intent(this, PlacementTestActivity.class);
            startActivity(intent);
        });

        if(Functions.checkInternet(this)){
            JSONObject request = new JSONObject();
            JsonObjectRequest jsArrayRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    WebUrl.PROFILESETTINGURL + sessionManager.getUserId(),
                    request,
                    response -> {
                        try {
                            if(response.getBoolean("success")){
                                JSONArray dataArray = response.getJSONArray("data");
                                String dataJson = dataArray.toString();
                                Log.d("TAG", "init: " + dataArray);

                                Gson gson = new Gson();
                                Type userProfileSettingListType = new TypeToken<List<UserProfileSetting>>() {}.getType();
                                profileSettings = gson.fromJson(dataJson, userProfileSettingListType);
                                adapter = new SettingAvatarListAdapter(profileSettings);
                                adapter.setOnSelectionChangeListener(ApplicationSettingActivity.this);
                                rv_avatar.setAdapter(adapter);
                                ChangeSetting(adapter.getSelectedProfile());
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

    private void handleAutoCorrect(){
        isAutoCorrect = !isAutoCorrect;
        if(selectedProfile != null)
            selectedProfile.setAutoCorrectOwnStory(isAutoCorrect);
        changeSwitchState(switch_auto_correct, isAutoCorrect);
        saveSettings();
    }

    private void handleBookBasedReadingLevel(){
        isBookBasedReadingLevel = !isBookBasedReadingLevel;
        if(selectedProfile != null)
            selectedProfile.setBookByReadingLevel(isBookBasedReadingLevel);
        changeSwitchState(switch_book_based_reading_level, isBookBasedReadingLevel);
        saveSettings();
    }

    private void handleRecommendBasedReadingLevel(){
        isRecommendBasedReadingLevel = !isRecommendBasedReadingLevel;
        if(selectedProfile != null)
            selectedProfile.setRecommendByReadingLevel(isRecommendBasedReadingLevel);
        changeSwitchState(switch_recommend_based_reading_level, isRecommendBasedReadingLevel);
        saveSettings();
    }

    private void handleActivatedTime(){
        isActiveReadingTime = !isActiveReadingTime;
        if(selectedProfile != null)
            selectedProfile.setEnableLimitTime(isActiveReadingTime);
        activateReadingTime(isActiveReadingTime);
        saveSettings();
    }

    private void activateReadingTime(boolean isActive){
        changeSwitchState(switch_reading_time, isActive);
        int textColor = isActive ? Color.BLACK : Color.GRAY;
        float alpha = isActive ? 1.0f : 0.5f;
        tv_label_reading_day.setTextColor(textColor);
        tv_label_calculated_by.setTextColor(textColor);
        tv_label_reading_time.setTextColor(textColor);

        spinner_reading_day.setAlpha(alpha);
        spinner_calculated_by.setAlpha(alpha);
        spinner_reading_time.setAlpha(alpha);

        spinner_reading_day.setEnabled(isActive);
        spinner_calculated_by.setEnabled(isActive);
        spinner_reading_time.setEnabled(isActive);
    }

    private void changeSwitchState(@SuppressLint("UseSwitchCompatOrMaterialCode") Switch selected_switch, boolean isActive){
        selected_switch.setChecked(isActive);
        ColorStateList thumbColorStateList = ColorStateList.valueOf(isActive ? Color.BLUE : Color.WHITE);
        ColorStateList trackColorStateList = ColorStateList.valueOf(isActive ? Color.BLUE : Color.GRAY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            selected_switch.setThumbTintList(thumbColorStateList);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            selected_switch.setTrackTintList(trackColorStateList);
        }
    }

    private void ChangeSetting(UserProfileSetting profile){
        selectedProfile = profile;
        isActiveReadingTime = selectedProfile.isSetLimitTime();
        activateReadingTime(isActiveReadingTime);
        isAutoCorrect = selectedProfile.isAutoCorrectOwnStory();
        changeSwitchState(switch_auto_correct, isAutoCorrect);
        isBookBasedReadingLevel = selectedProfile.isBookByReadingLevel();
        changeSwitchState(switch_book_based_reading_level, isBookBasedReadingLevel);
        isRecommendBasedReadingLevel = selectedProfile.isRecommendByReadingLevel();
        changeSwitchState(switch_recommend_based_reading_level, isRecommendBasedReadingLevel);
        
        setSelectedDays(selectedProfile.getReadingDay());
        spinner_calculated_by.setSelection(getSelectedItemPosition(calculatedByItems, selectedProfile.getCalculatedBy()));
        String time = String.valueOf(selectedProfile.getReadingTime());
        spinner_reading_time.setSelection(getSelectedItemPosition(readingTimeItems, time));
        sessionManager.resetCurrentReadingTime();
    }
    
    void setSelectedDays(String dayString){
        selectedDays = convertStringToBooleanArray(dayString);
        if(selectedDays.length <= 0){

        }
        daysAdapter = new CustomSpinnerAdapter(this, dayItems, selectedDays, "Daily", this);
        spinner_reading_day.setAdapter(daysAdapter);
    }

    public void saveSelectedDays(String dayString){
        if(selectedProfile != null)
            selectedProfile.setReadingDay(dayString);
        saveSettings();
    }

    private void saveCalculatedBy(String calculatedBy){
        if(selectedProfile != null)
            selectedProfile.setCalculatedBy(calculatedBy);
        saveSettings();
    }

    private void saveReadingTime(int time){
        if(selectedProfile != null)
            selectedProfile.setReadingTime(time);
        saveSettings();
    }

    @Override
    public void onSelectionChanged(UserProfileSetting profile) {
        ChangeSetting(profile);
    }

    private void saveSettings(){
        if(profileSettings.size() > 0)
            sessionManager.setUserProfileSettings(profileSettings);
        try {
            saveSettingToDB();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveSettingToDB() throws JSONException {
        Gson gson = new Gson();
        String userProfileJson = gson.toJson(selectedProfile);
        JsonObjectRequest jsArrayRequest = new JsonObjectRequest(
                Request.Method.POST,
                WebUrl.SAVESETTINGURL,
                new JSONObject(userProfileJson),
                response -> Log.d("TAG", "Settings saved successfully"),
                error -> Log.e("TAG", "Failed to save settings: " + error.getMessage())
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

    private int getSelectedItemPosition(List<ComboBoxItem> comboBoxItems, String selectedValue) {
        for (int i = 0; i < comboBoxItems.size(); i++) {
            ComboBoxItem item = comboBoxItems.get(i);
            if (item.getValue().equals(selectedValue)) {
                return i;
            }
        }
        return 0;
    }

    private boolean[] convertStringToBooleanArray(String daysString) {
        boolean[] selectedItems = new boolean[dayItems.size()];
        if(!daysString.isEmpty()){
            String[] daysArray = daysString.split(", ");
            for (String day : daysArray) {
                for (int i = 0; i < dayItems.size(); i++) {
                    if (dayItems.get(i).getValue().equalsIgnoreCase(day)) {
                        selectedItems[i] = true;
                        break;
                    }
                }
            }
        }
        else{
            Arrays.fill(selectedItems, true);
        }
        return selectedItems;
    }

}