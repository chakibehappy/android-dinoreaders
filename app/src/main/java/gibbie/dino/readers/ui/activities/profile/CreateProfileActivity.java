package gibbie.dino.readers.ui.activities.profile;

import static gibbie.dino.readers.retrofitsetup.CommonResponse.isUnauthorized;
import static gibbie.dino.readers.retrofitsetup.WebUrl.CREATEPROFILEURL;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.Constant;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.commonclasses.GetAsyncPost;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.databinding.FragmentCreateProfileBinding;
import gibbie.dino.readers.interfaces.NoInternet;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class CreateProfileActivity extends AppCompatActivity implements NoInternet, SharedPreferences.OnSharedPreferenceChangeListener {
    SessionManager sessionManager;
    private FrameLayout flRoot;
    private File imgFile;
    LinearLayout ll_back;
    Button btnRegister;
    TextView tv_name, tv_age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_profile);
        getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_orange));

        sessionManager = new SessionManager(this);
        sessionManager.getSharedPreferencesInstance().registerOnSharedPreferenceChangeListener(this);

        ll_back = findViewById(R.id.ll_back);
        flRoot = findViewById(R.id.ll_root);
        btnRegister = findViewById(R.id.btn_register);
        tv_name = findViewById(R.id.tv_name);
        tv_age = findViewById(R.id.tv_age);

        ll_back.setOnClickListener(v -> this.finish());
        btnRegister.setOnClickListener( v -> createProfile(tv_name.getText().toString().trim()));

    }

    public void createProfile(String name) {
        uploadProfileImage(imgFile,name);
    }

    private void uploadProfileImage(File img_file,String name) {
        MultipartBody.Builder formBuilder = new MultipartBody.Builder();
        formBuilder.setType(MultipartBody.FORM);
        if(img_file != null) {
            final MediaType MEDIA_TYPE = img_file.getPath().endsWith("png") ? MediaType.parse("image/png") : MediaType.parse("image/jpeg");
            formBuilder.addFormDataPart("img_url", img_file.getName(), RequestBody.create(MEDIA_TYPE, img_file));
        }
        formBuilder.addFormDataPart("name", name);
        formBuilder.addFormDataPart("dob",tv_age.getText().toString().trim());

        if (Functions.checkInternet(this)) {
            Functions.showProgressbar(CreateProfileActivity.this);
            RequestBody formBody = formBuilder.build();
            GetAsyncPost mAsync = new GetAsyncPost(this, CREATEPROFILEURL, formBody, "") {
                @Override
                public void getValueParse(okhttp3.Response response) {

                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    String result = "";
                    Functions.hideProgressbar(CreateProfileActivity.this);
                    try {
                        if (response.isSuccessful()) {
                            parseResponse(response);
                        } else if (isUnauthorized(CreateProfileActivity.this, response.code())) {

                        } else if (response.code() == 404) {
//                            showSnackbar(flRoot, gibbie.dino.readers.commonclasses.Constant.DUPLICATEENTRYERROR, "");

                        } else if (response.code() == 500) {
//                            showSnackbar(flRoot, Constant.Server_ERROR, "");
                        } else {
//                            showSnackbar(flRoot, Constant.FAILED_TO_LOAD, "");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void retry() {
                    Functions.hideProgressbar(CreateProfileActivity.this);
                }
            };

            mAsync.execute();

        } else {
            Functions.hideProgressbar(CreateProfileActivity.this);
            Log.e("No Internet:", "Create Profile");
        }
    }

    private void parseResponse(okhttp3.Response response) {
        try {
            JSONObject jsonObject = null;
            String mResponse = null;
            try {
                mResponse = response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (mResponse != null && !mResponse.isEmpty()) {
                jsonObject = new JSONObject(mResponse);
                JSONObject jsonObject_data = jsonObject.optJSONObject("data");
                if (jsonObject_data.optString("img_url") != null) {
                    // Picasso.get().load(WebUrl.BASEURL + jsonObject_data.optString("img_url")).error(R.drawable.profile).placeholder(R.drawable.profile).into(binding.ivProfile);
                    sessionManager.setProfilePicturePath(jsonObject_data.optString("img_url"));
                    BottomNavigation.fm.popBackStack();
                }
            } else {
                if (jsonObject != null){
//                    showSnackbar(flRoot, jsonObject.optString("message"), "");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }

    @Override
    public void Retry() {

    }
}