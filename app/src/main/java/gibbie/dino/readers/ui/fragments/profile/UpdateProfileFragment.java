package gibbie.dino.readers.ui.fragments.profile;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonElement;
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
import gibbie.dino.readers.retrofitsetup.ServiceGenerator;
import gibbie.dino.readers.retrofitsetup.WebServices;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static gibbie.dino.readers.retrofitsetup.CommonResponse.isUnauthorized;
import static gibbie.dino.readers.retrofitsetup.WebUrl.UPDATEPROFILEURL;

public class UpdateProfileFragment extends SuperFragment implements NoInternet, SharedPreferences.OnSharedPreferenceChangeListener, SwipeRefreshLayout.OnRefreshListener,NumberPicker.OnValueChangeListener {
    SessionManager sessionManager;
    BottomNavigationView navBar;
    private FragmentCreateProfileBinding binding;
    private FrameLayout flRoot;
    private File imgFile;
    private String profile_id;
  //  private String Age;
    SwipeRefreshLayout swipe_to_refresh;
    private static TextView tv_age;
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
        binding.llDelete.setVisibility(View.VISIBLE);
        getProfileDetails();
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
        binding.tvTitle.setText(R.string.update_profile);
        binding.btnRegister.setText(R.string.update);
        tv_age = binding.tvAge;
        binding.llInputDob.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                MonthYearPicker(v,binding.tvAge);
            }
        });
        binding.llDelete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                deleteProfile();
            }
        });
        binding.editName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView name = binding.tvName;
                String inputName =  name.getText().toString().trim();
                ToggleEditable(view,true);
                EditText et = (EditText)getActivity().findViewById(R.id.item_edit_text);
                et.setText(inputName);
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
    }

    public void createProfile(String name) {
        uploadProfileImage(imgFile, name);
    }

    public void profile_pic_click(View view) {
        Intent intent = new Intent(getContext(), ProfilePictureActivity.class);
        startActivityForResult(intent, Constant.CREATEPROFILEIMAGECROPPER);
        // startActivity(intent);
    }

    // UPLOAD PROFILE PICTURE TO SERVER
    private void uploadProfileImage(File img_file, String name) {

        MultipartBody.Builder formBuilder = new MultipartBody.Builder();
        formBuilder.setType(MultipartBody.FORM);
        if(img_file != null) {
            final MediaType MEDIA_TYPE = img_file.getPath().endsWith("png") ? MediaType.parse("image/png") : MediaType.parse("image/jpeg");
            formBuilder.addFormDataPart("img_url", img_file.getName(), RequestBody.create(MEDIA_TYPE, img_file));
        }
        formBuilder.addFormDataPart("name", name);
        formBuilder.addFormDataPart("id", profile_id);
        formBuilder.addFormDataPart("dob",binding.tvAge.getText().toString().trim());
        if (Functions.checkInternet(getContext())) {
            Functions.showProgressbar(getActivity());
            RequestBody formBody = formBuilder.build();
            GetAsyncPost mAsync = new GetAsyncPost(getContext(), UPDATEPROFILEURL, formBody, "") {
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
    public void getProfileDetails(){
        if(getArguments() == null) {
            Functions.hideProgressbar(getActivity());
            return;
        }
        if(getArguments().containsKey(Constant.SELECTED_PROFILE_ID) ){
            profile_id =  getArguments().getString(Constant.SELECTED_PROFILE_ID);
            if(profile_id != ""){
                if (Functions.checkInternet(getContext())) {
                    Functions.showProgressbar(getActivity());
                    WebServices webServices = ServiceGenerator.createService(WebServices.class);
                    webServices.ProfileDetail(WebUrl.Accept, WebUrl.ContentType, sessionManager.getAccesstoken(),profile_id).enqueue(new Callback<ProfileDetailModel>() {
                        @Override
                        public void onResponse(Call<ProfileDetailModel> call, Response<ProfileDetailModel> response) {
                            Functions.hideProgressbar(getActivity());
                            try {

                                if (response.isSuccessful()) {
                                    try {
                                        parseResponse(response.body());

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else if (isUnauthorized(getActivity(), response.code())) {
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
                        public void onFailure(Call<ProfileDetailModel> call, Throwable t) {
                            t.getMessage();
                            Functions.hideProgressbar(getActivity());
                        }
                    });

                } else {
                    Functions.NoInternetcConnectionDialog(getContext(), this);
                    Functions.hideProgressbar(getActivity());
                }
            }


        }
    }
    private void parseResponse(ProfileDetailModel body) {
        try {
            sessionManager.setCreateFilePicturePath(body.getData().getImg_url());
            binding.tvName.setText(body.getData().getName());
            //Age = body.getData().getAge();
//            if(Age == 0) {
//                tv_age.setText(R.string.undisclosed);
//            }else
//                tv_age.setText(Age);
            tv_age.setText(body.getData().getDob());
            Picasso.get().load(body.getData().getImg_url()).into(binding.ivProfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void ToggleEditable(View v,boolean isEditable){
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
                    ToggleEditable(view, false);
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
                    ToggleEditable(view,true);
                    EditText et = (EditText)getActivity().findViewById(R.id.item_edit_text);
                    et.setText(inputName);
                }
            });
        }
    }
    public void deleteProfile(){
        if(getArguments() == null) {
            Functions.hideProgressbar(getActivity());
            return;
        }
        if(getArguments().containsKey(Constant.SELECTED_PROFILE_ID) ){
            profile_id =  getArguments().getString(Constant.SELECTED_PROFILE_ID);
            if(profile_id != ""){
                if (Functions.checkInternet(getContext())) {
                    Functions.showProgressbar(getActivity());
                    WebServices webServices = ServiceGenerator.createService(WebServices.class);
                    webServices.DeleteProfile(WebUrl.Accept, WebUrl.ContentType, sessionManager.getAccesstoken(),profile_id).enqueue(new Callback<JsonElement>() {
                        @Override
                        public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                            Functions.hideProgressbar(getActivity());
                            try {

                                if (response.isSuccessful()) {
                                    try {
                                        parseResponse(response.body());

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else if (isUnauthorized(getActivity(), response.code())) {
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
                            Functions.hideProgressbar(getActivity());
                        }
                    });

                } else {
                    Functions.NoInternetcConnectionDialog(getContext(), this);
                    Functions.hideProgressbar(getActivity());
                }
            }


        }
    }
    private void parseResponse(JsonElement body) {
        try {
            sessionManager.setCreateFilePicturePath("");
            BottomNavigation.fm.popBackStack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh() {
        getProfileDetails();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipe_to_refresh.setRefreshing(false);
            }
        }, 1000);
    }
    public void showAgeDialog()
    {

        final Dialog d = new Dialog(getContext());

        d.setTitle("Age Picker");
        d.setContentView(R.layout.dialog_number_picker);
        Button btn_set = (Button) d.findViewById(R.id.btn_set);
        Button btn_cancel = (Button) d.findViewById(R.id.btn_cancel);
        final NumberPicker agePicker = (NumberPicker) d.findViewById(R.id.np_main);
        agePicker.setMaxValue(100);
        agePicker.setMinValue(1);
//        agePicker.setValue(Age);
        agePicker.setWrapSelectorWheel(false);
        agePicker.setOnValueChangedListener(this);
        btn_set.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                tv_age.setText(agePicker.getValue());
//                Age = agePicker.getValue();
                d.dismiss();
            }
        });
        btn_cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
        Window window = d.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 20);
        window.setBackgroundDrawable(inset);
        wlp.gravity = Gravity.BOTTOM;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);


    }

    @Override
    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
//        Age = newVal;
//        tv_age.setText("Age:"+String.valueOf(newVal));
    }
}