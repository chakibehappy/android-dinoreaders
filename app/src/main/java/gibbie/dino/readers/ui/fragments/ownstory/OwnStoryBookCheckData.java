package gibbie.dino.readers.ui.fragments.ownstory;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gibbie.dino.readers.commonclasses.FileDownloader;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.retrofitsetup.WebUrl;

public class OwnStoryBookCheckData {

    String bookListJson;
    boolean isReloadData = false;

    public void loadStoryBook(Context context, SharedPreferences sp, RecyclerView recyclerView){
        SessionManager sessionManager = new SessionManager(context);
        bookListJson = sp.getString("myOwnStoryBook", null) ;

        if(Functions.checkInternet(context)){
            JSONObject request = new JSONObject();
            JsonObjectRequest jsArrayRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    WebUrl.OWNSTORYURL + sessionManager.getUserId(),
                    request,
                    new com.android.volley.Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if(response.getBoolean("success")){
                                    String webBookListJson = response.getString("data");
                                    Log.d("TAG", "on web: " + webBookListJson);
                                    Log.d("TAG", "on local: " + bookListJson);

                                    Gson gson = new Gson();
                                    Type listType = new TypeToken<List<OwnStoryBookModel>>() {}.getType();
                                    List<OwnStoryBookModel> webSavedBooks = gson.fromJson(webBookListJson, listType);
                                    List<OwnStoryBookModel> localSavedBooks = new ArrayList<>();

                                    if(bookListJson != null){
                                        localSavedBooks = gson.fromJson(bookListJson, listType);
                                    }

                                    // check by book counts :
                                    if(localSavedBooks.size() > 0){
                                        if(localSavedBooks.size() == webSavedBooks.size()){
                                            //boolean isEqual = false;
                                            for (int i = 0; i < localSavedBooks.size(); i++) {
                                                if (localSavedBooks.get(i).getId() != webSavedBooks.get(i).getId()){
                                                    localSavedBooks.get(i).setId(webSavedBooks.get(i).getId());
                                                }
                                                if (!localSavedBooks.get(i).getTitle().equals(webSavedBooks.get(i).getTitle())){
                                                    localSavedBooks.get(i).setTitle(webSavedBooks.get(i).getTitle());
                                                }
                                                if (!localSavedBooks.get(i).getCover().equals(webSavedBooks.get(i).getCover())){
                                                    String url = webSavedBooks.get(i).getCover();
                                                    localSavedBooks.get(i).setCover(url);
                                                    deleteFile(localSavedBooks.get(i).getCoverLocalPath());
                                                    localSavedBooks.get(i).setCoverPath(null);
                                                    downloadAndUpdateCover(localSavedBooks, i, url, sp, context);
                                                }
                                                if (!localSavedBooks.get(i).getAudioUrl().equals(webSavedBooks.get(i).getAudioUrl())){
                                                    String url = webSavedBooks.get(i).getAudioUrl();
                                                    localSavedBooks.get(i).setAudioUrl(url);
                                                    deleteFile(localSavedBooks.get(i).getLocalAudioUrl());
                                                    localSavedBooks.get(i).setLocalAudioUrl(null);
                                                    downloadAndUpdateAudio(localSavedBooks, i, url, sp, context);
                                                }

                                                for(int j = 0; j < localSavedBooks.get(i).getPages().size(); j++){
                                                    OwnStoryPageModel localPage = localSavedBooks.get(i).getPage(j);
                                                    OwnStoryPageModel webPage = webSavedBooks.get(i).getPage(j);
                                                    if(!localPage.getStory().equals(webPage.getStory())){
                                                        localPage.setStory(webPage.getStory());
                                                    }
                                                    if(!localPage.getAudioUrl().equals(webPage.getAudioUrl())){
                                                        localPage.setAudioUrl(webPage.getAudioUrl());
                                                        deleteFile(localPage.getLocalAudioUrl());
                                                        String url = webSavedBooks.get(i).getPage(j).getAudioUrl();
                                                        downloadAndUpdatePageAudio(localSavedBooks, i, j, url, sp, context);
                                                    }
                                                }
                                            }
                                            bookListJson = new Gson().toJson(localSavedBooks);
                                            saveLocalBooks(sp, localSavedBooks);
                                        }
                                        else{
                                            for (int i = 0; i < localSavedBooks.size(); i++) {
                                                deleteFile(localSavedBooks.get(i).getCoverLocalPath());
                                                deleteFile(localSavedBooks.get(i).getLocalAudioUrl());
                                                for(int j = 0; j < localSavedBooks.get(i).getPages().size(); j++){
                                                    OwnStoryPageModel localPage = localSavedBooks.get(i).getPage(j);
                                                    deleteFile(localPage.getLocalAudioUrl());
                                                }
                                            }
                                            bookListJson = webBookListJson;
                                            isReloadData = true;
                                        }
                                    }
                                    else{
                                        // local books is empty
                                        bookListJson = webBookListJson;
                                        isReloadData = true;
                                    }
                                    loadOwnStoryBook(recyclerView, (Activity) context, sp);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
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

            RequestQueue mRequestQueue = Volley.newRequestQueue(context);
            String TAG_JSON = "json_obj_req";
            jsArrayRequest.setTag(TAG_JSON);
            jsArrayRequest.setRetryPolicy(new DefaultRetryPolicy(0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mRequestQueue.add(jsArrayRequest);
        }
        else {
            loadOwnStoryBook(recyclerView, (Activity) context, sp);
        }
    }


    private  void  loadOwnStoryBook(RecyclerView recyclerView, Activity activity, SharedPreferences sp){
        List<OwnStoryBookModel> bookList = new ArrayList<>();
        if (!isReloadData){
            if(bookListJson != null){
                Gson gson = new Gson();
                Type listType = new TypeToken<List<OwnStoryBookModel>>() {}.getType();
                bookList = gson.fromJson(bookListJson, listType);
            }
        }
        else
        {
            if(bookListJson != null) {
                JSONArray jArray = null;
                try {
                    jArray = new JSONArray(bookListJson);
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject jsonObject = jArray.getJSONObject(i);
                        JSONArray bookPage = jsonObject.getJSONArray("pages");
                        List<OwnStoryPageModel> pages = new ArrayList<>();

                        for (int j = 0; j < bookPage.length(); j++) {
                            JSONObject pageObject = bookPage.getJSONObject(j);
                            pages.add(new OwnStoryPageModel(
                                    pageObject.getInt("id"),
                                    pageObject.getInt("page_number"),
                                    pageObject.getString("page_story"),
                                    pageObject.getString("page_audio_url")
                            ));
                            downloadAndUpdatePageAudio(bookList, i, j, pageObject.getString("page_audio_url"), sp, activity);
                        }
                        bookList.add(new OwnStoryBookModel(
                                jsonObject.getInt("id"),
                                jsonObject.getInt("template_id"),
                                jsonObject.getString("title"),
                                jsonObject.getString("cover"),
                                pages,
                                jsonObject.getString("audio_url")
                        ));
                        downloadAndUpdateCover(bookList, i, jsonObject.getString("cover"), sp, activity);
                        downloadAndUpdateAudio(bookList, i, jsonObject.getString("audio_url"), sp, activity);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            saveLocalBooks(sp, bookList);
        }

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        OwnStoryBookListAdapter adapter = new OwnStoryBookListAdapter(activity, bookList);
        recyclerView.setAdapter(adapter);
    }

    public void saveLocalBooks(SharedPreferences sp, List<OwnStoryBookModel> userBooks){
        String myOwnStoryListJson = new Gson().toJson(userBooks);
        sp.edit().putString("myOwnStoryBook", myOwnStoryListJson).apply();
    }

    public void deleteFile(String path){
        if(path == null || path.equals(""))
            return;

        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    public void downloadAndUpdateCover(List<OwnStoryBookModel> localSavedBooks, int i, String url, SharedPreferences sp, Context context){
        FileDownloader imageDownloader = new FileDownloader(context, "image", new FileDownloader.FileDownloadListener() {
            @Override
            public void onFileDownloaded(String filePath) {
                localSavedBooks.get(i).setCoverPath(filePath);
                saveLocalBooks(sp, localSavedBooks);
            }
            @Override
            public void onFileDownloadFailed() {}
        });
        imageDownloader.downloadFile(url);
    }

    public void downloadAndUpdateAudio(List<OwnStoryBookModel> localSavedBooks, int i, String url, SharedPreferences sp, Context context){
        FileDownloader audioDownloader = new FileDownloader(context, "audio", new FileDownloader.FileDownloadListener() {
            @Override
            public void onFileDownloaded(String filePath) {
                localSavedBooks.get(i).setLocalAudioUrl(filePath);
                saveLocalBooks(sp, localSavedBooks);
            }
            @Override
            public void onFileDownloadFailed() {}
        });
        audioDownloader.downloadFile(url);
    }

    public void downloadAndUpdatePageAudio(List<OwnStoryBookModel> localSavedBooks, int i, int j, String url, SharedPreferences sp, Context context){
        FileDownloader audioDownloader = new FileDownloader(context, "audio", new FileDownloader.FileDownloadListener() {
            @Override
            public void onFileDownloaded(String filePath) {
                localSavedBooks.get(i).getPage(j).setPageLocalAudioUrl(filePath);
                saveLocalBooks(sp, localSavedBooks);
            }
            @Override
            public void onFileDownloadFailed() {}
        });
        audioDownloader.downloadFile(url);
    }
}

