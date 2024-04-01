package gibbie.dino.readers.ui.activities.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.commonclasses.SuperActivity;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.databinding.ActivityLoginBinding;
import gibbie.dino.readers.interfaces.NoInternet;
import gibbie.dino.readers.retrofitsetup.ServiceGenerator;
import gibbie.dino.readers.retrofitsetup.WebServices;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;
import gibbie.dino.readers.ui.activities.logout.LogoutModel;
import gibbie.dino.readers.ui.activities.register.RegisterActivity;
import gibbie.dino.readers.ui.activities.setting.ApplicationSettingActivity;
import gibbie.dino.readers.ui.activities.setting.SettingAvatarListAdapter;
import gibbie.dino.readers.ui.activities.setting.UserProfileSetting;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.os.Debug.stopMethodTracing;
import static gibbie.dino.readers.retrofitsetup.CommonResponse.isUnauthorized;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends SuperActivity implements LoginView, NoInternet {
    Button bt_login;
    EditText et_username;
    EditText et_password;
    LinearLayout rl;
    CheckBox ch_remember_me;
    TextView txt_forgot;
    LoginPresenterImplementation loginPresenterImple;
    SessionManager sessionManager;
    private ActivityLoginBinding LoginActivityBinding;
    GoogleSignInClient mGoogleSignInClient;
    private static int RC_SIGN_IN = 100;
    CallbackManager callbackManager;
    private LoginButton loginButton;

    List<UserProfileSetting> profileSettings;

    @Override
    protected int MyView() {
        return R.layout.activity_login;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoginActivityBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        bt_login = LoginActivityBinding.loginBt;
        et_username = LoginActivityBinding.etUsername;
        et_password = LoginActivityBinding.etPassword;
        ch_remember_me = LoginActivityBinding.chRememberMe;
        rl = LoginActivityBinding.rl;
        hideShowToolbar(true);
        changeStatusBarColor(R.color.black);
        createObjects();
        checkLoginOrNot();
        checkIsRememberChecked();
        removeUnderlineOnType();
        View view = LoginActivityBinding.getRoot();

        LoginActivityBinding.loginBt.setOnClickListener(this::makeClick);
        LoginActivityBinding.llCheckbox.setOnClickListener(this::checkparentClick);
        LoginActivityBinding.txtRegister.setOnClickListener(this::openRegisterClick);

        // Request only the user's ID token, which can be used to identify the
        // user securely to your backend. This will contain the user's basic
        // profile (name, profile picture URL, etc) so you should not need to
        // make an additional call to personalize your application.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        LoginActivityBinding.googleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        loginButton = (LoginButton) findViewById(R.id.fb_login_button);
        callbackManager = CallbackManager.Factory.create();
        LoginActivityBinding.facebookLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginButton.performClick();
            }
        });

        loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code

                        AccessToken access_token = loginResult.getAccessToken();
                        boolean isLoggedIn = access_token != null && !access_token.isExpired();
                        if(isLoggedIn){
                            Log.e("FB AT:",access_token.getToken());
                            sessionManager.setGoogleSignIn(false);
                            sessionManager.setFacebookSignIn(true);
                            loginPresenterImple.doLoginFacebook(access_token.getToken());
                        }

                    }

                    @Override
                    public void onCancel() {
                        // App code
                        sessionManager.setFacebookSignIn(false);
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        sessionManager.setFacebookSignIn(false);
                    }
                });
        setContentView(view);
    }
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if(account != null){
                String access_token = account.getIdToken();
                sessionManager.setGoogleSignIn(true);
                loginPresenterImple.doLoginGoogle(access_token);
            }
            // Signed in successfully, show authenticated UI.
            //updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("GOOGLE_SIGN_IN", "signInResult:failed code=" + e.getStatusCode());
            //updateUI(null);
        }
    }
    private void removeUnderlineOnType() {
        et_username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                for (UnderlineSpan span : s.getSpans(0, s.length(), UnderlineSpan.class)) {
                    s.removeSpan(span);
                }
            }
        });
    }

    private void checkIsRememberChecked() {
        if (sessionManager.getIsRememberme()) {
            et_username.setText(sessionManager.getEmail());
            et_password.requestFocus();
            ch_remember_me.setChecked(true);
        } else {
            ch_remember_me.setChecked(false);
        }
    }

    private void rememberMeFunctionality() {
        ch_remember_me.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked()) {
                    ch_remember_me.setChecked(true);
                    sessionManager.setRememberme(true);

                } else {
                    ch_remember_me.setChecked(false);
                    sessionManager.setRememberme(false);
                    sessionManager.setEmail("");

                }
            }
        });
    }

    private void checkLoginOrNot() {
        if (sessionManager.IsLoggedIn()) {
           startNewActivity(LoginActivity.this, BottomNavigation.class, true, null);
        } else {
            rememberMeFunctionality();
        }
    }

    private void createObjects() {
        sessionManager = new SessionManager(this);
        loginPresenterImple = new LoginPresenterImplementation(this, this);
        profileSettings = new ArrayList<>();
    }

    public void makeClick(View view) {
        //startNewActivity(LoginActivity.this, TemporaryHome.class, true, null);
        validateFields();

    }

    public void checkparentClick(View view) {
        ch_remember_me.setChecked(!ch_remember_me.isChecked());
    }
    public void openRegisterClick(View view) {
        startNewActivity(LoginActivity.this, RegisterActivity.class, false, null);
    }

    // validations for fields
    private void validateFields() {
        if (!checkLoginFields(et_username, et_password)) {
            showSnackbar(rl, "Please Enter username and Password.", "");
        } else if (!checkEditext(et_username)) {
            showSnackbar(rl, "Please Enter username.", "");
        } else if (!checkEditext(et_password)) {
            showSnackbar(rl, "Please Enter Password.", "");
        } else {
            if (Functions.checkInternet(this)) {
                if (sessionManager.getAccesstoken().isEmpty()) {
                    requestLogin(et_username, et_password);
                } else {
                  //  doLogout();

                }
            } else {
                Functions.NoInternetcConnectionDialog(this, this);
            }

        }
    }
    public void doLogout() {
        if (Functions.checkInternet(this))
            Functions.showProgressbar(this);
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.makeLogout(WebUrl.Accept, sessionManager.getAccesstoken()).enqueue(new Callback<LogoutModel>() {
            @Override
            public void onResponse(Call<LogoutModel> call, Response<LogoutModel> response) {
                Functions.hideProgressbar(LoginActivity.this);

                if (response.body().getMessage().equalsIgnoreCase("Unauthenticated")) {
                    sessionManager.setAccessToken("");
                    sessionManager.logOut();
                    requestLogin(et_username, et_password);
                } else if (isUnauthorized(LoginActivity.this, response.code())) {
                    Log.e("response", "Unauthorized" + (response.code()));
                } else if (!response.isSuccessful()) {
                    showDialog(getString(R.string.server_wrong));
                    validateFields();
                } else {
                    // SUCCESS CASE
                    sessionManager.setAccessToken("");
                    sessionManager.logOut();
                    requestLogin(et_username, et_password);
                }
            }

            @Override
            public void onFailure(Call<LogoutModel> call, Throwable t) {
                Functions.hideProgressbar(LoginActivity.this);
                showDialog(getResources().getString(R.string.something_wrong));
                validateFields();
            }
        });
    }
    private void requestLogin(EditText et_username, EditText et_password) {
        loginPresenterImple.doLogin(et_username.getText().toString().trim(), et_password.getText().toString().trim());
    }

    @Override
    public void onSuccess(ArrayList<LoginModel> model) {
        try {

            parseResponse(model);
        } catch (Exception e) {
            e.printStackTrace();
            showSnackbar(rl, getString(R.string.something_wrong), "");
        }
    }


    private void saveDataInSharedPreferences(LoginModel loginModel) {
        String accessToken = loginModel.getData().getAccessToken();
        sessionManager.setAccessToken("Bearer " + accessToken);
        sessionManager.SetLoggedin(true);
        sessionManager.setUserId(loginModel.getData().getUserId());

        if (sessionManager.getIsRememberme()) {
            sessionManager.setPass(et_password.getText().toString().trim());
            sessionManager.setEmail(et_username.getText().toString().trim());

        } else {
            sessionManager.setPass("");
        }

    }

    private void saveExpiresAt(String expiresAt) {
        sessionManager.setExpiresAt(expiresAt);
    }


    @Override
    public void onError(String error) {
        showSnackbar(rl, error, "");
    }

    @Override
    public void onNoInternet() {
        Functions.NoInternetcConnectionDialog(this, this);
    }

    @Override
    public void Retry() {
        validateFields();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMethodTracing();
    }


    private void parseResponse(ArrayList<LoginModel> model) throws Exception {
        if (model != null && model.size() > 0) {
            for (int i = 0; i < model.size(); i++) {
                String expiresAt = model.get(i).getData().getExpiresAt();
                saveDataInSharedPreferences(model.get(i));
                saveExpiresAt(expiresAt);
              //  Log.e("path",model.get(i).getData().getProfile_image());
               // Log.e("ac:",accessToken);

                sessionManager.setProfilePicturePath(model.get(i).getData().getProfile_image());
                sessionManager.setProfileId(model.get(i).getData().getProfile_id());
                sessionManager.setSettingPassword(model.get(i).getData().getSettingPassword());
            }
        }
        sessionManager.setPass(et_password.getText().toString());
        sessionManager.setEmail(et_username.getText().toString());

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
                                startNewActivity(LoginActivity.this, BottomNavigation.class, true, null);
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