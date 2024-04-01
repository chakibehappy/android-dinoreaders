package gibbie.dino.readers.ui.activities.placementtest;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JSONConversionTask extends AsyncTask<String, Void, String> {
    private static final String TAG = "JSONConversionTask";

    @Override
    protected String doInBackground(String... urls) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(urls[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }

            String json = builder.toString();
            return convertJSON(json);
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Error converting JSON", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing reader", e);
                }
            }
        }
        return null;
    }

    private String convertJSON(String json) throws JSONException {
        JSONObject originalObject = new JSONObject(json);

        // Create the new JSON object
        JSONObject newObject = new JSONObject();
        newObject.put("success", true);
        newObject.put("title", "Foxy Joxy Plays A Trick");
        newObject.put("folder_name", "foxy-joxy-plays-a-trick");
        newObject.put("total_page", "18");
        newObject.put("thumbnail", "https://sittingduckgames.com/aviationlearn/dino-reader/book/foxy-joxy-plays-a-trick/images/foxy-joxy-plays-a-trick_english_20170320_cover.jpg");
        newObject.put("created_at", "2023-01-12 20:59:51");

        JSONArray pagesArray = originalObject.getJSONArray("pages");
        JSONArray newPagesArray = new JSONArray();
        for (int i = 0; i < pagesArray.length(); i++) {
            JSONObject pageObject = pagesArray.getJSONObject(i);
            JSONObject newPageObject = new JSONObject();

            // Set page ID
            newPageObject.put("id", String.valueOf(i + 1));

            // Set page text
            String text = pageObject.getString("text");
            newPageObject.put("text", text);

            // Set is_story_page
            boolean isStoryPage = i == 0; // Assuming the first page is the story page
            newPageObject.put("is_story_page", isStoryPage);

            // Set image URL, image width, and image height
            String imgUrl = pageObject.getString("imgUrl");
            newPageObject.put("image", imgUrl);
            newPageObject.put("image_width", "567");
            newPageObject.put("image_height", "567");

            // Set audio URL
            newPageObject.put("audio_url", "");

            newPagesArray.put(newPageObject);
        }
        newObject.put("pages", newPagesArray);

        return newObject.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        // Handle the converted JSON string here
        Log.d(TAG, result);
    }
}
