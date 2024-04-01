package gibbie.dino.readers.ui.fragments.home;

import static gibbie.dino.readers.retrofitsetup.CommonResponse.isUnauthorized;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.Constant;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.commonclasses.SuperFragment;
import gibbie.dino.readers.customlayout.OutlineTextView;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.databinding.FragmentHomeBinding;
import gibbie.dino.readers.interfaces.NoInternet;
import gibbie.dino.readers.retrofitsetup.ServiceGenerator;
import gibbie.dino.readers.retrofitsetup.WebServices;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;
import gibbie.dino.readers.ui.activities.guidedreading.GuidedReadingActivity;
import gibbie.dino.readers.ui.activities.guidedreading.SingleBookActivity;
import gibbie.dino.readers.ui.activities.placementtest.BookGradeSelectionActivity;
import gibbie.dino.readers.ui.activities.placementtest.PlacementTestActivity;
import gibbie.dino.readers.ui.activities.profile.ProfileActivity;
import gibbie.dino.readers.ui.activities.quiz.QuizActivity;
import gibbie.dino.readers.ui.activities.readingbuddy.PickAvatarBuddyActivity;
import gibbie.dino.readers.ui.activities.readingbuddy.ReadingBuddyActivity;
import gibbie.dino.readers.ui.activities.setting.ApplicationSettingActivity;
import gibbie.dino.readers.ui.activities.setting.ReadingTimeCounter;
import gibbie.dino.readers.ui.activities.setting.SettingAvatarListAdapter;
import gibbie.dino.readers.ui.activities.setting.UserProfileSetting;
import gibbie.dino.readers.ui.fragments.ownstory.OwnStoryBookListFragment;
import gibbie.dino.readers.ui.fragments.profile.ProfileFragment;
import gibbie.dino.readers.ui.fragments.readbook.ReadBookFragment;
import gibbie.dino.readers.ui.fragments.search.SearchFragment;
import gibbie.dino.readers.ui.fragments.singlebook.SingleBookFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends SuperFragment implements NoInternet, SharedPreferences.OnSharedPreferenceChangeListener, SwipeRefreshLayout.OnRefreshListener {
    List<String> menu_list;
    //ArrayList<BookCategoryModel> parentModelArrayList = new ArrayList<>();
    ArrayList<DashboardBookCategoryModel> dashboardParentModelArrayList = new ArrayList<>();
    SessionManager sessionManager;
    SwipeRefreshLayout swipe_to_refresh;
//    CarouselView customCarouselView;
    private FragmentHomeBinding binding;
    private RecyclerView parentRecyclerView;
    private boolean shouldRefresh = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_brown));
        binding = FragmentHomeBinding.inflate(getLayoutInflater());
        Functions.hideProgressbar(getActivity());
        View view = binding.getRoot();

        createObjects();
        getDashboard();
        getProfileInfo();

        getActivity().findViewById(R.id.nav_view).setVisibility(View.VISIBLE);

        return view;
    }
    public void toSearch(View view) {
        Fragment fragment = new SearchFragment();
        BottomNavigation.fm
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }
    public void toCreateOwnStory(View view) {
        if(sessionManager.canReadToday())
        {
            Fragment fragment = new OwnStoryBookListFragment();
            BottomNavigation.fm
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment)
                    .addToBackStack(null)
                    .commit();
        }
        else
        {
            ReadingTimeCounter.showReadingDayOutAlert(getContext());
        }
    }

    private void getProfileInfo() {
        if (sessionManager.getProfilePicturePath() != "") {
            ImageView ivProfile = (ImageView) binding.getRoot().findViewById(R.id.iv_profile);
            if(sessionManager.getProfilePicturePath().contains("cache")) {
                File imgFile = new  File(sessionManager.getProfilePicturePath());
                Picasso.get().load(imgFile).placeholder(R.drawable.profile).error(R.drawable.profile).into(ivProfile);
            }else {
                Picasso.get().load(sessionManager.getProfilePicturePath()).placeholder(R.drawable.profile).error(R.drawable.profile).into(ivProfile);
            }
            ivProfile.setOnClickListener(this::toProfile);
        }
        String profileName = sessionManager.getCurrentUserProfile().getProfile().get(0).getName();
        OutlineTextView tv_profile_name = (OutlineTextView) binding.getRoot().findViewById(R.id.tv_profile_name);
        tv_profile_name.setText(profileName);

        if(Functions.checkInternet(getContext())){
            JSONObject request = new JSONObject();
            JsonObjectRequest jsArrayRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    WebUrl.GETPROFILEINFOURL + sessionManager.getProfileId(),
                    request,
                    response -> {
                        try {
                            OutlineTextView tv_profile_point = (OutlineTextView) binding.getRoot().findViewById(R.id.tv_profile_point);
                            tv_profile_point.setText(response.getString("totalPoints"));
                            int readingPoints = Integer.parseInt(response.getString("totalPoints"));
                            sessionManager.setReadingPoints(readingPoints);
                            OutlineTextView tv_profile_level = (OutlineTextView) binding.getRoot().findViewById(R.id.tv_profile_level);
                            String level = "Lv" + response.getString("readingLevel");
                            tv_profile_level.setText(level);
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

            RequestQueue mRequestQueue = Volley.newRequestQueue(getContext());
            String TAG_JSON = "json_obj_req";
            jsArrayRequest.setTag(TAG_JSON);
            jsArrayRequest.setRetryPolicy(new DefaultRetryPolicy(0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mRequestQueue.add(jsArrayRequest);
        }
    }

    public void toProfile(View view) {
//        Fragment fragment = new ProfileFragment();
//        BottomNavigation.fm
//                .beginTransaction()
//                .replace(R.id.nav_host_fragment, fragment)
//                .addToBackStack(null)
//                .commit();
        Intent i = new Intent(getActivity(), ProfileActivity.class);
        shouldRefresh = true;
        startActivity(i);
    }

    private void createObjects() {
        sessionManager = new SessionManager(getActivity());
        swipe_to_refresh = binding.swipeToRefresh;
        swipe_to_refresh.setOnRefreshListener(this);
        menu_list = new ArrayList<>();
        menu_list.addAll(((BottomNavigation) getActivity()).menu_list);

//        customCarouselView = binding.carouselView;


        parentRecyclerView = binding.ParentRecyclerView;
        parentRecyclerView.setHasFixedSize(true);
//        SearchBarHandler();
    }
    public void getDashboard() {
        if (Functions.checkInternet(getContext())) {
            Functions.showProgressbar(getActivity());
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.Dashboard(WebUrl.Accept, WebUrl.ContentType, sessionManager.getAccesstoken()).enqueue(new Callback<DashboardModel>() {
                @Override
                public void onResponse(Call<DashboardModel> call, Response<DashboardModel> response) {
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
                public void onFailure(Call<DashboardModel> call, Throwable t) {
                    t.getMessage();
                    Functions.hideProgressbar(getActivity());
                }
            });

        } else {
            Functions.NoInternetcConnectionDialog(getContext(), this);
        }

    }

    private void parseResponse(DashboardModel body) {
        try {
            DashboardHorizontalRecycleViewAdapter dashboardAdapter = new DashboardHorizontalRecycleViewAdapter(dashboardParentModelArrayList, getContext());
            dashboardParentModelArrayList.clear();
            for (DashboardModel.Data data : body.getData()) {
                switch (data.getType()) {
                    case "slider":
                        dashboardParentModelArrayList.add(new DashboardBookCategoryModel(data.getName(), WebUrl.BASEURL + data.getTitle_icon(), data));
                        dashboardAdapter.addVerticalAddOnItemClickListener(new VerticalRecyclerViewAdapter.AddOnItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                GoToBookDetail(data.getContent().get(position));
                            }

                            @Override
                            public void onFavouriteClick(int position) {
                                requestFavourite(data.getContent().get(position));
                            }
                        });
                        break;
                    case "carousel":
                        CarouselView(data.getContent());
                        break;
                    default:
                        Log.e("Parse error:", "Dashboard get");
                        break;

                }
            }
            if (dashboardParentModelArrayList.size() > 0) {
                RecyclerView.LayoutManager parentLayoutManager = new LinearLayoutManager(getContext());
                parentRecyclerView.setLayoutManager(parentLayoutManager);
                parentRecyclerView.setAdapter(dashboardAdapter);
                dashboardAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get Latest Book API
//    public void getLatestBook() {
//        if (Functions.checkInternet(getContext())) {
//            Functions.showProgressbar(getActivity());
//            WebServices webServices = ServiceGenerator.createService(WebServices.class);
//            webServices.BookLatest(WebUrl.Accept, WebUrl.ContentType, sessionManager.getAccesstoken()).enqueue(new Callback<BookModel>() {
//                @Override
//                public void onResponse(Call<BookModel> call, Response<BookModel> response) {
//                    Functions.hideProgressbar(getActivity());
//                    try {
//
//                        if (response.isSuccessful()) {
//                            try {
//                                parseResponse(response.body());
//
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        } else if (isUnauthorized(getActivity(), response.code())) {
//                            Log.e("response", "Unauthorized");
//                        } else if (response.code() == 404) {
//                            // "Duplicate entry";
//                        } else if (response.code() == 500) {
//                            // "Server is busy at this time Please try again.";
//                        } else {
//                            // "Oops something went wrong!Please try again";
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<BookModel> call, Throwable t) {
//                    t.getMessage();
//                    Functions.hideProgressbar(getActivity());
//                    //t.getMessage();
//                }
//            });
//
//        } else {
//            Functions.NoInternetcConnectionDialog(getContext(), this);
//        }
//
//    }
//
//    private void parseResponse(BookModel body) {
//        try {
//            ArrayList<BookModel.Data> data = body.getData();
//            CarouselView(data);
//            // set ViewListener for custom view
//
//            //set the Categories for each array list set in the `ParentViewHolder`
//            parentModelArrayList.clear();
//            parentModelArrayList.add(new BookCategoryModel("Continue Last View", body));
//            parentModelArrayList.add(new BookCategoryModel("Recommended for you", body));
//            parentModelArrayList.add(new BookCategoryModel("Top Picks", body));
//
//            ParentAdapter = new HorizontalRecyclerViewAdapter(parentModelArrayList, getContext());
//            parentLayoutManager = new LinearLayoutManager(getContext());
//            parentRecyclerView.setLayoutManager(parentLayoutManager);
//            parentRecyclerView.setAdapter(ParentAdapter);
//            ParentAdapter.setVerticalAddOnItemClickListener(new VerticalRecyclerViewAdapter.AddOnItemClickListener() {
//                @Override
//                public void onItemClick(int position) {
//                    GoToBookDetail(body.getData().get(position));
//
//                }
//
//                @Override
//                public void onFavouriteClick(int position) {
//                    requestFavourite(body.getData().get(position));
//                }
//            });
//            ParentAdapter.notifyDataSetChanged();
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private void CarouselView(ArrayList<BookModel.Data> data) {
//        ViewListener viewListener = position -> {
//            View customView = getLayoutInflater().inflate(R.layout.child_carouselview_items, null);
//            //set view attributes here
//            TextView bookTitle = customView.findViewById(R.id.book_name);
//            TextView bookDescription = customView.findViewById(R.id.book_description);
//            ImageView bookCover = customView.findViewById(R.id.hero_image);
//            LinearLayout llRoot = customView.findViewById(R.id.ll_root);
//            BookModel.Data bookData = data.get(position);
//            Picasso.get().load(bookData.getBaseAndImage_url()).into(bookCover);
//            bookTitle.setText(bookData.getTitle());
//            bookDescription.setText(bookData.getDescription());
//
////            GridLayout glReadingLevel = customView.findViewById(R.id.gl_reading_level);
////            for(BookModel.Data.Categories cat : bookData.getLevel()) {
////                if(cat.getName() != null && cat.getColor() != null)
////                    glReadingLevel.addView(MakeCategory(getActivity(), cat.getName(), Color.parseColor(cat.getColor()),true));
////            }
//
//            if(bookData.getCategories() != null) {
//                GridLayout glCategories = customView.findViewById(R.id.gl_categories);
//                for (BookModel.Data.Categories cat : bookData.getCategories()) {
//                    if (cat.getName() != null && cat.getColor() != null)
//                        glCategories.addView(MakeCategory(getActivity(), cat.getName(), Color.parseColor(cat.getColor()), true));
//                }
//            }
//
//            llRoot.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    GoToBookDetail(bookData);
//                }
//            });
//            return customView;
//        };
//        customCarouselView.setViewListener(viewListener);
//        customCarouselView.setPageCount(data.size());
    }

    private void requestFavourite(BookModel.Data body) {
        if (Functions.checkInternet(getContext())) {
            Functions.showProgressbar(getActivity());
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.Favourite(WebUrl.Accept, WebUrl.ContentType,sessionManager.getAccesstoken(), body.getId()).enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    Functions.hideProgressbar(getActivity());
                    try {

                        if (response.isSuccessful()) {
                            try {
                                refreshScreen();
                              //  parseResponse(response.body());
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

    private void GoToBookDetail( BookModel.Data body) {
//        Fragment fragment = new SingleBookFragment();
//        BottomNavigation.fm
//                .beginTransaction()
//                .replace(R.id.nav_host_fragment, fragment, "BookDetail")
//                .addToBackStack(null)
//                .commit();
//        Bundle arguments = new Bundle();
//        arguments.putString(Constant.SELECTED_BOOK_ID, body.getId());
//        arguments.putBoolean("IsFavourite", body.getFavourite());
//        fragment.setArguments(arguments);


        Intent i = new Intent(getActivity(), SingleBookActivity.class);
        i.putExtra("IsFavourite", body.getFavourite());
        i.putExtra(Constant.SELECTED_BOOK_ID, body.getId());
        i.putExtra("defaultMenu", "Home");
        startActivity(i);
    }
//
//    void SearchBarHandler(){
//        EditText search = binding.etSearch;
//        //search.setImeActionLabel("Search", KeyEvent.KEYCODE_ENTER);
//        search.setOnKeyListener(new View.OnKeyListener() {
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                // If the event is a key-down event on the "enter" button
//                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
//                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
//                    // Perform action on key press
//                    toSearch(search.getText().toString());
//                    Toast.makeText(getContext(), search.getText(), Toast.LENGTH_SHORT).show();
//                    return true;
//                }
//                return false;
//            }
//        });
//    }
    public void toSearch(String query) {
        Fragment fragment = new SearchFragment();
        BottomNavigation.fm
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
        Bundle arguments = new Bundle();
        arguments.putString(Constant.COLLECTION_DETAILS_SEARCH_QUERY,query);
        fragment.setArguments(arguments);
        // Check if no view has focus:
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void Retry() {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {}

    @Override
    public void onRefresh() {
        refreshScreen();
    }

    private void refreshScreen() {
        //getLatestBook();
        getDashboard();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipe_to_refresh.setRefreshing(false);
            }
        }, 1000);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().findViewById(R.id.nav_view).setVisibility(View.VISIBLE);
        if(shouldRefresh){
            refreshScreen();
            getProfileInfo();
        }
    }
}