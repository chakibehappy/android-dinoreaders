package gibbie.dino.readers.ui.fragments.singlebook;

import static gibbie.dino.readers.retrofitsetup.CommonResponse.isUnauthorized;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.Constant;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.commonclasses.SuperFragment;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.databinding.FragmentSingleBookBinding;
import gibbie.dino.readers.interfaces.NoInternet;
import gibbie.dino.readers.retrofitsetup.ServiceGenerator;
import gibbie.dino.readers.retrofitsetup.WebServices;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;
import gibbie.dino.readers.ui.activities.guidedreading.GuidedReadingActivity;
import gibbie.dino.readers.ui.activities.placementtest.Book;
import gibbie.dino.readers.ui.activities.placementtest.JsonReaderTask;
import gibbie.dino.readers.ui.activities.placementtest.Line;
import gibbie.dino.readers.ui.activities.placementtest.Page;
import gibbie.dino.readers.ui.activities.placementtest.PlacementTestReadingActivity;
import gibbie.dino.readers.ui.activities.quiz.Quiz;
import gibbie.dino.readers.ui.activities.quiz.QuizActivity;
import gibbie.dino.readers.ui.activities.readingbuddy.ReadingBuddyActivity;
import gibbie.dino.readers.ui.activities.setting.ReadingTimeCounter;
import gibbie.dino.readers.ui.fragments.home.BookCategoryModel;
import gibbie.dino.readers.ui.fragments.home.BookModel;
import gibbie.dino.readers.ui.fragments.home.HorizontalRecyclerViewAdapter;
import gibbie.dino.readers.ui.fragments.home.VerticalRecyclerViewAdapter;
import gibbie.dino.readers.ui.fragments.ownstory.OwnStoryBookModel;
import gibbie.dino.readers.ui.fragments.ownstory.OwnStoryPageModel;
import gibbie.dino.readers.ui.fragments.readbook.ReadBookFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SingleBookFragment extends SuperFragment implements NoInternet, SwipeRefreshLayout.OnRefreshListener {
    private FragmentSingleBookBinding binding;
    private RecyclerView parentRecyclerView;
    private HorizontalRecyclerViewAdapter ParentAdapter;
    ArrayList<BookCategoryModel> parentModelArrayList = new ArrayList<>();
    private RecyclerView.LayoutManager parentLayoutManager;
    SessionManager sessionManager;
    SwipeRefreshLayout swipe_to_refresh;
    BottomNavigationView navBar;
    private String book_id;
    boolean isFavourite = false;
    private List<Quiz> quizList;
    private List<SingleBookModel.Data.Pages> pageSettings;

    SingleBookModel.Data selectedBookData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSingleBookBinding.inflate(getLayoutInflater());
        binding.llBack.setOnClickListener(v -> goback());

        View view = binding.getRoot();
        createObjects();
        getBookDetails();
        getLatestBook();
        getQuizData();

        getActivity().findViewById(R.id.nav_view).setVisibility(View.GONE);
        return view;
    }
    private void createObjects() {
        sessionManager = new SessionManager(getActivity());
//        Log.d("TAG", "reading time: " + sessionManager.getReadingTime());

        swipe_to_refresh = binding.swipeToRefresh;
        swipe_to_refresh.setOnRefreshListener(this);
        Functions.hideProgressbar(getActivity());

        parentRecyclerView = binding.ParentRecyclerView;
        parentRecyclerView.setHasFixedSize(true);

        binding.llSelection.setOnClickListener(v->{showReadSelection(false);});
        showReadSelection(false);

        binding.btnReadToMe.setOnClickListener((v -> {ReadBook(true);}));
        binding.btnReadAlone.setOnClickListener((v -> {ReadBook(false);}));
    }

    private void showReadSelection(boolean isShow){
        binding.llSelection.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void getBookDetails() {
        if(getArguments() == null)
            return;
        if(getArguments().containsKey(Constant.SELECTED_BOOK_ID) ){
            String book_id =  getArguments().getString(Constant.SELECTED_BOOK_ID);
            isFavourite = getArguments().getBoolean("IsFavourite");

            if(book_id != ""){
                if (Functions.checkInternet(getContext())) {
                    Functions.showProgressbar(getActivity());
                    WebServices webServices = ServiceGenerator.createService(WebServices.class);
                    webServices.BookDetail(WebUrl.Accept, WebUrl.ContentType, sessionManager.getAccesstoken(),book_id).enqueue(new Callback<SingleBookModel>() {
                        @Override
                        public void onResponse(Call<SingleBookModel> call, Response<SingleBookModel> response) {
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
                        public void onFailure(Call<SingleBookModel> call, Throwable t) {
                            t.getMessage();
                            Functions.hideProgressbar(getActivity());
                        }
                    });

                } else {
                    Functions.NoInternetcConnectionDialog(getContext(), this);
                }
            }
        }
    }
    private void parseResponse(SingleBookModel body) {
        try {
            SingleBookModel.Data data = body.getData();
            pageSettings = data.getPages();
            binding.tvAuthor.setText(data.getAuthor());

            binding.ivFavouriteIcon.setVisibility(View.VISIBLE);
            if (isFavourite)
                binding.ivFavouriteIcon.setImageResource(R.drawable.heart);
            else
                binding.ivFavouriteIcon.setVisibility(View.GONE);
//                binding.ivFavouriteIcon.setImageResource(R.drawable.heart_empty);
            String readingLevel = "Reading level : " + data.getReading_level();
            binding.tvReadingLevel.setText(readingLevel);

            book_id = data.getId();
            Picasso.get().load(data.getImage_url()).into(binding.bookCover);
//            GridLayout glCategories = binding.glCategories;
//            for(BookModel.Data.Categories cat : data.getCategories()) {
//                if(cat.getName() != null && cat.getColor() != null)
//                    glCategories.addView(MakeCategory(getActivity(), cat.getName(), Color.parseColor(cat.getColor()),false));
//            }
//            binding.bookDescription.setText( data.getDescription());
            binding.tvTitle.setText(data.getTitle());
            binding.btnRead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showReadSelection(true);
                    selectedBookData = data;
//                    ReadBook(data);
                }
            });
            Picasso.get().load(data.getImage_url()).into(binding.bookCover);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void getQuizData(){
        quizList = new ArrayList<>();
        if(Functions.checkInternet(getContext())){
            JSONObject request = new JSONObject();
            String book_id =  getArguments().getString(Constant.SELECTED_BOOK_ID);
            JsonObjectRequest jsArrayRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    WebUrl.GETBOOKQUIZ + book_id,
                    request,
                    new com.android.volley.Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if(response.getBoolean("success")) {
                                    if(response.getJSONArray("quiz").length() > 0){
                                        Gson gson = new Gson();
                                        Type listType = new TypeToken<List<Quiz>>() {}.getType();
                                        quizList = gson.fromJson(response.getJSONArray("quiz").toString(), listType);
                                    }
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

    private void ReadBook(Boolean isReadToMe) {
        SingleBookModel.Data data = selectedBookData;
        if(sessionManager.canReadToday())
        {
            String uid = data.getUid();
            String url = WebUrl.AWSBOOKPATH + uid + "/book_data.json";
            JsonReaderTask task = new JsonReaderTask(new JsonReaderTask.JsonReaderListener() {
                @Override
                public void onJsonRead(Book book) {
                    List<Page> pages = book.getPages();
                    List<Page> finalPages = new ArrayList<>();
                    // process to add both text and audio url here
                    for (int i = 0; i < pages.size(); i++) {
                        pages.get(i).setStoryPage(pageSettings.get(i).getShow_pages() == 1);
                        pages.get(i).setPlayAudio(pageSettings.get(i).getPlay_audio() == 1);

                        if(pages.get(i).isStoryPage())
                        {
                            if(pages.get(i).getLines().size() <= 0)
                                pages.get(i).setPlayAudio(false);
                            finalPages.add(pages.get(i));
                        }
                    }

                    Intent intent = new Intent(getContext(), GuidedReadingActivity.class);
                    intent.putExtra("id", book_id);
                    intent.putExtra("uid", uid);
                    intent.putExtra("pagesList", (Serializable) finalPages);
                    intent.putExtra("quizList", (Serializable) quizList);
                    intent.putExtra("isReadToMe", isReadToMe);
                    getContext().startActivity(intent);
                    showReadSelection(false);
                }
            });
            task.execute(url);
        }
        else
        {
            ReadingTimeCounter.showReadingDayOutAlert(getContext());
        }
    }

    // Get Latest Book API
    public void getLatestBook() {
        if (Functions.checkInternet(getContext())) {
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.BookLatest(WebUrl.Accept, WebUrl.ContentType, sessionManager.getAccesstoken()).enqueue(new Callback<BookModel>() {
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
//            set the Categories for each array list set in the `ParentViewHolder`
            parentModelArrayList.clear();
            parentModelArrayList.add(new BookCategoryModel("More for you!",body));

            ParentAdapter = new HorizontalRecyclerViewAdapter(parentModelArrayList, getContext(), true, true);
            parentLayoutManager = new LinearLayoutManager(getContext());
            parentRecyclerView.setLayoutManager(parentLayoutManager);
            parentRecyclerView.setAdapter(ParentAdapter);
            ParentAdapter.notifyDataSetChanged();
            ParentAdapter.setVerticalAddOnItemClickListener(new VerticalRecyclerViewAdapter.AddOnItemClickListener() {
                @Override
                public void onItemClick(int position) {  GoToBookDetail(position, body);  }

                @Override
                public void onFavouriteClick(int position) {
                    requestFavourite(position, body);
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void GoToBookDetail(int position, BookModel body) {
        Fragment fragment = new SingleBookFragment();
        BottomNavigation.fm
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
        Bundle arguments = new Bundle();
        arguments.putString(Constant.SELECTED_BOOK_ID, body.getData().get(position).getId());
        arguments.putBoolean("IsFavourite", body.getData().get(position).getFavourite());
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

    public void goback() {
        BottomNavigation.fm.popBackStack();
    }

    @Override
    public void Retry() {
        getLatestBook();
    }

    @Override
    public void onRefresh() {
        refreshScreen();
    }
    private void refreshScreen() {
        getBookDetails();
        getLatestBook();

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
    }

}