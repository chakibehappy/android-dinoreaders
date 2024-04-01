package gibbie.dino.readers.ui.fragments.readbook;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.SuperFragment;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.databinding.FragmentReadBookBinding;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.activities.bottomnav.BottomNavigation;
import gibbie.dino.readers.ui.activities.setting.ReadingTimeCounter;

public class ReadBookFragment extends SuperFragment {
    private FragmentReadBookBinding binding;
    private WebView webView;
    private SessionManager sessionManager;
    private String book_id;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentReadBookBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        webView = binding.webView;
        binding.llBack.setOnClickListener(this::goback);
        sessionManager = new SessionManager(getContext());
        LoadBook();
        return view;
    }
    void goback(View view) {
        BottomNavigation.fm.popBackStack();
    }
    @SuppressLint("SetJavaScriptEnabled")
    private void LoadBook(){
        if(getArguments() == null)
            return;
        if(getArguments().containsKey("READ_URL") ) {
            String read_url =  getArguments().getString("READ_URL");
            webView.setWebViewClient(new WebViewClient());
            //Log.e("url",read_url);
            book_id = getArguments().getString("BOOK_ID");

            WebSettings webSettings = webView.getSettings();
            webSettings.setJavaScriptEnabled(true);
            webView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
            webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
            webView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                    Log.d("WebView", consoleMessage.message());
                    return true;
                }
            });
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    Log.e("WebView", "Error: " + description);
                }
            });
            webView.loadUrl(read_url);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        ReadingTimeCounter.stopTimer();
    }

    @Override
    public void onResume() {
        super.onResume();
        ReadingTimeCounter.startTimer(getContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ReadingTimeCounter.stopTimer();

        // Sending the reading progress here :
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("user_id", sessionManager.getUserId());
            requestBody.put("profile_id", sessionManager.getProfileId());
            requestBody.put("book_id", book_id);
            requestBody.put("reading_count", 1);
            requestBody.put("reading_time", sessionManager.getTempReadingTime());
            requestBody.put("reading_score", 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsArrayRequest = new JsonObjectRequest(
                Request.Method.POST,
                WebUrl.SAVEREADINGHISTORYURL,
                requestBody,
                response -> Log.d("TAG", "reading history saved successfully"),
                error -> Log.e("TAG", "Failed to save reading history: " + error.getMessage())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", sessionManager.getAccesstoken());
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
