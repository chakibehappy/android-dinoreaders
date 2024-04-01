package gibbie.dino.readers.ui.fragments.library;

import static android.content.Context.MODE_PRIVATE;

import static gibbie.dino.readers.retrofitsetup.CommonResponse.isUnauthorized;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

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
import gibbie.dino.readers.customlayout.OutlineTextView;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.databinding.FragmentLibraryNewBinding;
import gibbie.dino.readers.databinding.FragmentOwnStoryBinding;
import gibbie.dino.readers.retrofitsetup.ServiceGenerator;
import gibbie.dino.readers.retrofitsetup.WebServices;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;
import gibbie.dino.readers.ui.activities.guidedreading.SingleBookActivity;
import gibbie.dino.readers.ui.activities.quiz.Quiz;
import gibbie.dino.readers.ui.fragments.collection.CollectionData;
import gibbie.dino.readers.ui.fragments.collection.CollectionDetailListAdapter;
import gibbie.dino.readers.ui.fragments.collection.CollectionDetailModel;
import gibbie.dino.readers.ui.fragments.collection.CollectionListAdapter;
import gibbie.dino.readers.ui.fragments.collection.ResponseData;
import gibbie.dino.readers.ui.fragments.home.BookModel;
import gibbie.dino.readers.ui.fragments.home.DashboardModel;
import gibbie.dino.readers.ui.fragments.home.VerticalRecyclerViewAdapter;
import gibbie.dino.readers.ui.fragments.ownstory.OwnStoryBookCheckData;
import gibbie.dino.readers.ui.fragments.ownstory.SpellChecker;
import gibbie.dino.readers.ui.fragments.profile.ProfileFragment;
import gibbie.dino.readers.ui.fragments.singlebook.SingleBookFragment;
import gibbie.dino.readers.ui.fragments.singlebook.SingleBookModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    FragmentLibraryNewBinding binding;
    BottomNavigationView navBar;

    SessionManager sessionManager;
    SwipeRefreshLayout swipe_to_refresh;
    SharedPreferences sp;

    MaterialCardView tab_books, tab_collections;
    LinearLayout ll_book_section, ll_collection_section;
    RecyclerView rv_books_favourite, rv_created_books, rv_all_collections, rv_my_collections, rv_collections_detail;

    DashboardModel favouriteBookList;

    LinearLayout ll_collection_details;
    CardView cv_your_collection;
    TextView tv_collection_name;
    List<CollectionData> collectionDataList;
    ImageView btn_close;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLibraryNewBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        navBar = getActivity().findViewById(R.id.nav_view);

        View parent = binding.getRoot().getRootView();
        tab_books = parent.findViewById(R.id.tab_books);
        ll_book_section = parent.findViewById(R.id.ll_book_section);
        tab_books.setOnClickListener(v -> showBookTab());

        tab_collections = parent.findViewById(R.id.tab_collections);
        ll_collection_section = parent.findViewById(R.id.ll_collection_section);
        tab_collections.setOnClickListener(v -> showCollectionTab());

        sessionManager = new SessionManager(getContext());
        getProfileInfo();

        rv_books_favourite = parent.findViewById(R.id.rv_books_favourite);
        rv_books_favourite.setHasFixedSize(false);
        rv_created_books = parent.findViewById(R.id.rv_created_books);
        rv_all_collections = parent.findViewById(R.id.rv_all_collections);
        rv_my_collections = parent.findViewById(R.id.rv_my_collections);
        rv_collections_detail = parent.findViewById(R.id.rv_collections_detail);

        ll_collection_details = parent.findViewById(R.id.ll_collection_details);
        btn_close = parent.findViewById(R.id.btn_close);
        cv_your_collection = parent.findViewById(R.id.cv_your_collection);
        tv_collection_name = parent.findViewById(R.id.tv_collection_name);
        cv_your_collection = parent.findViewById(R.id.cv_your_collection);
        ll_collection_details.setVisibility(View.GONE);
        cv_your_collection.setVisibility(View.GONE);

        btn_close.setOnClickListener( v -> {
            ll_collection_details.setVisibility(View.GONE);}
        );

        getCollections();
        getFavouriteData();
        sp = getActivity().getSharedPreferences("DinoReader", MODE_PRIVATE);
        new OwnStoryBookCheckData().loadStoryBook(getContext(), sp, rv_created_books);

        swipe_to_refresh = binding.swipeToRefresh;
        swipe_to_refresh.setOnRefreshListener(this);
        showBookTab();

        return view;
    }

    void getCollections(){
        if(Functions.checkInternet(getContext())){
            JSONObject request = new JSONObject();
            JsonObjectRequest jsArrayRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    WebUrl.COLLECTIONDASHBOARDURL + "/show-all",
                    request,
                    new com.android.volley.Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Gson gson = new Gson();
                            ResponseData responseData = gson.fromJson(response.toString(), ResponseData.class);
                            collectionDataList = responseData.data;
                            CollectionListAdapter collectionListAdapter = new CollectionListAdapter(getContext(), collectionDataList);
                            CollectionListAdapter.OnItemClickListener clickListener = new CollectionListAdapter.OnItemClickListener() {
                                @Override
                                public void onItemClick(int position) {
                                    showCollectionDetails(collectionDataList.get(position));
                                }
                            };
                            collectionListAdapter.setOnItemClickListener(clickListener);
                            rv_all_collections.setAdapter(collectionListAdapter);
                        }
                    },
                    new com.android.volley.Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("TAG", "onErrorResponse: " + error);
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
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

    private void getFavouriteData()
    {
        if(Functions.checkInternet(getContext())){
            Functions.showProgressbar(getActivity());
            JSONObject request = new JSONObject();
            JsonObjectRequest jsArrayRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    WebUrl.LIBRARYURL,
                    request,
                    new com.android.volley.Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if(response.getBoolean("success")) {
                                    Gson gson = new Gson();
                                    Type listType = new TypeToken<DashboardModel>() {}.getType();
                                    favouriteBookList = gson.fromJson(response.toString(), listType);

                                    VerticalRecyclerViewAdapter.AddOnItemClickListener favouriteClickListener = new VerticalRecyclerViewAdapter.AddOnItemClickListener() {
                                        @Override
                                        public void onItemClick(int position) {
                                            GoToBookDetail(favouriteBookList.getData().get(0).getContent().get(position));
                                        }

                                        @Override
                                        public void onFavouriteClick(int position) {
                                            requestFavourite(favouriteBookList.getData().get(0).getContent().get(position));
                                        }
                                    };

                                    VerticalRecyclerViewAdapter favouriteBookAdapter = new VerticalRecyclerViewAdapter(favouriteBookList.getData().get(0).getContent(), getContext(), favouriteClickListener, false, true);
                                    favouriteBookAdapter.setAddOnItemClickListener(favouriteClickListener);
                                    rv_books_favourite.setAdapter(favouriteBookAdapter);
                                    Functions.hideProgressbar(getActivity());
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    },
                    new com.android.volley.Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("TAG", "onErrorResponse: " + error);
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
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

    private void showBookTab(){
        tab_books.setStrokeWidth(0);
        tab_collections.setStrokeWidth(4);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) tab_collections.getLayoutParams();
        layoutParams.setMargins(0, -2, 0, 0); // left, top, right, bottom margins in pixels
        tab_collections.setLayoutParams(layoutParams);
        ViewGroup.MarginLayoutParams layoutParams2 = (ViewGroup.MarginLayoutParams) tab_books.getLayoutParams();
        layoutParams2.setMargins(0, 2, 0, 0); // left, top, right, bottom margins in pixels
        tab_books.setLayoutParams(layoutParams2);

        ll_book_section.setVisibility(View.VISIBLE);
        ll_collection_section.setVisibility(View.GONE);
    }

    private void showCollectionTab(){
        tab_books.setStrokeWidth(4);
        tab_collections.setStrokeWidth(0);
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) tab_books.getLayoutParams();
        layoutParams.setMargins(0, -2, 0, 0); // left, top, right, bottom margins in pixels
        tab_books.setLayoutParams(layoutParams);
        ViewGroup.MarginLayoutParams layoutParams2 = (ViewGroup.MarginLayoutParams) tab_collections.getLayoutParams();
        layoutParams2.setMargins(0, 2, 0, 0); // left, top, right, bottom margins in pixels
        tab_collections.setLayoutParams(layoutParams2);

        ll_book_section.setVisibility(View.GONE);
        ll_collection_section.setVisibility(View.VISIBLE);
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
        Fragment fragment = new ProfileFragment();
        BottomNavigation.fm
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void GoToBookDetail( BookModel.Data body) {
        openBook(body.getId(), body.getFavourite());
    }

    private void openBook(String id, boolean isFavourite){

        Intent i = new Intent(getActivity(), SingleBookActivity.class);
        i.putExtra("IsFavourite", isFavourite);
        i.putExtra(Constant.SELECTED_BOOK_ID, id);
        i.putExtra("defaultMenu", "Your Library");
        startActivity(i);
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

    private void showCollectionDetails(@NonNull CollectionData data)
    {
        tv_collection_name.setText(data.getName());
        CollectionDetailListAdapter collectionDetailListAdapter = new CollectionDetailListAdapter(getContext(), data.getBooks());
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        rv_collections_detail.setLayoutManager(layoutManager);
        CollectionDetailListAdapter.OnItemClickListener clickListener = new CollectionDetailListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                openBook(String.valueOf(position), false);
            }
        };
        collectionDetailListAdapter.setOnItemClickListener(clickListener);
        rv_collections_detail.setAdapter(collectionDetailListAdapter);
        ll_collection_details.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRefresh() {
        refreshScreen();
    }

    private void refreshScreen() {
        //getLatestBook();
        getFavouriteData();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                swipe_to_refresh.setRefreshing(false);
            }
        }, 1000);
    }
}