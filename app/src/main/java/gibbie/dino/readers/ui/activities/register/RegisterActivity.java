package gibbie.dino.readers.ui.activities.register;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.commonclasses.SuperActivity;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.databinding.ActivityRegisterBinding;
import gibbie.dino.readers.interfaces.NoInternet;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;
import gibbie.dino.readers.ui.activities.login.LoginActivity;
import gibbie.dino.readers.ui.activities.setting.UserProfileSetting;

public class RegisterActivity extends SuperActivity implements RegisterView, NoInternet {
    private ActivityRegisterBinding binding;
    private RegisterPresenterImplementation registerPresenterImplementation;
    SessionManager sessionManager;
    LinearLayout ll;
    EditText et_confirm_password;
    EditText et_email;
    EditText et_password;

    List<UserProfileSetting> profileSettings;

    @Override
    protected int MyView() {
        return R.layout.activity_register;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        ll = binding.ll;
        et_confirm_password = binding.etConfirmPassword;
        et_email = binding.etEmail;
        et_password = binding.etPassword;
        hideShowToolbar(true);
        changeStatusBarColor(R.color.black);
        createObjects();
        View view = binding.getRoot();
        setContentView(view);
        binding.btnRegister.setOnClickListener(this::registerOnclick);
        binding.llBack.setOnClickListener(this::backOnclick);
    }

    private void createObjects() {
        sessionManager = new SessionManager(this);
        registerPresenterImplementation = new RegisterPresenterImplementation(this, this);
    }
    public void registerOnclick(View view) {
        //startNewActivity(LoginActivity.this, TemporaryHome.class, true, null);
        validateFields();

    }
    private void backOnclick(View view){
        super.onBackPressed();
    }
    @Override
    public void Retry() {
        validateFields();
    }

    @Override
    public void onSuccess(ArrayList<RegisterModel> model) {
        try {

            parseResponse(model);
        } catch (Exception e) {
            e.printStackTrace();
            showSnackbar(ll, getString(R.string.something_wrong), "");
        }
    }

    @Override
    public void onError(String error) {
        showSnackbar(ll, error, "");
    }

    @Override
    public void onNoInternet() {
        Functions.NoInternetcConnectionDialog(this, this);
    }

    private void saveDataInSharedPreferences(String accessToken) {
        sessionManager.setAccessToken("Bearer " + accessToken);
        sessionManager.SetLoggedin(true);

            sessionManager.setPass(et_password.getText().toString().trim());
            sessionManager.setEmail(et_email.getText().toString().trim());



    }

    private void saveExpiresAt(String expiresAt) {
        sessionManager.setExpiresAt(expiresAt);
    }

    // validations for fields
    private void validateFields() {
        if (!checkLoginFields(et_email, et_password)) {
            showSnackbar(ll, "Please Enter Email and Password.", "");
        } else if (!checkEditext(et_email)) {
            showSnackbar(ll, "Please Enter Email.", "");
        } else if (!checkEditext(et_password)) {
            showSnackbar(ll, "Please Enter Password.", "");
        } else {
            if (Functions.checkInternet(this)) {
                if (sessionManager.getAccesstoken().isEmpty()) {
                    requestRegister(et_email, et_password,et_confirm_password);
                } else {
                    // doLogout();

                }
            } else {
                Functions.NoInternetcConnectionDialog(this, this);
            }

        }
    }

    private void requestRegister( EditText et_email, EditText et_password,EditText et_confirm_password) {
        registerPresenterImplementation.registration(et_email.getText().toString().trim(), et_password.getText().toString().trim(),et_confirm_password.getText().toString().trim());
    }

    private void parseResponse(ArrayList<RegisterModel> model) throws Exception {
        if (model != null && model.size() > 0) {
            for (int i = 0; i < model.size(); i++) {
                String accessToken = model.get(i).getData().getAccessToken();
                String expiresAt = model.get(i).getData().getExpiresAt();
                saveDataInSharedPreferences(accessToken);
                saveExpiresAt(expiresAt);
                sessionManager.setUserId(model.get(i).getData().getUserId());
                sessionManager.setProfileId(model.get(i).getData().getProfile_id());

                sessionManager.setAccessToken("Bearer " + accessToken);
                sessionManager.setProfilePicturePath(WebUrl.BASEURL+model.get(i).getData().getProfile_image());
                sessionManager.setSettingPassword(model.get(i).getData().getSettingPassword());
            }
        }


        if(Functions.checkInternet(this)) {
            JSONObject request = new JSONObject();
            JsonObjectRequest jsArrayRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    WebUrl.PROFILESETTINGURL + sessionManager.getUserId(),
                    request,
                    response -> {
                        try {
                            if (response.getBoolean("success")) {
                                JSONArray dataArray = response.getJSONArray("data");
                                String dataJson = dataArray.toString();
                                Gson gson = new Gson();
                                Type userProfileSettingListType = new TypeToken<List<UserProfileSetting>>() {
                                }.getType();
                                profileSettings = gson.fromJson(dataJson, userProfileSettingListType);
                                if(profileSettings.size() > 0)
                                    sessionManager.setUserProfileSettings(profileSettings);
                                startNewActivity(RegisterActivity.this, BottomNavigation.class, true, null);
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