package gibbie.dino.readers.ui.fragments.profile;

import static gibbie.dino.readers.retrofitsetup.CommonResponse.isUnauthorized;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.Constant;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.commonclasses.SuperFragment;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.databinding.FragmentManageProfileBinding;
import gibbie.dino.readers.interfaces.NoInternet;
import gibbie.dino.readers.retrofitsetup.ServiceGenerator;
import gibbie.dino.readers.retrofitsetup.WebServices;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;
import gibbie.dino.readers.ui.activities.login.LoginActivity;
import gibbie.dino.readers.ui.activities.logout.LogoutModel;
import gibbie.dino.readers.ui.activities.logout.LogoutPresenterImplementation;
import gibbie.dino.readers.ui.activities.logout.LogoutView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageProfileFragment extends SuperFragment implements NoInternet, SharedPreferences.OnSharedPreferenceChangeListener, LogoutView {
    SessionManager sessionManager;
    LogoutPresenterImplementation logoutPresenterImplementation;
    BottomNavigationView navBar;
    private FragmentManageProfileBinding binding;
    GridView androidGridView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentManageProfileBinding.inflate(getLayoutInflater());
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_orange));

        View view = binding.getRoot();
        createObject();
        navBar = getActivity().findViewById(R.id.nav_view);
        navBar.setVisibility(View.GONE);
        sessionManager.getSharedPreferencesInstance().registerOnSharedPreferenceChangeListener(this);

        getProfile();
        binding.llBack.setOnClickListener(this::backOnclick);
        return view;
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

    @Override
    public void onNoInternet() {
        Functions.NoInternetcConnectionDialog(getContext(), new NoInternet() {
            @Override
            public void Retry() {
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
        ProfileGridViewAdapter adapterViewAndroid = new ProfileGridViewAdapter(getContext(),body.getData(),true,true);
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
                }else{
                 //   Log.e("ProfleID:",body.getData().get(position).getId());
                    Fragment fragment = new UpdateProfileFragment();
                    Bundle b = new Bundle();
                    b.putString(Constant.SELECTED_PROFILE_ID,body.getData().get(position).getId());
                    fragment.setArguments(b);
                    sessionManager.setCreateFilePicturePath("");
                    BottomNavigation.fm
                            .beginTransaction()
                            .replace(R.id.nav_host_fragment, fragment)
                            .addToBackStack(null)
                            .commit();
                }

            }
        });
    }
}
