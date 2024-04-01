package gibbie.dino.readers.ui.fragments.library;

import static gibbie.dino.readers.retrofitsetup.CommonResponse.isUnauthorized;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.Constant;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.commonclasses.SuperFragment;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.databinding.FragmentClassesHomeBinding;
import gibbie.dino.readers.interfaces.NoInternet;
import gibbie.dino.readers.retrofitsetup.ServiceGenerator;
import gibbie.dino.readers.retrofitsetup.WebServices;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;
import gibbie.dino.readers.ui.fragments.collection.CollectionDetailFragment;
import gibbie.dino.readers.ui.fragments.home.BookModel;
import gibbie.dino.readers.ui.fragments.home.DashboardBookCategoryModel;
import gibbie.dino.readers.ui.fragments.home.DashboardHorizontalRecycleViewAdapter;
import gibbie.dino.readers.ui.fragments.home.DashboardModel;
import gibbie.dino.readers.ui.fragments.home.VerticalRecyclerViewAdapter;
import gibbie.dino.readers.ui.fragments.singlebook.SingleBookFragment;
import io.github.muddz.styleabletoast.StyleableToast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibrarySliderFragment  extends SuperFragment implements NoInternet, SharedPreferences.OnSharedPreferenceChangeListener, SwipeRefreshLayout.OnRefreshListener{
    ArrayList<DashboardBookCategoryModel> dashboardParentModelArrayList = new ArrayList<>();
    SessionManager sessionManager;
    SwipeRefreshLayout swipe_to_refresh;
    private FragmentClassesHomeBinding binding;
    private RecyclerView parentRecyclerView;
    private RecyclerView.LayoutManager parentLayoutManager;
    private DashboardHorizontalRecycleViewAdapter DashboardAdapter;
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_brown));
        binding = FragmentClassesHomeBinding.inflate(getLayoutInflater());
        Functions.hideProgressbar(getActivity());
        View view = binding.getRoot();

        createObjects();
        getDashboard();

        return view;
    }
    private void createObjects() {
        sessionManager = new SessionManager(getActivity());
        swipe_to_refresh = binding.swipeToRefresh;
        swipe_to_refresh.setOnRefreshListener(this);


        parentRecyclerView = binding.ParentRecyclerView;
        parentRecyclerView.setHasFixedSize(true);

    }
    public void getDashboard() {

        if (Functions.checkInternet(getContext())) {
            Functions.showProgressbar(getActivity());
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.LibrarySlider(WebUrl.Accept, WebUrl.ContentType,sessionManager.getAccesstoken()).enqueue(new Callback<DashboardModel>() {
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
                                DetailItemTypeParser(data.getItem_type(),data.getContent().get(position));
                            }

                            @Override
                            public void onFavouriteClick(int position) {
                                FavouriteItemTypeParser(data.getItem_type(),data.getContent().get(position));
                            }
                        });
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }

    @Override
    public void onRefresh()  {
        refreshScreen();
    }

    private void refreshScreen() {
        getDashboard();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipe_to_refresh.setRefreshing(false);
            }
        }, 1000);
    }

    @Override
    public void Retry() {

    }
    private void DetailItemTypeParser(String item_type,BookModel.Data data){
        switch (item_type){
            case "book":
                GoToBookDetail(data);
                break;
            case "collection":
                GoToCollectionDetail(data);
                break;
            default:
                ToastFail("Unavailable features");
                break;
        }
    }
    private void FavouriteItemTypeParser(String item_type,BookModel.Data data){
        switch (item_type){
            case "book":
                requestBookFavourite(data);
                break;
            case "collection":
                requestCollectionFavourite(data);
                break;
            default:
                ToastFail("Unavailable features");
                break;
        }
    }
    private void GoToBookDetail( BookModel.Data body) {
        Fragment fragment = new SingleBookFragment();
        BottomNavigation.fm
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
        Bundle arguments = new Bundle();
        arguments.putString(Constant.SELECTED_BOOK_ID, body.getId());
        fragment.setArguments(arguments);
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
                                FavouriteParser(response);
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
    private void requestBookFavourite(BookModel.Data body) {
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
                                FavouriteParser(response);
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

    private void FavouriteParser(Response<JsonElement> response) throws JSONException {
        JSONObject jsonObject = new JSONObject(response.body().toString());
        JSONObject jsonObject_data = jsonObject.optJSONObject("data");
        boolean favourite = Boolean.parseBoolean(jsonObject_data.optString("favourite"));
        if (favourite) {
            ToastSuccess("Added Collection");
        } else {
            ToastFail("Removed Collection");
        }
        refreshScreen();
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

    void ToastSuccess(String Message){
        StyleableToast.makeText(getContext(),Message, Toast.LENGTH_LONG,R.style.AddCollection).show();
    }
    void ToastFail(String Message){
        StyleableToast.makeText(getContext(),Message, Toast.LENGTH_LONG,R.style.RemoveCollection).show();
    }
}
