package gibbie.dino.readers.ui.fragments.library;

import static gibbie.dino.readers.retrofitsetup.CommonResponse.isUnauthorized;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonElement;
import com.squareup.picasso.Picasso;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.Constant;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.commonclasses.SuperFragment;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.databinding.FragmentLibraryGridBinding;
import gibbie.dino.readers.interfaces.NoInternet;
import gibbie.dino.readers.retrofitsetup.ServiceGenerator;
import gibbie.dino.readers.retrofitsetup.WebServices;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;
import gibbie.dino.readers.ui.fragments.home.BookModel;
import gibbie.dino.readers.ui.fragments.profile.ProfileFragment;
import gibbie.dino.readers.ui.fragments.search.SearchFragment;
import gibbie.dino.readers.ui.fragments.singlebook.SingleBookFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryGridFragment extends SuperFragment implements NoInternet, SharedPreferences.OnSharedPreferenceChangeListener , SwipeRefreshLayout.OnRefreshListener {
    private LibraryGridViewAdapter libraryGridViewAdapter;
    SwipeRefreshLayout swipe_to_refresh;
    private GridView gridView;
    private FragmentLibraryGridBinding binding;
    SessionManager sessionManager;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_brown));
        binding = FragmentLibraryGridBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        gridView = binding.gridView;
        createObjects();
        Functions.hideProgressbar(getActivity());
        getLibrary();
        getUploadImageLocal();
        view.findViewById(R.id.iv_profile).setOnClickListener(this::toProfile);
        view.findViewById(R.id.iv_search).setOnClickListener(this::toSearch);
        return view;
    }
    private void createObjects() {
        sessionManager = new SessionManager(getActivity());
        swipe_to_refresh = binding.swipeToRefresh;
        swipe_to_refresh.setOnRefreshListener(this);
    }

    private void setAdapters(BookModel body) {

            libraryGridViewAdapter = new LibraryGridViewAdapter(getContext(), R.layout.child_recyclerview_items_new, body.getData());
            gridView.setAdapter(libraryGridViewAdapter);
            libraryGridViewAdapter.setAddOnItemClickListener(new LibraryGridViewAdapter.AddOnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    GoToBookDetail(position,body);

                }

                @Override
                public void onFavouriteClick(int position) {
                    requestFavourite(position,body);
                }
            });
    }
    public void toProfile(View view) {
        Fragment fragment = new ProfileFragment();
        BottomNavigation.fm
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }
    public void toSearch(View view) {
        Fragment fragment = new SearchFragment();
        BottomNavigation.fm
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }
    private void getUploadImageLocal() {
        if (sessionManager.getProfilePicturePath() != "") {
            ImageView ivProfile = (ImageView) binding.getRoot().findViewById(R.id.iv_profile);
            Picasso.get().load(sessionManager.getProfilePicturePath()).placeholder(R.drawable.profile).error(R.drawable.profile).into(ivProfile);

        }
    }
    public void getLibrary() {
        if (Functions.checkInternet(getContext())) {
            Functions.showProgressbar(getActivity());
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.LibraryGrid(WebUrl.Accept, WebUrl.ContentType, sessionManager.getAccesstoken()).enqueue(new Callback<BookModel>() {
                @Override
                public void onResponse(Call<BookModel> call, Response<BookModel> response) {
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
                public void onFailure(Call<BookModel> call, Throwable t) {
                    t.getMessage();
                }
            });

        } else {
            Functions.NoInternetcConnectionDialog(getContext(), this);
        }

    }
    private void parseResponse(BookModel body) {
        try {
            //bookData = body.getData();
            setAdapters(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void GoToBookDetail(int position,BookModel body) {
        Fragment fragment = new SingleBookFragment();
        BottomNavigation.fm
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
        Log.e("my id",body.getData().get(position).getId());
        Bundle arguments = new Bundle();
        arguments.putString(Constant.SELECTED_BOOK_ID, body.getData().get(position).getId());
        fragment.setArguments(arguments);
    }

    private void requestFavourite(int position,BookModel body) {
        if (Functions.checkInternet(getContext())) {
            Functions.showProgressbar(getActivity());
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.Favourite(WebUrl.Accept, WebUrl.ContentType,sessionManager.getAccesstoken(), body.getData().get(position).getId()).enqueue(new Callback<JsonElement>() {
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
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    @Override
    public void Retry() {

    }
    private void refreshScreen() {
        getLibrary();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipe_to_refresh.setRefreshing(false);
            }
        }, 1000);
    }

    @Override
    public void onRefresh() {
        refreshScreen();
    }
}
