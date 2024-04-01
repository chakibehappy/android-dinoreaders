package gibbie.dino.readers.ui.activities.guidedreading;

import static gibbie.dino.readers.retrofitsetup.CommonResponse.isUnauthorized;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.Constant;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.interfaces.NoInternet;
import gibbie.dino.readers.retrofitsetup.ServiceGenerator;
import gibbie.dino.readers.retrofitsetup.WebServices;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;
import gibbie.dino.readers.ui.activities.placementtest.Book;
import gibbie.dino.readers.ui.activities.placementtest.JsonReaderTask;
import gibbie.dino.readers.ui.activities.placementtest.Page;
import gibbie.dino.readers.ui.activities.quiz.Quiz;
import gibbie.dino.readers.ui.activities.setting.ReadingTimeCounter;
import gibbie.dino.readers.ui.fragments.home.BookCategoryModel;
import gibbie.dino.readers.ui.fragments.home.BookModel;
import gibbie.dino.readers.ui.fragments.home.HorizontalRecyclerViewAdapter;
import gibbie.dino.readers.ui.fragments.home.VerticalRecyclerViewAdapter;
import gibbie.dino.readers.ui.fragments.singlebook.SingleBookFragment;
import gibbie.dino.readers.ui.fragments.singlebook.SingleBookModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SingleBookActivity extends AppCompatActivity implements NoInternet, SwipeRefreshLayout.OnRefreshListener {

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

    LinearLayout ll_selection;
    String defaultMenu = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_book);
        getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_brown));
        LinearLayout llBack = findViewById(R.id.ll_back);
        llBack.setOnClickListener(v -> this.finish());

        createObjects();
        getBookDetails();
        getLatestBook();
        getQuizData();
    }

    private void createObjects() {
        sessionManager = new SessionManager(SingleBookActivity.this);

        swipe_to_refresh = findViewById(R.id.swipe_to_refresh);
        swipe_to_refresh.setOnRefreshListener(this);
        Functions.hideProgressbar(this);

        parentRecyclerView = findViewById(R.id.Parent_recyclerView);
        parentRecyclerView.setHasFixedSize(true);

        ll_selection = findViewById(R.id.ll_selection);
        ll_selection.setOnClickListener(v->{showReadSelection(false);});
        showReadSelection(false);

        LinearLayout btnReadToMe = findViewById(R.id.btn_read_to_me);
        btnReadToMe.setOnClickListener((v -> {ReadBook(true);}));
        LinearLayout btnReadAlone = findViewById(R.id.btn_read_alone);
        btnReadAlone.setOnClickListener((v -> {ReadBook(false);}));
        LinearLayout btnReadNoText = findViewById(R.id.btn_read_no_text);
        btnReadNoText.setOnClickListener((v -> {ReadBookWithNoText(true);}));
    }

    private void showReadSelection(boolean isShow){
        ll_selection.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void getBookDetails() {

        book_id = getIntent().getStringExtra(Constant.SELECTED_BOOK_ID);
        isFavourite = getIntent().getBooleanExtra("IsFavourite", false);
        defaultMenu = getIntent().getStringExtra("defaultMenu");

        if(book_id != ""){
            if (Functions.checkInternet(this)) {
                Functions.showProgressbar(SingleBookActivity.this);
                WebServices webServices = ServiceGenerator.createService(WebServices.class);
                webServices.BookDetail(WebUrl.Accept, WebUrl.ContentType, sessionManager.getAccesstoken(),book_id).enqueue(new Callback<SingleBookModel>() {
                    @Override
                    public void onResponse(Call<SingleBookModel> call, Response<SingleBookModel> response) {
                        Functions.hideProgressbar(SingleBookActivity.this);
                        try {

                            if (response.isSuccessful()) {
                                try {
                                    parseResponse(response.body());

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else if (isUnauthorized(SingleBookActivity.this, response.code())) {
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
                        Functions.hideProgressbar(SingleBookActivity.this);
                    }
                });

            } else {
                Functions.NoInternetcConnectionDialog(this, this);
            }
        }
    }
    private void parseResponse(SingleBookModel body) {
        try {
            SingleBookModel.Data data = body.getData();
            pageSettings = data.getPages();
            TextView tvAuthor = findViewById(R.id.tv_author);
            tvAuthor.setText(data.getAuthor());

            ImageView ivFavouriteIcon = findViewById(R.id.iv_favourite_icon);
            ivFavouriteIcon.setVisibility(View.VISIBLE);
            if (isFavourite)
                ivFavouriteIcon.setImageResource(R.drawable.heart);
            else
                ivFavouriteIcon.setVisibility(View.GONE);

            TextView tvReadingLevel = findViewById(R.id.tv_reading_level);
            String readingLevel = "Reading level : " + data.getReading_level();
            tvReadingLevel.setText(readingLevel);

            book_id = data.getId();
            ImageView bookCover = findViewById(R.id.book_cover);
            Picasso.get().load(data.getImage_url()).into(bookCover);

            TextView tvTitle = findViewById(R.id.tv_title);
            tvTitle.setText(data.getTitle());
            AppCompatButton btnRead = findViewById(R.id.btn_read);
            btnRead.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showReadSelection(true);
                    selectedBookData = data;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void getQuizData(){
        quizList = new ArrayList<>();
        if(Functions.checkInternet(this)){
            JSONObject request = new JSONObject();

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

            RequestQueue mRequestQueue = Volley.newRequestQueue(this);
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

                    Intent intent = new Intent(SingleBookActivity.this, GuidedReadingActivity.class);
                    intent.putExtra("id", book_id);
                    intent.putExtra("uid", uid);
                    intent.putExtra("pagesList", (Serializable) finalPages);
                    intent.putExtra("quizList", (Serializable) quizList);
                    intent.putExtra("isReadToMe", isReadToMe);
                    intent.putExtra("defaultMenu", defaultMenu);
                    startActivity(intent);
                    showReadSelection(false);
                }
            });
            task.execute(url);
        }
        else
        {
            ReadingTimeCounter.showReadingDayOutAlert(this);
        }
    }

    private void ReadBookWithNoText(Boolean isReadToMe) {
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

                    Intent intent = new Intent(SingleBookActivity.this, SingleBookReadingActivity.class);
                    intent.putExtra("id", book_id);
                    intent.putExtra("uid", uid);
                    intent.putExtra("pagesList", (Serializable) finalPages);
                    intent.putExtra("quizList", (Serializable) quizList);
                    intent.putExtra("isReadToMe", isReadToMe);
                    intent.putExtra("defaultMenu", defaultMenu);
                    startActivity(intent);
                    showReadSelection(false);
                }
            });
            task.execute(url);
        }
        else
        {
            ReadingTimeCounter.showReadingDayOutAlert(this);
        }
    }

    // Get Latest Book API
    public void getLatestBook() {
        if (Functions.checkInternet(this)) {
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.BookLatest(WebUrl.Accept, WebUrl.ContentType, sessionManager.getAccesstoken()).enqueue(new Callback<BookModel>() {
                @Override
                public void onResponse(Call<BookModel> call, Response<BookModel> response) {
                    Functions.hideProgressbar(SingleBookActivity.this);
                    try {

                        if (response.isSuccessful()) {
                            try {
                                parseResponse(response.body());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (isUnauthorized(SingleBookActivity.this, response.code())) {
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
                    Functions.hideProgressbar(SingleBookActivity.this);
                }
            });

        } else {
            Functions.NoInternetcConnectionDialog(this, this);
        }
    }

    private void parseResponse(BookModel body) {
        try {
            parentModelArrayList.clear();
            parentModelArrayList.add(new BookCategoryModel("More for you!",body));

            ParentAdapter = new HorizontalRecyclerViewAdapter(parentModelArrayList, this, true, true);
            parentLayoutManager = new LinearLayoutManager(this);
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
        Intent i = new Intent(this, SingleBookActivity.class);
        i.putExtra("IsFavourite", body.getData().get(position).getFavourite());
        i.putExtra(Constant.SELECTED_BOOK_ID, body.getData().get(position).getId());
        startActivity(i);
    }

    private void requestFavourite(int position,BookModel body) {
        if (Functions.checkInternet(this)) {
            Functions.showProgressbar(SingleBookActivity.this);
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.Favourite(WebUrl.Accept, WebUrl.ContentType,sessionManager.getAccesstoken(), body.getData().get(position).getId()).enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    Functions.hideProgressbar(SingleBookActivity.this);
                    try {

                        if (response.isSuccessful()) {
                            try {
                                refreshScreen();
                                //  parseResponse(response.body());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (isUnauthorized(SingleBookActivity.this, response.code())) {
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
            Functions.hideProgressbar(SingleBookActivity.this);
            Log.e("No Internet:","OnFavourite");
        }
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
}