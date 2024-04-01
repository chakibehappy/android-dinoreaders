package gibbie.dino.readers.ui.activities.placementtest;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JsonReaderTask extends AsyncTask<String, Void, String> {

    private static final String TAG = "TAG";

    private JsonReaderListener listener;

    public JsonReaderTask(JsonReaderListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        String urlString = params[0];
        InputStream inputStream = null;
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            inputStream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }

            return builder.toString();
        } catch (IOException e) {
            Log.e(TAG, "Error reading JSON from URL: " + e.getMessage());
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing resources: " + e.getMessage());
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) {
            try {
                Gson gson = new Gson();
                Book book = gson.fromJson(result, Book.class);

                if (listener != null) {
                    listener.onJsonRead(book);
                }
            } catch (JsonSyntaxException e) {
                Log.e(TAG, "Error parsing JSON: " + e.getMessage());
            }
        } else {
            Log.e(TAG, "Failed to get JSON from URL");
        }
    }

    public interface JsonReaderListener {
        void onJsonRead(Book json);
    }
}

