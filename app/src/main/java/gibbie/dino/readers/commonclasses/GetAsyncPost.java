package gibbie.dino.readers.commonclasses;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;

import androidx.appcompat.app.AlertDialog;

import java.lang.ref.WeakReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import gibbie.dino.readers.database.SessionManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Gibbie on 5/25/2021.
 */

public abstract class GetAsyncPost extends AsyncTask<Response, Void, Response> {

    String url = "";
    RequestBody requestBody;
    Context context;
    WeakReference<Context> contextWeakReference;
    AlertDialog.Builder builder;
    ProgressDialog progressDialog;
    String auth_key;
    SessionManager sessionManager;

    public GetAsyncPost(Context context, String url, RequestBody requestBody, String auth_key) {

        this.url = url;
        this.auth_key = auth_key;
        this.requestBody = requestBody;
        contextWeakReference = new WeakReference<Context>(context);
        this.context = contextWeakReference.get();
        this.sessionManager=new SessionManager(context);

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected Response doInBackground(Response... params) {
        Response jsonData = null;

        try {

            try {

                OkHttpClient client = new OkHttpClient.Builder().connectTimeout(50, TimeUnit.SECONDS).writeTimeout(50, TimeUnit.SECONDS)
                        .readTimeout(50, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(true)
                        .build();
                Request request = new Request.Builder()
                        .post(requestBody)
                       .addHeader("Authorization",sessionManager.getAccesstoken())
                        .addHeader("Accept","application/json")
                        .url(url)
                        .build();


                try {

                    Response responses = client.newCall(request).execute();
                    jsonData = responses;
//                    jsonData = responses.body().string();
                } catch (SocketTimeoutException ex) {
                    dismissDialog();
                  //  showDialog();
                } catch (ConnectException ex) {
                    dismissDialog();
                   // showDialog();
                } catch (Exception ex) {
                    dismissDialog();
                    ex.printStackTrace();
                }

            } catch (Exception ex) {
                dismissDialog();
                ex.printStackTrace();
            }
        } catch (Exception e) {
            dismissDialog();
            e.printStackTrace();
        }

        return jsonData;

    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);


    }

    @Override
    protected void onPostExecute(Response result) {
        super.onPostExecute(result);

        if (result != null) {

            getValueParse(result);

          } else {

        }
    }

    public abstract void getValueParse(Response listValue);

    public abstract void retry();

    private void dismissDialog() {

        try {

            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();

        } catch (final IllegalArgumentException e) {

            // Handle or log or ignore
        } catch (final Exception e) {

            // Handle or log or ignore

        }
    }

    private void showDialog() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {


            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Light_Dialog_Alert);

        } else {

            builder = new AlertDialog.Builder(context);

        }

        builder.setTitle("Warning");
        builder.setMessage("Please check your internet connection");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        retry();

                    }
                });

        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                if (context != null) {
                    AlertDialog alert11 = builder.create();
                    alert11.show();
                }
            }
        });
    }
}