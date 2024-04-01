package gibbie.dino.readers.ui.fragments.search;

import static gibbie.dino.readers.retrofitsetup.CommonResponse.isUnauthorized;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonElement;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.Constant;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.commonclasses.SuperFragment;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.databinding.FragmentSearchGridBinding;
import gibbie.dino.readers.interfaces.NoInternet;
import gibbie.dino.readers.retrofitsetup.ServiceGenerator;
import gibbie.dino.readers.retrofitsetup.WebServices;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;
import gibbie.dino.readers.ui.fragments.home.BookModel;
import gibbie.dino.readers.ui.fragments.library.LibraryGridViewAdapter;
import gibbie.dino.readers.ui.fragments.singlebook.SingleBookFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends SuperFragment implements NoInternet, SharedPreferences.OnSharedPreferenceChangeListener, SwipeRefreshLayout.OnRefreshListener {
    private FragmentSearchGridBinding binding;
    SwipeRefreshLayout swipe_to_refresh;
    SessionManager sessionManager;
    private GridView gridView;
    private LibraryGridViewAdapter libraryGridViewAdapter;
    private String searchQuery;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchGridBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        createObjects();
        return view;
    }
    private void createObjects() {
        sessionManager = new SessionManager(getActivity());
        swipe_to_refresh = binding.swipeToRefresh;
        swipe_to_refresh.setOnRefreshListener(this);
        Functions.hideProgressbar(getActivity());
        gridView = binding.gridView;
        binding.llBack.setOnClickListener(this::backOnclick);
//        binding.llSearch.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                getSearch(binding.etSearch.getText().toString());
//            }
//        });
        SearchBarHandler();
        GetSearchGlobalQuery();
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
                    getSearch(binding.etSearch.getText().toString());
                    Toast.makeText(getContext(), search.getText(), Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });
    }
    void GetSearchGlobalQuery(){
        if(getArguments() == null)
            return;
        if(getArguments().containsKey(Constant.COLLECTION_DETAILS_SEARCH_QUERY) ) {
            String query = getArguments().getString(Constant.COLLECTION_DETAILS_SEARCH_QUERY);
            if (query != "") {
                binding.etSearch.setText(query);
                getSearch(query);

            }
        }
    }
    private void backOnclick(View view){
        BottomNavigation.fm.popBackStack();
    }
    public void getSearch(String query) {

        if (Functions.checkInternet(getContext())) {
            searchQuery = query;
            Functions.showProgressbar(getActivity());
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.Search(WebUrl.Accept, WebUrl.ContentType, sessionManager.getAccesstoken(), query).enqueue(new Callback<BookModel>() {
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
                    Functions.hideProgressbar(getActivity());
                }
            });

        } else {
            Functions.NoInternetcConnectionDialog(getContext(), this);
        }
    }

    private void parseResponse(BookModel body) {
        try {
            setAdapters(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        getSearch(searchQuery);

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

