package gibbie.dino.readers.ui.activities.profile;

import static gibbie.dino.readers.retrofitsetup.CommonResponse.isUnauthorized;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonElement;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.customlayout.OutlineTextView;
import gibbie.dino.readers.customlayout.StylishButton;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.interfaces.NoInternet;
import gibbie.dino.readers.retrofitsetup.ServiceGenerator;
import gibbie.dino.readers.retrofitsetup.WebServices;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.login.LoginActivity;
import gibbie.dino.readers.ui.activities.logout.LogoutModel;
import gibbie.dino.readers.ui.activities.logout.LogoutPresenterImplementation;
import gibbie.dino.readers.ui.activities.logout.LogoutView;
import gibbie.dino.readers.ui.activities.readingbuddy.EditDinoBuddy;
import gibbie.dino.readers.ui.activities.setting.ApplicationSettingActivity;
import gibbie.dino.readers.ui.fragments.collection.CollectionListAdapter;
import gibbie.dino.readers.ui.fragments.profile.ProfilesModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity implements NoInternet, SharedPreferences.OnSharedPreferenceChangeListener, LogoutView {

    SessionManager sessionManager;
    LogoutPresenterImplementation logoutPresenterImplementation;

    ProfilesModel profileList;
    ImageView iv_profile;
    OutlineTextView tv_name;
    LinearLayout btn_dino_buddy, btn_back, btn_new_profile, btn_setting, btn_logout;
    ProfileGridViewAdapter profileGridViewAdapter;
    RecyclerView rv_avatar;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_brown));

        sessionManager = new SessionManager(this);
        logoutPresenterImplementation = new LogoutPresenterImplementation(ProfileActivity.this, this);

        sessionManager.getSharedPreferencesInstance().registerOnSharedPreferenceChangeListener(this);

        tv_name =  findViewById(R.id.tv_name);
        iv_profile = findViewById(R.id.iv_profile);
        rv_avatar = findViewById(R.id.rv_avatar);
        btn_back = findViewById(R.id.ll_back);
        btn_dino_buddy = findViewById(R.id.btn_dino_buddy);
        btn_new_profile = findViewById(R.id.btn_new_profile);
        btn_setting = findViewById(R.id.btn_setting);
        btn_logout = findViewById(R.id.btn_logout);

        Picasso.get().load(sessionManager.getProfilePicturePath()).placeholder(R.drawable.profile).error(R.drawable.profile).into(iv_profile);
        tv_name.setText(sessionManager.getCurrentUserProfile().getProfile().get(0).getName());

        btn_back.setOnClickListener(v -> this.finish());
        btn_dino_buddy.setOnClickListener(v -> goToEditDinoBuddy());
        btn_new_profile.setOnClickListener(v -> goToNewProfile());
        btn_setting.setOnClickListener(v -> toApplicationSettings());
        btn_logout.setOnClickListener(v -> hitLogoutApi());

        getProfile();
        getProfileInfo();
    }

    void goToEditDinoBuddy(){
        Intent i = new Intent(this, EditDinoBuddy.class);
        startActivity(i);
    }

    public void goToNewProfile(){
        Intent i = new Intent(this, CreateProfileActivity.class);
        startActivity(i);
    }

    public void toApplicationSettings(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialogTheme);
        builder.setTitle("Enter Password");
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_password, null);
        final EditText passwordInput = dialogView.findViewById(R.id.passwordInput);
        builder.setView(dialogView);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = passwordInput.getText().toString();
                if(password.equals(sessionManager.getSettingPassword())){
                    Intent i = new Intent(ProfileActivity.this, ApplicationSettingActivity.class);
                    startActivity(i);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    public void getProfileInfo(){

        if(Functions.checkInternet(ProfileActivity.this)){
            JSONObject request = new JSONObject();
            JsonObjectRequest jsArrayRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    WebUrl.GETPROFILEINFOURL + sessionManager.getProfileId(),
                    request,
                    response -> {
                        try {
                            OutlineTextView tv_points = findViewById(R.id.tv_points);
                            tv_points.setText(response.getString("totalPoints"));
                            OutlineTextView tv_reading_level = findViewById(R.id.tv_reading_level);
                            String level = "Lv" + response.getString("readingLevel");
                            tv_reading_level.setText(level);
                            OutlineTextView tv_books_read = findViewById(R.id.tv_books_read);
                            tv_books_read.setText(response.getString("bookCount"));

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

            RequestQueue mRequestQueue = Volley.newRequestQueue(ProfileActivity.this);
            String TAG_JSON = "json_obj_req";
            jsArrayRequest.setTag(TAG_JSON);
            jsArrayRequest.setRetryPolicy(new DefaultRetryPolicy(0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mRequestQueue.add(jsArrayRequest);
        }
    }

    public void getProfile() {
        if (Functions.checkInternet(this)) {
            Functions.showProgressbar(this);
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.MyProfiles(WebUrl.Accept, WebUrl.ContentType, sessionManager.getAccesstoken()).enqueue(new Callback<ProfilesModel>() {
                @Override
                public void onResponse(Call<ProfilesModel> call, Response<ProfilesModel> response) {
                    Functions.hideProgressbar(ProfileActivity.this);
                    try {

                        if (response.isSuccessful()) {
                            try {
                                profileList = response.body();
                                profileGridViewAdapter = new ProfileGridViewAdapter(ProfileActivity.this, profileList.getData());
                                ProfileGridViewAdapter.OnItemClickListener clickListener = new ProfileGridViewAdapter.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(int position) {
                                        requestSetProfile(position, profileList);
                                    }
                                };
                                profileGridViewAdapter.setOnItemClickListener(clickListener);
                                rv_avatar.setAdapter(profileGridViewAdapter);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (isUnauthorized(ProfileActivity.this, response.code())) {
                            Log.e("response", "Unauthorized");
                        } else if (response.code() == 404) {
                            // "Duplicate entry";
                        } else if (response.code() == 500) {
                            // "Server is busy at this time Please try again.";
                        } else {
                            // "Oops something went wrong!Please try again";
                        }
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }

                @Override
                public void onFailure(Call<ProfilesModel> call, Throwable t) {
                    t.getMessage();
                }
            });

        } else {
            Functions.NoInternetcConnectionDialog(this, this);
        }

    }

    private void requestSetProfile(int position, ProfilesModel body) {
        if (Functions.checkInternet(this)) {
            Functions.showProgressbar(ProfileActivity.this);
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.SetProfile(WebUrl.Accept, WebUrl.ContentType,sessionManager.getAccesstoken(), body.getData().get(position).getId()).enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    Functions.hideProgressbar(ProfileActivity.this);
                    try {

                        if (response.isSuccessful()) {
                            try {
                                sessionManager.setProfilePicturePath(body.getData().get(position).getImg_url());
                                sessionManager.setProfileId(body.getData().get(position).getId());
                                finish();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (isUnauthorized(ProfileActivity.this, response.code())) {
                            Log.e("response", "Unauthorized");
                        } else if (response.code() == 404) {
                            // "Duplicate entry";
                        } else if (response.code() == 500) {
                            // "Server is busy at this time Please try again.";
                        } else {
                            // "Oops something went wrong!Please try again";
                        }
                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    t.getMessage();
                }
            });

        } else {
            Functions.hideProgressbar(ProfileActivity.this);
            Log.e("No Internet:","OnFavourite");
        }
    }


    @Override
    public void Retry() {

    }

    public void logoutUser(View view) {
        hitLogoutApi();
    }
    private void hitLogoutApi() {
        if(sessionManager.getGoogleSignIn()){
//            googleSignOut();
        }else if(sessionManager.getFacebookSignIn()){
//            facebookLogout();
        }else {
            logoutPresenterImplementation.doLogout(WebUrl.Accept, sessionManager.getAccesstoken());
        }
    }


    @Override
    public void onSuccess(ArrayList<LogoutModel> list) {
        sessionManager.getSharedPreferencesInstance().unregisterOnSharedPreferenceChangeListener(this);
        sessionManager.logOut();
        this.finish();
        Intent i = new Intent(ProfileActivity.this, LoginActivity.class);
        // set the new task and clear flags
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    @Override
    public void onError(String error)  {

    }

    @Override
    public void onNoInternet() {

    }

    @Override
    public void onNoInternetLog() {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }
}