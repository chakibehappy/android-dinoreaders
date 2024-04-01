package gibbie.dino.readers.ui.fragments.profile;

import static gibbie.dino.readers.retrofitsetup.CommonResponse.isUnauthorized;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.commonclasses.SuperFragment;
import gibbie.dino.readers.customlayout.OutlineTextView;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.databinding.FragmentProfileBinding;
import gibbie.dino.readers.interfaces.NoInternet;
import gibbie.dino.readers.retrofitsetup.ServiceGenerator;
import gibbie.dino.readers.retrofitsetup.WebServices;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;
import gibbie.dino.readers.ui.activities.login.LoginActivity;
import gibbie.dino.readers.ui.activities.logout.LogoutModel;
import gibbie.dino.readers.ui.activities.logout.LogoutPresenterImplementation;
import gibbie.dino.readers.ui.activities.logout.LogoutView;
import gibbie.dino.readers.ui.activities.readingbuddy.EditDinoBuddy;
import gibbie.dino.readers.ui.activities.readingbuddy.ReadingBuddyActivity;
import gibbie.dino.readers.ui.activities.setting.ApplicationSettingActivity;
import gibbie.dino.readers.ui.fragments.home.HomeFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends SuperFragment implements NoInternet, SharedPreferences.OnSharedPreferenceChangeListener, LogoutView {
    SessionManager sessionManager;
    LogoutPresenterImplementation logoutPresenterImplementation;
    BottomNavigationView navBar;
    private FragmentProfileBinding binding;
    GridView androidGridView;
    GoogleSignInClient mGoogleSignInClient;
    AccessTokenTracker accessTokenTracker;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentProfileBinding.inflate(getLayoutInflater());
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_orange));

        View view = binding.getRoot();
        createObject();
        navBar = getActivity().findViewById(R.id.nav_view);
        navBar.setVisibility(View.GONE);
        sessionManager.getSharedPreferencesInstance().registerOnSharedPreferenceChangeListener(this);

        getProfile();
        binding.tvSettings.setOnClickListener(this::toApplicationSettings);
        binding.tvLogout.setOnClickListener(this::logoutUser);
        binding.llBack.setOnClickListener(this::backOnclick);
        binding.llManageProfile.setOnClickListener(this::toManageProfile);

        getActivity().getOnBackPressedDispatcher().addCallback(new  OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navBar.setVisibility(View.VISIBLE);
                BottomNavigation.fm.popBackStack();
            }
        });
        FacebookSdk.sdkInitialize(getContext());

        googleInit();
        return view;
    }

    private void facebookLogout() {
        sessionManager.setGoogleSignIn(false);
        sessionManager.setFacebookSignIn(false);
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if(currentAccessToken == null){
                    LoginManager.getInstance().logOut();
                }
            }
        };

        logoutPresenterImplementation.doLogout(WebUrl.Accept, sessionManager.getAccesstoken());
    }

    void googleInit() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);
    }
    void googleSignOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                sessionManager.setGoogleSignIn(false);
                logoutPresenterImplementation.doLogout(WebUrl.Accept, sessionManager.getAccesstoken());
            }
        });

    }
    public void toApplicationSettings(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomAlertDialogTheme);
        builder.setTitle("Enter Password");
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_password, null);
        final EditText passwordInput = dialogView.findViewById(R.id.passwordInput);
        builder.setView(dialogView);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = passwordInput.getText().toString();
                if(password.equals(sessionManager.getSettingPassword())){
                    Intent i = new Intent(getActivity(), ApplicationSettingActivity.class);
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

    public void toManageProfile(View view) {
        Fragment fragment = new ManageProfileFragment();
        BottomNavigation.fm
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }
    // Initialisation of objects
    private void createObject() {
        sessionManager = new SessionManager(getActivity());
        logoutPresenterImplementation = new LogoutPresenterImplementation(getActivity(), this);
    }
    private void backOnclick(View view){
        navBar.setVisibility(View.VISIBLE);
        BottomNavigation.fm.popBackStack();
    }
    public void logoutUser(View view) {
        hitLogoutApi();
    }
    private void hitLogoutApi() {
        if(sessionManager.getGoogleSignIn()){
            googleSignOut();
        }else if(sessionManager.getFacebookSignIn()){
         facebookLogout();
        }else {
            logoutPresenterImplementation.doLogout(WebUrl.Accept, sessionManager.getAccesstoken());
        }
    }
    @Override
    public void onNoInternet() {
        Functions.NoInternetcConnectionDialog(getContext(), new NoInternet() {
            @Override
            public void Retry() {
                hitLogoutApi();
            }
        });
    }

    @Override
    public void onNoInternetLog() {
        sessionManager.setInternetCheck(true);
        sessionManager.getSharedPreferencesInstance().unregisterOnSharedPreferenceChangeListener(this);
        sessionManager.logOut();
        startNewActivity(getActivity(), LoginActivity.class, true, null);
    }

    @Override
    public void Retry() {

    }
    @Override
    public void onSuccess(ArrayList<LogoutModel> list) {
        sessionManager.getSharedPreferencesInstance().unregisterOnSharedPreferenceChangeListener(this);
        sessionManager.logOut();
        startNewActivity(getActivity(), LoginActivity.class, true, null);
    }

    @Override
    public void onError(String error)  {
        MakeToast(error);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
    public void getProfile() {
        if (Functions.checkInternet(getContext())) {
            Functions.showProgressbar(getActivity());
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.MyProfiles(WebUrl.Accept, WebUrl.ContentType, sessionManager.getAccesstoken()).enqueue(new Callback<ProfilesModel>() {
                @Override
                public void onResponse(Call<ProfilesModel> call, Response<ProfilesModel> response) {
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
                public void onFailure(Call<ProfilesModel> call, Throwable t) {
                    t.getMessage();
                }
            });

        } else {
            Functions.NoInternetcConnectionDialog(getContext(), this);
        }
    }
    private void parseResponse(ProfilesModel body) {
        try {
            //bookData = body.getData();
            setAdapters(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setAdapters(ProfilesModel body) {
        ProfileModel createProfile  = new ProfileModel();
        createProfile.setName("");
        createProfile.setLocal_image(R.drawable.plusprofile);
        body.getData().add(createProfile);
        ProfileGridViewAdapter adapterViewAndroid = new ProfileGridViewAdapter(getContext(),body.getData(),false,true);
        androidGridView= binding.gridView;
        androidGridView.setAdapter(adapterViewAndroid);
        adapterViewAndroid.setAddOnItemClickListener(new ProfileGridViewAdapter.AddOnItemClickListener(){
            @Override
            public void onItemClick(int position) {
                if(position == adapterViewAndroid.getCount()-1){
                    Fragment fragment = new CreateProfileFragment();
                    sessionManager.setCreateFilePicturePath("");
                    BottomNavigation.fm
                            .beginTransaction()
                            .replace(R.id.nav_host_fragment, fragment)
                            .addToBackStack(null)
                            .commit();
                }else {
                    requestSetProfile(position,body);
                }

            }
        });
    }
    private void requestSetProfile(int position, ProfilesModel body) {
        if (Functions.checkInternet(getContext())) {
            Functions.showProgressbar(getActivity());
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.SetProfile(WebUrl.Accept, WebUrl.ContentType,sessionManager.getAccesstoken(), body.getData().get(position).getId()).enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    Functions.hideProgressbar(getActivity());
                    try {

                        if (response.isSuccessful()) {
                            try {
                                parseResponse(body, position);
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
                }
            });

        } else {
            Functions.hideProgressbar(getActivity());
            Log.e("No Internet:","OnFavourite");
        }
    }

    private void parseResponse(ProfilesModel body, int position) {
        Fragment fragment = new HomeFragment();
        sessionManager.setProfilePicturePath(body.getData().get(position).getImg_url());
        sessionManager.setProfileId(body.getData().get(position).getId());
        navBar.setVisibility(View.VISIBLE);
        BottomNavigation.fm
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }


//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        if(sessionManager.getFacebookSignIn()) {
//            accessTokenTracker.stopTracking();
//        }
//    }
}