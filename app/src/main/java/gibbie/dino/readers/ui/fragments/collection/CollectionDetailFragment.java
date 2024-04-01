package gibbie.dino.readers.ui.fragments.collection;

import static gibbie.dino.readers.retrofitsetup.CommonResponse.isUnauthorized;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;
import com.synnapps.carouselview.CarouselView;
import com.synnapps.carouselview.ViewListener;

import java.util.ArrayList;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.Constant;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.commonclasses.SuperFragment;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.databinding.FragmentCollectionDetailsBinding;
import gibbie.dino.readers.interfaces.NoInternet;
import gibbie.dino.readers.retrofitsetup.ServiceGenerator;
import gibbie.dino.readers.retrofitsetup.WebServices;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;
import gibbie.dino.readers.ui.fragments.home.BookModel;
import gibbie.dino.readers.ui.fragments.readbook.ReadBookFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CollectionDetailFragment extends SuperFragment implements NoInternet, SharedPreferences.OnSharedPreferenceChangeListener, SwipeRefreshLayout.OnRefreshListener{

    SessionManager sessionManager;
    SwipeRefreshLayout swipe_to_refresh;
    private FragmentCollectionDetailsBinding binding;
    ListView listView;
    String mTitle[] = {"Book 1", "Book 1", "Book 1", "Book 1", "Book 1"};
    String mDescription[] = {"Book 1 Description", "Book 1 Description", "Book 1 Description", "Book 1 Description", "Book 1 Description"};
    int images[] = {R.drawable.logo, R.drawable.logo, R.drawable.logo, R.drawable.logo, R.drawable.logo};
    // so our images and other things are set in array
    private RecyclerView.LayoutManager parentLayoutManager;
    private CollectionHorizontalRecycleViewAdapter CollectionHeaderAdapter;
    ArrayList<BookModel.Data> dashboardParentModelArrayList = new ArrayList<>();
    ArrayList<BookModel.Data> bookData = new ArrayList<>();
    CarouselView customCarouselView;
    BottomNavigationView navBar;
    TextView tv_title;
    TextView tv_description;
    TextView tv_bookCount;
    TextView tv_listTitle;
    TextView tv_box_description_view;
    ListViewDetailAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_brown));
        binding = FragmentCollectionDetailsBinding.inflate(getLayoutInflater());
        Functions.hideProgressbar(getActivity());
        View view = binding.getRoot();
        createObjects();
        getDetails();
        return view;
    }
    private void createObjects() {
        sessionManager = new SessionManager(getActivity());
        swipe_to_refresh = binding.swipeToRefresh;
        swipe_to_refresh.setOnRefreshListener(this);

//        customCarouselView = binding.carouselView;

        listView = binding.listviewBooks;
        listView.setNestedScrollingEnabled(true);


        tv_title = binding.title;
//        tv_description = binding.description;
        tv_bookCount = binding.bookCount;
        tv_listTitle = binding.tvListTitle;
        tv_box_description_view = binding.boxDescriptionView;

        navBar = getActivity().findViewById(R.id.nav_view);
        navBar.setVisibility(View.GONE);
        binding.ivBack.setOnClickListener(this::backOnclick);
        
        getActivity().getOnBackPressedDispatcher().addCallback(new  OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navBar.setVisibility(View.VISIBLE);
                BottomNavigation.fm.popBackStack();
            }
        });
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }

    @Override
    public void onRefresh()  {
        refreshScreen();
    }

    private void refreshScreen() {
        getDetails();
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
    public void getDetails() {
        if(getArguments() == null)
            return;
        if(getArguments().containsKey(Constant.SELECTED_COLLECTION_ID) ) {
            String collection_id = getArguments().getString(Constant.SELECTED_COLLECTION_ID);
            if (collection_id != "") {
                if (Functions.checkInternet(getContext())) {
                    Functions.showProgressbar(getActivity());
                    WebServices webServices = ServiceGenerator.createService(WebServices.class);
                    webServices.CollectionDetail(WebUrl.Accept, WebUrl.ContentType, sessionManager.getAccesstoken(), collection_id).enqueue(new Callback<CollectionDetailModel>() {
                        @Override
                        public void onResponse(Call<CollectionDetailModel> call, Response<CollectionDetailModel> response) {
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
                                    Log.e("response", "Oops something went wrong!Please try again");
                                    // "Oops something went wrong!Please try again";
                                }
                            } catch (Exception e) {
                                e.printStackTrace();

                            }
                        }

                        @Override
                        public void onFailure(Call<CollectionDetailModel> call, Throwable t) {
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

    private void parseResponse(CollectionDetailModel body) {
        try {
            tv_title.setText(body.getData().getName());
//            tv_description.setText(body.getData().getDescription());
            tv_box_description_view.setText(body.getData().getDescription());
            int bookCount = body.getData().getContent().size();

            String bookCountString;
            bookData = body.getData().getContent();

            if(bookCount <= 0) {
                bookCountString = "No Books";
                tv_listTitle.setText(bookCountString);
                tv_bookCount.setText("");
            }
            else {
                bookCountString = String.valueOf(bookCount) + " Books";
                tv_listTitle.setText("List");
                tv_bookCount.setText(bookCountString);
            }

            tv_bookCount.setText(bookCountString);
            CarouselView(body.getData().getContent());
//            BookCategoryView(body.getData().getContent());
            ListViewAdapter(body.getData().getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void CarouselView(ArrayList<BookModel.Data> data) {
        ViewListener viewListener = position -> {
            View customView = getLayoutInflater().inflate(R.layout.child_carouselview_items, null);
            //set view attributes here
            TextView bookTitle = customView.findViewById(R.id.book_name);
            TextView bookDescription = customView.findViewById(R.id.book_description);
            ImageView bookCover = customView.findViewById(R.id.hero_image);
            LinearLayout llRoot = customView.findViewById(R.id.ll_root);
            BookModel.Data bookData = data.get(position);
            //Picasso.get().load(bookData.getBaseAndImage_url()).into(bookCover);
            Picasso.get().load(bookData.getImg_url_only()).into(bookCover);
            bookTitle.setText(bookData.getTitle());
            bookDescription.setText(bookData.getDescription());

//            GridLayout glReadingLevel = customView.findViewById(R.id.gl_reading_level);
//            for(BookModel.Data.Categories cat : bookData.getLevel()) {
//                if(cat.getName() != null && cat.getColor() != null)
//                    glReadingLevel.addView(MakeCategory(getActivity(), cat.getName(), Color.parseColor(cat.getColor()),true));
//            }

            if(bookData.getCategories() != null) {
                GridLayout glCategories = customView.findViewById(R.id.gl_categories);
                for (BookModel.Data.Categories cat : bookData.getCategories()) {
                    if (cat.getName() != null && cat.getColor() != null)
                        glCategories.addView(MakeCategory(getActivity(), cat.getName(), Color.parseColor(cat.getColor()), true));
                }
            }

            llRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   // GoToBookDetail(bookData);
                }
            });
            return customView;
        };
//        customCarouselView.setViewListener(viewListener);
//        customCarouselView.setPageCount(data.size());
    }
    private void BookCategoryView(ArrayList<BookModel.Data> data) {
        ViewListener viewListener = position -> {
            View customView = getLayoutInflater().inflate(R.layout.child_carouselview_items, null);
            //set view attributes here
            TextView bookTitle = customView.findViewById(R.id.book_name);
            TextView bookDescription = customView.findViewById(R.id.book_description);
            ImageView bookCover = customView.findViewById(R.id.hero_image);
            LinearLayout llRoot = customView.findViewById(R.id.ll_root);
            BookModel.Data bookData = data.get(position);
            //Picasso.get().load(bookData.getBaseAndImage_url()).into(bookCover);
            Picasso.get().load(bookData.getImg_url_only()).into(bookCover);
            bookTitle.setText(bookData.getTitle());
            bookDescription.setText(bookData.getDescription());

//            GridLayout glReadingLevel = customView.findViewById(R.id.gl_reading_level);
//            for(BookModel.Data.Categories cat : bookData.getLevel()) {
//                if(cat.getName() != null && cat.getColor() != null)
//                    glReadingLevel.addView(MakeCategory(getActivity(), cat.getName(), Color.parseColor(cat.getColor()),true));
//            }

            if(bookData.getCategories() != null) {
                GridLayout glCategories = customView.findViewById(R.id.gl_categories);
                for (BookModel.Data.Categories cat : bookData.getCategories()) {
                    if (cat.getName() != null && cat.getColor() != null)
                        glCategories.addView(MakeCategory(getActivity(), cat.getName(), Color.parseColor(cat.getColor()), true));
                }
            }

            llRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // GoToBookDetail(bookData);
                }
            });
            return customView;
        };
    }
    private void ListViewAdapter(ArrayList<BookModel.Data> data) {
        // now create an adapter class
       // Log.e("LISTVIEW","ADD");
        adapter = new ListViewDetailAdapter(data,getContext());
        listView.setAdapter(adapter);
        // there is my mistake...
        // now again check this..

        // now set item click on list view
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ReadBook(data.get(position));
            }
        });
        adapter.notifyDataSetChanged();
    }
    private void ReadBook(BookModel.Data data) {
        Fragment fragment = new ReadBookFragment();
        BottomNavigation.fm
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
        Bundle arguments = new Bundle();
        arguments.putString("READ_URL", data.getRead_url()+"&profile_id="+sessionManager.getProfileId());
        fragment.setArguments(arguments);
    }
    private void backOnclick(View view){
        navBar.setVisibility(View.VISIBLE);
        BottomNavigation.fm.popBackStack();
    }
}
