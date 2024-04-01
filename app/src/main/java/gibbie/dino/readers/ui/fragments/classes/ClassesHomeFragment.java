package gibbie.dino.readers.ui.fragments.classes;

import static gibbie.dino.readers.retrofitsetup.CommonResponse.isUnauthorized;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonElement;

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
import gibbie.dino.readers.ui.fragments.home.BookModel;
import gibbie.dino.readers.ui.fragments.home.DashboardBookCategoryModel;
import gibbie.dino.readers.ui.fragments.home.DashboardHorizontalRecycleViewAdapter;
import gibbie.dino.readers.ui.fragments.home.DashboardModel;
import gibbie.dino.readers.ui.fragments.home.VerticalRecyclerViewAdapter;
import io.github.muddz.styleabletoast.StyleableToast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClassesHomeFragment extends SuperFragment implements NoInternet, SharedPreferences.OnSharedPreferenceChangeListener, SwipeRefreshLayout.OnRefreshListener{
    ArrayList<DashboardBookCategoryModel> dashboardParentModelArrayList = new ArrayList<>();
    SessionManager sessionManager;
    SwipeRefreshLayout swipe_to_refresh;
    private FragmentClassesHomeBinding binding;
    private RecyclerView parentRecyclerView;
    private RecyclerView.LayoutManager parentLayoutManager;
    private DashboardHorizontalRecycleViewAdapter DashboardAdapter;

    FloatingActionButton mJoinClassFab;
    ExtendedFloatingActionButton mSettingFab;
    TextView joinClassActionText;
    // to check whether sub FABs are visible or not
    Boolean isAllFabsVisible;

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

        binding.clFabCore.setVisibility(View.VISIBLE);
        mSettingFab = binding.settingFab;
        mJoinClassFab = binding.joinClassFab;
        joinClassActionText = binding.joinClassActionText;
        // Now set all the FABs and all the action name
        // texts as GONE
        mJoinClassFab.setVisibility(View.GONE);
        joinClassActionText.setVisibility(View.GONE);
        isAllFabsVisible = false;
        mSettingFab.shrink();
        mSettingFab.setOnClickListener(this::OpenSetting);

        mJoinClassFab.setOnClickListener(this::JoinClassByCode);


    }

    private void OpenSetting(View v) {
        if (!isAllFabsVisible) {
            mJoinClassFab.show();
            joinClassActionText
                    .setVisibility(View.VISIBLE);
            mSettingFab.extend();
            isAllFabsVisible = true;
        } else {
            // when isAllFabsVisible becomes
            // true make all the action name
            // texts and FABs GONE.
            mJoinClassFab.hide();
            joinClassActionText
                    .setVisibility(View.GONE);
            // Set the FAB to shrink after user
            // closes all the sub FABs
            mSettingFab.shrink();
            // make the boolean variable false
            // as we have set the sub FABs
            // visibility to GONE
            isAllFabsVisible = false;
        }
    }

    public void JoinClassByCode(View v){
        Dialog d_joinClass = new Dialog(getContext());
        d_joinClass.setContentView(R.layout.dialog_join_class);
        ImageView closeButton = d_joinClass.findViewById(R.id.iv_dialog_close);
        Button joinButton = d_joinClass.findViewById(R.id.btn_join);
        EditText et_join_class = d_joinClass.findViewById(R.id.et_join_class);
        closeButton.setOnClickListener(view -> d_joinClass.dismiss());
        joinButton.setBackground(getResources().getDrawable(R.drawable.button));
        joinButton.setOnClickListener(view -> requestJoinClassByCode(et_join_class.getText().toString().trim()));

        d_joinClass.show();
    }
    public void requestJoinClassByCode(String classCode){
        if (Functions.checkInternet(getContext())) {
            Functions.showProgressbar(getActivity());
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.ClassesJoinByCode(WebUrl.Accept, WebUrl.ContentType,sessionManager.getAccesstoken(), classCode).enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    Functions.hideProgressbar(getActivity());
                    try {

                        if (response.isSuccessful()) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                String jsonObject_message = jsonObject.getString("message");

                                ToastSuccess(jsonObject_message);

                                refreshScreen();
                            } catch (Exception e) {
                                e.printStackTrace();
                                String errorResponse = response.errorBody().string().trim();
                                JSONObject jsonObject = new JSONObject(errorResponse);
                                String jsonObject_message = jsonObject.getString("message");
                                ToastFail(jsonObject_message);
                            }
                        } else if (isUnauthorized(getActivity(), response.code())) {
                            Log.e("response", "Unauthorized");
                        } else {
                            String errorResponse = response.errorBody().string().trim();
                            JSONObject jsonObject = new JSONObject(errorResponse);
                            String jsonObject_message = jsonObject.getString("message");
                            ToastFail(jsonObject_message);
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
    public void getDashboard() {

        if (Functions.checkInternet(getContext())) {
            Functions.showProgressbar(getActivity());
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.ClassesDashboard(WebUrl.Accept, WebUrl.ContentType,sessionManager.getAccesstoken()).enqueue(new Callback<DashboardModel>() {
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
                                GoToDetail(data.getContent().get(position));
                            }

                            @Override
                            public void onFavouriteClick(int position) {
                                Log.e("FAVOURITE STATS:",data.getContent().get(position).getFavourite().toString());
                                Boolean isFav = data.getContent().get(position).getFavourite();
                                if(isFav)
                                    requestLeaveClass(data.getContent().get(position));
                                else
                                    requestJoinClass(data.getContent().get(position));
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
    private void GoToDetail( BookModel.Data body) {
        Fragment fragment = new ClassesDetailFragment();
        BottomNavigation.fm
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .addToBackStack(null)
                .commit();
        Bundle arguments = new Bundle();
        arguments.putString(Constant.SELECTED_CLASS_ID, body.getId());
        fragment.setArguments(arguments);
    }
    private void requestJoinClass(BookModel.Data body) {
        if (Functions.checkInternet(getContext())) {
            Functions.showProgressbar(getActivity());
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.ClassesJoin(WebUrl.Accept, WebUrl.ContentType,sessionManager.getAccesstoken(), body.getId()).enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    Functions.hideProgressbar(getActivity());
                    try {

                        if (response.isSuccessful()) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                String jsonObject_message = jsonObject.getString("message");

                                ToastSuccess(jsonObject_message);

                                refreshScreen();
                            } catch (Exception e) {
                                e.printStackTrace();
                                String errorResponse = response.errorBody().string().trim();
                                JSONObject jsonObject = new JSONObject(errorResponse);
                                String jsonObject_message = jsonObject.getString("message");
                                ToastFail(jsonObject_message);
                            }
                        } else if (isUnauthorized(getActivity(), response.code())) {
                            Log.e("response", "Unauthorized");
                        } else {
                            String errorResponse = response.errorBody().string().trim();
                            JSONObject jsonObject = new JSONObject(errorResponse);
                            String jsonObject_message = jsonObject.getString("message");
                            ToastFail(jsonObject_message);
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
    private void requestLeaveClass(BookModel.Data body) {
        if (Functions.checkInternet(getContext())) {
            Functions.showProgressbar(getActivity());
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.ClassesLeave(WebUrl.Accept, WebUrl.ContentType,sessionManager.getAccesstoken(), body.getId()).enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    Functions.hideProgressbar(getActivity());
                    try {

                        if (response.isSuccessful()) {
                            try {
                                JSONObject jsonObject = new JSONObject(response.body().toString());
                                String jsonObject_message = jsonObject.getString("message");

                                ToastSuccess(jsonObject_message);

                                refreshScreen();
                            } catch (Exception e) {
                                e.printStackTrace();
                                String errorResponse = response.errorBody().string().trim();
                                JSONObject jsonObject = new JSONObject(errorResponse);
                                String jsonObject_message = jsonObject.getString("message");
                                ToastFail(jsonObject_message);
                            }
                        } else if (isUnauthorized(getActivity(), response.code())) {
                            Log.e("response", "Unauthorized");
                        } else {
                            String errorResponse = response.errorBody().string().trim();
                            JSONObject jsonObject = new JSONObject(errorResponse);
                            String jsonObject_message = jsonObject.getString("message");
                            ToastFail(jsonObject_message);
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
    void ToastSuccess(String Message){
        StyleableToast.makeText(getContext(),Message, Toast.LENGTH_LONG,R.style.AddCollection).show();
    }
    void ToastFail(String Message){
        StyleableToast.makeText(getContext(),Message, Toast.LENGTH_LONG,R.style.RemoveCollection).show();
    }
}
