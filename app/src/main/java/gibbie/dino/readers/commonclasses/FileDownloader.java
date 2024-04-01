package gibbie.dino.readers.commonclasses;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class FileDownloader {

    private static final String TAG = "FileDownloader";

    private Context context;
    private String saveFolder;
    private FileDownloadListener listener;

    public FileDownloader(Context context, String saveFolder, FileDownloadListener listener) {
        this.context = context;
        this.saveFolder = saveFolder;
        this.listener = listener;
    }

    public FileDownloader downloadFile(String fileUrl) {
        new DownloadTask().execute(fileUrl);
        return null;
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {
            String fileUrl = params[0];
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            String uid = UUID.randomUUID().toString();
            String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1);

            String folderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
            String appName = "DinoReader";
            folderPath = folderPath + File.separator + appName + File.separator + saveFolder;

            // Create a File object with the folder path
            File folder = new File(folderPath);

            // Check if the folder does not exist, then create it
            if (!folder.exists()) {
                folder.mkdirs();
            }
            String savePath = folderPath + File.separator + uid + "." + fileExtension;

            try {
                URL url = new URL(fileUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                FileOutputStream output = new FileOutputStream(savePath);
                byte[] buffer = new byte[1024];
                int length;
                int downloadedBytes = 0;
                while ((length = input.read(buffer)) > 0) {
                    output.write(buffer, 0, length);
                    downloadedBytes += length;
                    publishProgress(downloadedBytes);
                }
                output.flush();
                output.close();
                input.close();
                connection.disconnect();
                return savePath;
            } catch (Exception e) {
                Log.e(TAG, "Failed to download file: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Update progress if needed
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                listener.onFileDownloaded(result);
            } else {
                listener.onFileDownloadFailed();
            }
        }
    }

    public interface FileDownloadListener {
        void onFileDownloaded(String filePath);
        void onFileDownloadFailed();
    }
}
