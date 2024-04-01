package gibbie.dino.readers.ui.fragments.ownstory;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.customlayout.OutlineTextView;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.databinding.FragmentOwnStoryBinding;
import gibbie.dino.readers.databinding.FragmentOwnStoryListBinding;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;
import gibbie.dino.readers.ui.activities.ownstory.CanvasSelection;
import gibbie.dino.readers.ui.activities.placementtest.BookGradeSelectionActivity;
import gibbie.dino.readers.ui.activities.placementtest.PlacementTestActivity;
import gibbie.dino.readers.ui.fragments.profile.ProfileFragment;

public class OwnStoryBookListFragment extends Fragment  {
    private FragmentOwnStoryListBinding binding;
    BottomNavigationView navBar;
    AppCompatButton bt_own_story;

    private RecyclerView recyclerView;

    SessionManager sessionManager;
    SharedPreferences sp;
    final int REQUEST_PERMISSION_CODE = 1000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOwnStoryListBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        navBar = getActivity().findViewById(R.id.nav_view);
        View parent = binding.getRoot().getRootView();

        bt_own_story = parent.findViewById(R.id.bt_own_story);
        bt_own_story.setOnClickListener(v -> openCreateOwnStoryPage());

        recyclerView = view.findViewById(R.id.rv_book);
        recyclerView.setVisibility(View.VISIBLE);

        sp = getActivity().getSharedPreferences("DinoReader", MODE_PRIVATE);
        new OwnStoryBookCheckData().loadStoryBook(getContext(), sp, recyclerView);

        if(!checkPermissionFromDevice())
            requestPermission();

        sessionManager = new SessionManager(getContext());
        getProfileInfo();
        return view;
    }

    private void openCreateOwnStoryPage(){
        Intent intent = new Intent(getActivity(), CanvasSelection.class);
        startActivity(intent);
    }


    private boolean checkPermissionFromDevice() {
        int write_external_storage_result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int record_audio_result = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO);
        return  write_external_storage_result == PackageManager.PERMISSION_GRANTED &&
                record_audio_result == PackageManager.PERMISSION_GRANTED;
    }
    private void requestPermission() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        }, REQUEST_PERMISSION_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(getActivity(), "Permission Granted", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            break;
        }
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
}
