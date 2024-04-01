package gibbie.dino.readers.ui.activities.register;

import android.app.Activity;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.retrofitsetup.ServiceGenerator;
import gibbie.dino.readers.retrofitsetup.WebServices;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import okhttp3.ResponseBody;
import java.lang.annotation.Annotation;

import static gibbie.dino.readers.retrofitsetup.CommonResponse.getErrorMSG;
import static gibbie.dino.readers.retrofitsetup.CommonResponse.isUnauthorized;

public class RegisterPresenterImplementation implements RegisterPresenter {
    RegisterView myView;
    ArrayList<RegisterModel> responselist;
    Activity activity;


    RegisterPresenterImplementation(Activity activity, RegisterView registerView)
    {
        this.activity = activity;
        this.myView = registerView;

    }
    @Override
    public void registration(String name, String email, String password) {
        if (Functions.checkInternet(activity))
        {

            Functions.showProgressbar(activity);
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.Register(WebUrl.Accept, WebUrl.ContentType,name, email, password).enqueue(new Callback<RegisterModel>() {
                @Override
                public void onResponse(Call<RegisterModel> call, Response<RegisterModel> response) {
                    Functions.hideProgressbar(activity);
                    try {
                        if (response.isSuccessful()) {
                            parseResponse(response);
                        }
                        else if (isUnauthorized(activity, response.code())) {
                            Log.e("response", "Unauthorized"+(response.code()));
                        }
                        else if (response.code() == 422) {

                            parseError(response);
                        }
                        else if (response.code() == 500) {
                            myView.onError("Server is busy at this time Please try again.");
                        } else {
                            myView.onError("Oops something went wrong!Please try again");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        myView.onError(e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<RegisterModel> call, Throwable t) {
                    Functions.hideProgressbar(activity);
                    myView.onError(getErrorMSG(t));

                }
            });




        }

        else
        {
            myView.onNoInternet();
        }
    }

    private void parseResponse(Response<RegisterModel> response) throws Exception
    {
        Log.e("Responseeeeeeeeeeee", ">>>>>>>>>>>>>>" + response.body().toString());
        responselist = new ArrayList<>();
        responselist.clear();
        RegisterModel model = response.body();
        if (model.getSuccess())
        {
            responselist.add(model);
            myView.onSuccess(responselist);
        }
        else {
            myView.onError(model.getMessage());
        }
    }
    private  void parseError(Response<RegisterModel> response) throws Exception
    {

        responselist = new ArrayList<>();
        responselist.clear();
        JSONObject jsonObject = null;

        //try {
            String responses = response.errorBody().string();
            Log.e("Responseeeeeeeeeeee", ">>>>>>>>>>>>>>" + responses);
            jsonObject = new JSONObject(responses);
            String email = jsonObject.getJSONObject("errors").isNull("email")?"":jsonObject.getJSONObject("errors").getString("email");

            String password =
                    jsonObject.getJSONObject("errors").isNull("password")
                    ?"":
                    jsonObject.getJSONObject("errors").getString("password");
            String final_message = email+password;
            myView.onError(final_message);
//        } catch (JSONException e) {
//            e.printStackTrace();
//            myView.onError(e.getMessage());
//        }


    }
}
