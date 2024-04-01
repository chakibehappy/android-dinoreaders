package gibbie.dino.readers.ui.activities.bottomnav;

import static gibbie.dino.readers.retrofitsetup.CommonResponse.isUnauthorized;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.Constant;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.commonclasses.SuperActivity;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.databinding.ActivityBottomNavigationBinding;
import gibbie.dino.readers.interfaces.NoInternet;
import gibbie.dino.readers.retrofitsetup.ServiceGenerator;
import gibbie.dino.readers.retrofitsetup.WebServices;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.logout.LogoutPresenterImplementation;
import gibbie.dino.readers.ui.activities.placementtest.PlacementTestActivity;
import gibbie.dino.readers.ui.activities.setting.ReadingTimeCounter;
import gibbie.dino.readers.ui.fragments.classes.ClassesHomeFragment;
import gibbie.dino.readers.ui.fragments.collection.CollectionHomeFragment;
import gibbie.dino.readers.ui.fragments.home.HomeFragment;
import gibbie.dino.readers.ui.fragments.library.LibraryFragment;
import gibbie.dino.readers.ui.fragments.library.LibrarySliderFragment;
import gibbie.dino.readers.ui.fragments.ownstory.OwnStoryBookListFragment;
import gibbie.dino.readers.ui.fragments.ownstory.OwnStoryFragment;
import gibbie.dino.readers.ui.fragments.profile.ManageProfileFragment;
import gibbie.dino.readers.ui.fragments.profile.ProfilesModel;
import gibbie.dino.readers.ui.fragments.search.NewSearchFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BottomNavigation extends SuperActivity implements BottomNavigationView.OnNavigationItemSelectedListener, NoInternet, SharedPreferences.OnSharedPreferenceChangeListener {
    private ActivityBottomNavigationBinding binding;
    BottomNavigationView bottomNavigationView;
    int selectedMenuId = 0;
    public static final String NAV_HOME = "Home";
    public static final String NAV_LIBRARY = "Your Library";
    public static final String NAV_CREATE = "Create";
    public static final String NAV_SEARCH = "Search";
    public ArrayList<String> menu_list = new ArrayList<>();
    public static FragmentManager fm;
    LogoutPresenterImplementation logoutPresenterImplementation;
    SessionManager sessionManager;
    private AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_brown));
       // requestWindowFeature(Window.FEATURE_NO_TITLE);
       // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        createObject();
//        getProfile();

        makeFunctionality();

//        AdmobInit();

//        if(!sessionManager.isHavingReadingLevel()){
//            Intent i = new Intent(BottomNavigation.this, PlacementTestActivity.class);
//            this.finish();
//            startActivity(i);
//        }
    }

    private void AdmobInit() {
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
//        mAdView = findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(adRequest);
    }

    @Override
    protected int MyView()  {
        return R.layout.activity_bottom_navigation;
    }
    // Initialisation of objects
    private void createObject() {
        sessionManager = new SessionManager(this);
        sessionManager.getSharedPreferencesInstance().registerOnSharedPreferenceChangeListener(this);
        binding = ActivityBottomNavigationBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        fm = getSupportFragmentManager();
        setContentView(view);

        bottomNavigationView = binding.navView;
    }

    private void makeFunctionality() {
        bottomNavigationView.setItemIconTintList(null);
        //bottomNavigationView.setOnNavigationItemSelectedListener(this);
        bottomNavigationView.setOnItemSelectedListener(this::onNavigationItemSelected);
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        selectedMenuId = R.id.navigation_home;

        menu_list.clear();
        menu_list.add(NAV_HOME);
        menu_list.add(NAV_LIBRARY);
        menu_list.add(NAV_CREATE);
        menu_list.add(NAV_SEARCH);

        bottomNavigationView.getMenu().add(0, R.id.navigation_home, 0, "").setIcon(R.drawable.home_menu_new_ui);
        bottomNavigationView.getMenu().add(1, R.id.navigation_library, 1, "").setIcon(R.drawable.library_menu_new);
        bottomNavigationView.getMenu().add(2, R.id.navigation_collection, 2, "").setIcon(R.drawable.create_menu_new_ui);
        bottomNavigationView.getMenu().add(3, R.id.navigation_class, 3, "").setIcon(R.drawable.search_menu_new_ui);
        // LOAD HOME FRAGMENT
        String defaultMenu = getIntent().getStringExtra("defaultMenu");
        if(defaultMenu == null)
            defaultMenu = NAV_HOME;
        loadmenuFragments(defaultMenu);
    }

    public void loadmenuFragments(String name) {
        switch (name) {
            case NAV_LIBRARY:
                bottomNavigationView.setSelectedItemId(R.id.navigation_library);
                break;
            case NAV_CREATE:
                bottomNavigationView.setSelectedItemId(R.id.navigation_collection);
                break;
            case NAV_HOME:
                bottomNavigationView.setSelectedItemId(R.id.navigation_home);
                break;
            case NAV_SEARCH:
                bottomNavigationView.setSelectedItemId(R.id.navigation_class);
                break;

        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Fragment fragment = null;
//        defaultIcons();
        selectedMenuId = menuItem.getItemId();
        switch (menuItem.getItemId()) {
            case R.id.navigation_collection:
//                fragment = new CollectionHomeFragment();
                fragment = new OwnStoryBookListFragment();
                menuItem.setIcon(R.drawable.create_menu_new_ui);
                break;
            case R.id.navigation_library:
//                fragment = new LibrarySliderFragment();
                fragment = new LibraryFragment();
                menuItem.setIcon(R.drawable.library_menu_new);
                break;
            case R.id.navigation_home:
                fragment = new HomeFragment();
                menuItem.setIcon(R.drawable.home_menu_new_ui);
                break;
            case R.id.navigation_class:
//                fragment = new ClassesHomeFragment();
                fragment = new NewSearchFragment();
                menuItem.setIcon(R.drawable.search_menu_new_ui);
                break;

        }

        return loadDefaultFragment(fragment);
    }
    public boolean loadDefaultFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    private void defaultIcons() {

        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            switch (bottomNavigationView.getMenu().getItem(i).getItemId()) {
                case R.id.navigation_collection:
                    bottomNavigationView.getMenu().getItem(i).setIcon(R.drawable.collection_nonselected);
                    break;
                case R.id.navigation_library:
                    bottomNavigationView.getMenu().getItem(i).setIcon(R.drawable.library_line);
                    break;

                case R.id.navigation_home:
                    bottomNavigationView.getMenu().getItem(i).setIcon(R.drawable.home_line);
                    break;
                case R.id.navigation_class:
                    bottomNavigationView.getMenu().getItem(i).setIcon(R.drawable.collection_nonselected);
                    break;

            }
        }
    }

    @Override
    public void Retry() {

    }



    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
    public void getProfile() {
        if (Functions.checkInternet(this)) {
            Functions.showProgressbar(this);
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.MyProfiles(WebUrl.Accept, WebUrl.ContentType, sessionManager.getAccesstoken()).enqueue(new Callback<ProfilesModel>() {
                @Override
                public void onResponse(Call<ProfilesModel> call, Response<ProfilesModel> response) {
                    Functions.hideProgressbar(BottomNavigation.this);
                    try {
                        if (response.isSuccessful()) {
                            try {
                                parseResponse(response.body());

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (isUnauthorized(BottomNavigation.this, response.code())) {
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
    private void parseResponse(ProfilesModel body) {
        try {
            if(body.getData().size() >= 1) {

            }else {
                toManageProfile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void toManageProfile() {
        Fragment fragment = new ManageProfileFragment();
        fm
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }
}