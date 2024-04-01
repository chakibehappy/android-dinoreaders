package gibbie.dino.readers.ui.fragments.collection;

import static gibbie.dino.readers.retrofitsetup.CommonResponse.isUnauthorized;

import android.content.Context;
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
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonElement;

import org.json.JSONObject;

import java.util.ArrayList;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.Constant;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.commonclasses.SuperFragment;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.databinding.FragmentCollectionHomeBinding;
import gibbie.dino.readers.interfaces.NoInternet;
import gibbie.dino.readers.retrofitsetup.ServiceGenerator;
import gibbie.dino.readers.retrofitsetup.WebServices;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;
import gibbie.dino.readers.ui.fragments.home.BookModel;
import gibbie.dino.readers.ui.fragments.home.DashboardBookCategoryModel;
import gibbie.dino.readers.ui.fragments.home.DashboardHorizontalRecycleViewAdapter;
import gibbie.dino.readers.ui.fragments.home.DashboardModel;
import gibbie.dino.readers.ui.fragments.home.VerticalRecyclerViewAdapter;
import gibbie.dino.readers.ui.fragments.profile.ProfileGridViewAdapter;
import gibbie.dino.readers.ui.fragments.profile.ProfileModel;
import gibbie.dino.readers.ui.fragments.search.SearchFragment;
import io.github.muddz.styleabletoast.StyleableToast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CollectionHomeFragment extends SuperFragment implements NoInternet, SharedPreferences.OnSharedPreferenceChangeListener, SwipeRefreshLayout.OnRefreshListener{

    ArrayList<DashboardBookCategoryModel> dashboardParentModelArrayList = new ArrayList<>();
    SessionManager sessionManager;
    SwipeRefreshLayout swipe_to_refresh;
    private FragmentCollectionHomeBinding binding;
    private RecyclerView parentRecyclerView;
    private RecyclerView.LayoutManager parentLayoutManager;
    private DashboardHorizontalRecycleViewAdapter DashboardAdapter;

    GridView gridViewCategory;
    ArrayList<ProfileModel> tempArrayForCategory;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_brown));
        binding = FragmentCollectionHomeBinding.inflate(getLayoutInflater());
        Functions.hideProgressbar(getActivity());
        View view = binding.getRoot();

        createObjects();
        getDashboard(null);

        return view;
    }
    private void createObjects() {
        sessionManager = new SessionManager(getActivity());
        swipe_to_refresh = binding.swipeToRefresh;
        swipe_to_refresh.setOnRefreshListener(this);

        tempArrayForCategory = new ArrayList<>();
        parentRecyclerView = binding.ParentRecyclerView;
        parentRecyclerView.setHasFixedSize(true);
        SearchBarHandler();

    }
    public void getDashboard(String category_selection) {

        if (Functions.checkInternet(getContext())) {
            Functions.showProgressbar(getActivity());
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.CollectionDashboard(WebUrl.Accept, WebUrl.ContentType,sessionManager.getAccesstoken(),category_selection).enqueue(new Callback<DashboardModel>() {
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
        tempArrayForCategory.clear();
    }

    private void parseResponse(DashboardModel body) {
        try {
            DashboardAdapter = new DashboardHorizontalRecycleViewAdapter(dashboardParentModelArrayList, getContext());
            //set the Categories for each array list set in the `ParentViewHolder`
            dashboardParentModelArrayList.clear();
            for (DashboardModel.Data data : body.getData()) {

                switch (data.getType()) {
                    case "slider":
                        dashboardParentModelArrayList.add(new DashboardBookCategoryModel(data.getName(), WebUrl.BASEURL + data.getTitle_icon(), data));
                        DashboardAdapter.addVerticalAddOnItemClickListener(new VerticalRecyclerViewAdapter.AddOnItemClickListener() {
                            @Override
                            public void onItemClick(int position) {
                                GoToCollectionDetail(data.getContent().get(position));
                            }

                            @Override
                            public void onFavouriteClick(int position) {
//                                Log.e("Book:", String.valueOf(position)+ "=" + data.getContent().get(position).getTitle() + "::" + data.getContent().get(position).getId());
                                requestCollectionFavourite(data.getContent().get(position));
                            }
                        });
                        break;
                    case "carousel":
                        break;
                    case "categories_pick":
                        InitCategory(data.getContent());
                        break;
                    default:
                        Log.e("Parse error:", "Dashboard get");
                        break;

                }
            }
            if (dashboardParentModelArrayList.size() > 0) {

                parentLayoutManager = new LinearLayoutManager(getContext());
                parentRecyclerView.setLayoutManager(parentLayoutManager);
                parentRecyclerView.setAdapter(DashboardAdapter);
                DashboardAdapter.notifyDataSetChanged();
            }
            if(tempArrayForCategory.size() > 0){
                SetCategoryAdapted();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void requestCollectionFavourite(BookModel.Data body) {
        if (Functions.checkInternet(getContext())) {
            Functions.showProgressbar(getActivity());
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.FavouriteCollection(WebUrl.Accept, WebUrl.ContentType,sessionManager.getAccesstoken(), body.getId()).enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    Functions.hideProgressbar(getActivity());
                    try {

                        if (response.isSuccessful()) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                JSONObject jsonObject_data = jsonObject.optJSONObject("data");
                                boolean favourite = Boolean.parseBoolean(jsonObject_data.optString("favourite"));
                                if(favourite) {
                                    ToastAddCollection();
                                }
                                else {
                                    ToastRemoveCollection();
                                }
                                refreshScreen();
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

    private void GoToCollectionDetail(BookModel.Data body) {
        Fragment fragment = new CollectionDetailFragment();
        BottomNavigation.fm
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
        Bundle arguments = new Bundle();
        arguments.putString(Constant.SELECTED_COLLECTION_ID, body.getId());
        fragment.setArguments(arguments);
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }

    @Override
    public void Retry() {

    }

    @Override
    public void onRefresh() {
        refreshScreen();
    }

    private void refreshScreen() {
        getDashboard(null);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipe_to_refresh.setRefreshing(false);
            }
        }, 1000);
    }
    private void InitCategory(ArrayList<BookModel.Data> body){
        for (BookModel.Data Data : body) {
            ProfileModel createProfile = new ProfileModel();
            createProfile.setName(Data.getTitle());
            createProfile.setImg_url(Data.getImg_url_only());
            tempArrayForCategory.add(createProfile);

        }
    }
    private void SetCategoryAdapted(){

        ProfileGridViewAdapter adapterViewAndroid = new ProfileGridViewAdapter(getContext(),tempArrayForCategory,false,false);
        adapterViewAndroid.setProfileSize(110,110);
        adapterViewAndroid.setTextSize(R.dimen._8sdp);
        gridViewCategory = binding.gridViewCategory;
        gridViewCategory.setAdapter(adapterViewAndroid);
        adapterViewAndroid.setAddOnItemClickListener(new ProfileGridViewAdapter.AddOnItemClickListener(){
            @Override
            public void onItemClick(int position) {

                getDashboard(tempArrayForCategory.get(position).getName());

            }
        });

    }
    void SearchBarHandler(){
        EditText search = binding.etSearch;
        //search.setImeActionLabel("Search", KeyEvent.KEYCODE_ENTER);
        search.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    toSearch(search.getText().toString());
                    Toast.makeText(getContext(), search.getText(), Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });
    }
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
    void ToastAddCollection(){
        StyleableToast.makeText(getContext(),"Saved To Collection", Toast.LENGTH_LONG,R.style.AddCollection).show();
    }
    void ToastRemoveCollection(){
        StyleableToast.makeText(getContext(),"Remove From Collection", Toast.LENGTH_LONG,R.style.RemoveCollection).show();
    }
}
