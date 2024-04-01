package gibbie.dino.readers.ui.fragments.classes;

import static gibbie.dino.readers.retrofitsetup.CommonResponse.isUnauthorized;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.Constant;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.commonclasses.SuperFragment;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.databinding.FragmentClassesDetailBinding;
import gibbie.dino.readers.interfaces.NoInternet;
import gibbie.dino.readers.retrofitsetup.ServiceGenerator;
import gibbie.dino.readers.retrofitsetup.WebServices;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;
import gibbie.dino.readers.ui.fragments.collection.CollectionDetailModel;
import gibbie.dino.readers.ui.fragments.collection.ListViewDetailAdapter;
import gibbie.dino.readers.ui.fragments.home.BookModel;
import gibbie.dino.readers.ui.fragments.readbook.ReadBookFragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClassesDetailFragment extends SuperFragment implements NoInternet, SharedPreferences.OnSharedPreferenceChangeListener, SwipeRefreshLayout.OnRefreshListener{

    SessionManager sessionManager;
    SwipeRefreshLayout swipe_to_refresh;
    private FragmentClassesDetailBinding binding;
    ListView listView;
    BottomNavigationView navBar;
    TextView tv_title;
    TextView tv_box_class_description;
    TextView tv_bookCount;
    TextView tv_listTitle;
    ListViewDetailAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_brown));
        binding = FragmentClassesDetailBinding.inflate(getLayoutInflater());
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

        listView = binding.listviewBooks;
        listView.setNestedScrollingEnabled(true);


        tv_title = binding.title;
        tv_box_class_description = binding.boxClassDescription;
        tv_bookCount = binding.bookCount;
        tv_listTitle = binding.tvListTitle;

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
        if(getArguments().containsKey(Constant.SELECTED_CLASS_ID) ) {
            String collection_id = getArguments().getString(Constant.SELECTED_CLASS_ID);
            if (collection_id != "") {
                if (Functions.checkInternet(getContext())) {
                    Functions.showProgressbar(getActivity());
                    WebServices webServices = ServiceGenerator.createService(WebServices.class);
                    webServices.ClassesDetail(WebUrl.Accept, WebUrl.ContentType, sessionManager.getAccesstoken(), collection_id).enqueue(new Callback<CollectionDetailModel>() {
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
            tv_box_class_description.setText(body.getData().getDescription());
            int bookCount = body.getData().getContent().size();

            String bookCountString;

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
            ListViewAdapter(body.getData().getContent());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ListViewAdapter(ArrayList<BookModel.Data> data) {
        // now create an adapter class
        //Log.e("LISTVIEW","ADD");
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
