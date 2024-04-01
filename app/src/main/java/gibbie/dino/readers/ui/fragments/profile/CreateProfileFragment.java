package gibbie.dino.readers.ui.fragments.profile;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.Constant;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.commonclasses.GetAsyncPost;
import gibbie.dino.readers.commonclasses.SuperFragment;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.databinding.FragmentCreateProfileBinding;
import gibbie.dino.readers.interfaces.NoInternet;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static gibbie.dino.readers.retrofitsetup.CommonResponse.isUnauthorized;
import static gibbie.dino.readers.retrofitsetup.WebUrl.CREATEPROFILEURL;

public class CreateProfileFragment extends SuperFragment implements NoInternet, SharedPreferences.OnSharedPreferenceChangeListener, SwipeRefreshLayout.OnRefreshListener {
    SessionManager sessionManager;
    BottomNavigationView navBar;
    private FragmentCreateProfileBinding binding;
    private FrameLayout flRoot;
    private File imgFile;
    SwipeRefreshLayout swipe_to_refresh;
    private static TextView tv_age;
    //private int Age;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentCreateProfileBinding.inflate(getLayoutInflater());
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_orange));

        View view = binding.getRoot();
        createObject();
        navBar = getActivity().findViewById(R.id.nav_view);
        navBar.setVisibility(View.GONE);
        sessionManager.getSharedPreferencesInstance().registerOnSharedPreferenceChangeListener(this);
        binding.llDelete.setVisibility(View.GONE);

        binding.editName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView name = binding.tvName;
                String inputName =  name.getText().toString().trim();
                ToggleEditable(true);
                EditText et = (EditText)getActivity().findViewById(R.id.item_edit_text);
                et.setText(inputName);
            }
        });
        InitEditOnly();
        getUploadImageLocal();
        return view;
    }

    // Initialisation of objects
    private void createObject() {
        swipe_to_refresh = binding.swipeToRefresh;
        swipe_to_refresh.setOnRefreshListener(this);
        sessionManager = new SessionManager(getActivity());
        flRoot = binding.llRoot;
        binding.llBack.setOnClickListener(this::backOnclick);
        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createProfile(binding.tvName.getText().toString().trim());
            }
        });
        binding.ivProfile.setOnClickListener(this::profile_pic_click);
        tv_age = binding.tvAge;
        binding.llInputDob.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                MonthYearPicker(v,binding.tvAge);
                //showAgeDialog();
            }
        });

    }

    private void ToggleEditable(boolean isEditable){
       int option = isEditable? R.layout.item_tv_edit: R.layout.item_tv;
        View llInputName =  isEditable?getActivity().findViewById(R.id.item_tv_rl_input_name):getActivity().findViewById(R.id.rl_item_edit) ;

        ViewGroup parent = (ViewGroup) llInputName.getParent();
        int index = parent.indexOfChild(llInputName);
        parent.removeView(llInputName);
        llInputName = (RelativeLayout) getLayoutInflater().inflate(option, parent, false);
        parent.addView(llInputName, index);
        if(isEditable) {
            getActivity().findViewById(R.id.item_save).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EditText et = (EditText)getActivity().findViewById(R.id.item_edit_text);
                    String inputName =  et.getText().toString().trim();
                    ToggleEditable(false);
                    TextView name = getActivity().findViewById(R.id.item_tv_tv_name);
                    name.setText(inputName);
                    binding.tvName.setText(inputName);

                }
            });
        }else{
            getActivity().findViewById(R.id.item_tv_ll_input_name).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TextView name = getActivity().findViewById(R.id.item_tv_tv_name);
                    String inputName =  name.getText().toString().trim();
                    ToggleEditable(true);
                    EditText et = (EditText)getActivity().findViewById(R.id.item_edit_text);
                    et.setText(inputName);
                }
            });
        }
    }
    private void InitEditOnly(){
        View llInputName = binding.itemTvRlInputName ;

        ViewGroup parent = (ViewGroup) llInputName.getParent();
        int index = parent.indexOfChild(llInputName);
        parent.removeView(llInputName);
        llInputName = (RelativeLayout) getLayoutInflater().inflate( R.layout.item_tv_edit, parent, false);
        parent.addView(llInputName, index);
        ImageView save = (ImageView) llInputName.findViewById(R.id.item_save);
        save.setVisibility(View.INVISIBLE);
        EditText input = (EditText) llInputName.findViewById(R.id.item_edit_text);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!charSequence.equals("") ) {

                    binding.tvName.setText(charSequence.toString().trim());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }
    private void backOnclick(View view) {
        // navBar.setVisibility(View.VISIBLE);
        sessionManager.setCreateFilePicturePath("");
        BottomNavigation.fm.popBackStack();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    @Override
    public void Retry() {

    }

    private void getUploadImageLocal() {
        //Log.e("path", sessionManager.getCreateProfilePicturePath());
        if (sessionManager.getCreateProfilePicturePath() != "") {
            if (sessionManager.getCreateProfilePicturePath().contains("cache")) {
                imgFile = new File(sessionManager.getCreateProfilePicturePath());
                Picasso.get().load(imgFile).placeholder(R.drawable.profile).error(R.drawable.profile).into(binding.ivProfile);
            } else {
                Picasso.get().load(sessionManager.getCreateProfilePicturePath()).placeholder(R.drawable.profile).error(R.drawable.profile).into(binding.ivProfile);
            }
        }
        Functions.hideProgressbar(getActivity());
    }

    public void createProfile(String name) {

       uploadProfileImage(imgFile,name);
    }

    public void profile_pic_click(View view) {
        Intent intent = new Intent(getContext(), ProfilePictureActivity.class);
        startActivityForResult(intent, Constant.CREATEPROFILEIMAGECROPPER);
        // startActivity(intent);
    }

    // UPLOAD PROFILE PICTURE TO SERVER
    private void uploadProfileImage(File img_file,String name) {

        MultipartBody.Builder formBuilder = new MultipartBody.Builder();
        formBuilder.setType(MultipartBody.FORM);

        if(img_file != null) {
            final MediaType MEDIA_TYPE = img_file.getPath().endsWith("png") ? MediaType.parse("image/png") : MediaType.parse("image/jpeg");
            formBuilder.addFormDataPart("img_url", img_file.getName(), RequestBody.create(MEDIA_TYPE, img_file));
        }
        formBuilder.addFormDataPart("name", name);
        formBuilder.addFormDataPart("dob",binding.tvAge.getText().toString().trim());

        if (Functions.checkInternet(getContext())) {
            Functions.showProgressbar(getActivity());
            RequestBody formBody = formBuilder.build();
            GetAsyncPost mAsync = new GetAsyncPost(getContext(), CREATEPROFILEURL, formBody, "") {
                @Override
                public void getValueParse(okhttp3.Response response) {

                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    String result = "";
                    Functions.hideProgressbar(getActivity());
                    try {
                        if (response.isSuccessful()) {
                            parseResponse(response);
                        } else if (isUnauthorized(getActivity(), response.code())) {

                        } else if (response.code() == 404) {
                            showSnackbar(flRoot, gibbie.dino.readers.commonclasses.Constant.DUPLICATEENTRYERROR, "");

                        } else if (response.code() == 500) {
                            showSnackbar(flRoot, Constant.Server_ERROR, "");

                        } else {
                            showSnackbar(flRoot, Constant.FAILED_TO_LOAD, "");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showSnackbar(flRoot, e.getMessage(), "");
                    }
                }

                @Override
                public void retry() {
                    Functions.hideProgressbar(getActivity());
                }
            };

            mAsync.execute();

        } else {
            Functions.hideProgressbar(getActivity());
            Log.e("No Internet:", "Create Profile");
        }

    }

    // PARSE RESPONSE OF PROFILE PICTURE API
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
                showSnackbar(flRoot, jsonObject.optString("message"), "");
                if (jsonObject_data.optString("img_url") != null) {
                   // Picasso.get().load(WebUrl.BASEURL + jsonObject_data.optString("img_url")).error(R.drawable.profile).placeholder(R.drawable.profile).into(binding.ivProfile);
                    sessionManager.setProfilePicturePath(WebUrl.BASEURL + jsonObject_data.optString("img_url"));
                    BottomNavigation.fm.popBackStack();
                }
            } else {
                if (jsonObject != null)
                    showSnackbar(flRoot, jsonObject.optString("message"), "");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.CREATEPROFILEIMAGECROPPER) {
            if (resultCode == Activity.RESULT_OK) {
                getUploadImageLocal();
            }
        }
    }

    @Override
    public void onRefresh() {
        getUploadImageLocal();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipe_to_refresh.setRefreshing(false);
            }
        }, 1000);
    }

}
