package gibbie.dino.readers.ui.activities.login;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;

import gibbie.dino.readers.commonclasses.Constant;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.retrofitsetup.ServiceGenerator;
import gibbie.dino.readers.retrofitsetup.WebServices;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static gibbie.dino.readers.retrofitsetup.CommonResponse.getErrorMSG;
import static gibbie.dino.readers.retrofitsetup.CommonResponse.isUnauthorized;

public class LoginPresenterImplementation implements LoginPresenter
{
    LoginView loginView;
    ArrayList<LoginModel> responselist;
    Activity activity;


    LoginPresenterImplementation(Activity activity, LoginView loginView)
    {
        this.activity = activity;
        this.loginView = loginView;

    }

    @Override
    public void doLogin(String username, String password)
    {
        if (Functions.checkInternet(activity))
        {

                Functions.showProgressbar(activity);
                WebServices webServices = ServiceGenerator.createService(WebServices.class);
                webServices.MakeLogin(WebUrl.Accept, WebUrl.ContentType, username, password, Constant.DEVICE_TYPE).enqueue(new Callback<LoginModel>() {
                    @Override
                    public void onResponse(Call<LoginModel> call, Response<LoginModel> response) {
                        Functions.hideProgressbar(activity);
                        try {
                            if (response.isSuccessful()) {
                                parseResponse(response);

                            }
                            else if (isUnauthorized(activity, response.code())) {
                                Log.e("response", "Unauthorized"+(response.code()));
                            }

                            else if (response.code() == 500) {
                                loginView.onError("Server is busy at this time Please try again.");
                            } else {
                                loginView.onError("Oops something went wrong!Please try again");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            loginView.onError(e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginModel> call, Throwable t) {
                        Functions.hideProgressbar(activity);
                        loginView.onError(getErrorMSG(t));

                    }
                });




        }

        else
            {
            loginView.onNoInternet();
        }
    }

    @Override
    public void doLoginFacebook(String access_token)
    {
        if (Functions.checkInternet(activity))
        {

            Functions.showProgressbar(activity);
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.MakeLoginFacebook(WebUrl.Accept, WebUrl.ContentType, access_token).enqueue(new Callback<LoginModel>() {
                @Override
                public void onResponse(Call<LoginModel> call, Response<LoginModel> response) {
                    Functions.hideProgressbar(activity);
                    try {
                        if (response.isSuccessful()) {
                            parseResponse(response);
                        }
                        else if (isUnauthorized(activity, response.code())) {
                            Log.e("response", "Unauthorized"+(response.code()));
                        }

                        else if (response.code() == 500) {
                            loginView.onError("Server is busy at this time Please try again.");
                        } else {
                            loginView.onError("Oops something went wrong!Please try again");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        loginView.onError(e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<LoginModel> call, Throwable t) {
                    Functions.hideProgressbar(activity);
                    loginView.onError(getErrorMSG(t));

                }
            });




        }

        else
        {
            loginView.onNoInternet();
        }
    }
    @Override
    public void doLoginGoogle(String access_token)
    {
        if (Functions.checkInternet(activity))
        {

            Functions.showProgressbar(activity);
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.MakeLoginGoogle(WebUrl.Accept, WebUrl.ContentType, access_token).enqueue(new Callback<LoginModel>() {
                @Override
                public void onResponse(Call<LoginModel> call, Response<LoginModel> response) {
                    Functions.hideProgressbar(activity);
                    try {
                        if (response.isSuccessful()) {
                            parseResponse(response);
                        }
                        else if (isUnauthorized(activity, response.code())) {
                            Log.e("response", "Unauthorized"+(response.code()));
                        }

                        else if (response.code() == 500) {
                            loginView.onError("Server is busy at this time Please try again.");
                        } else {
                            loginView.onError("Oops something went wrong!Please try again");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        loginView.onError(e.getMessage());
                    }
                }

                @Override
                public void onFailure(Call<LoginModel> call, Throwable t) {
                    Functions.hideProgressbar(activity);
                    loginView.onError(getErrorMSG(t));

                }
            });




        }

        else
        {
            loginView.onNoInternet();
        }
    }
    private void parseResponse(Response<LoginModel> response) throws Exception
    {
        //Log.e("Responseeeeeeeeeeee", ">>>>>>>>>>>>>>" + response.body().toString());
        responselist = new ArrayList<>();
        responselist.clear();
        LoginModel loginModel = response.body();
        if (loginModel.getSuccess())
        {
            responselist.add(loginModel);
            loginView.onSuccess(responselist);
        }
        else {
            loginView.onError(loginModel.getMessage());
        }
    }
}
