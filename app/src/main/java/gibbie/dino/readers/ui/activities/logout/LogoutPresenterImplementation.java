package gibbie.dino.readers.ui.activities.logout;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;

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

public class LogoutPresenterImplementation  implements LogoutPresenter {
    Activity activity;
    LogoutView loginView;
    SessionManager sessionManage;
    ArrayList<LogoutModel> list;


    public LogoutPresenterImplementation(Activity activity, LogoutView loginView) {
        this.activity = activity;
        this.loginView = loginView;
        sessionManage = new SessionManager(activity);
    }


    private void parseResponse(Response<LogoutModel> response) {
        list = new ArrayList<>();
        list.clear();

        LogoutModel logoutModel = response.body();
        if (logoutModel.getMessage().equalsIgnoreCase("Successfully logged out"))
        {
            list.add(logoutModel);
            loginView.onSuccess(list);

        } else {
            loginView.onError(logoutModel.getMessage());
        }

    }

    @Override
    public void doLogout(String accept, String authorization)
    {
        if (Functions.checkInternet(activity))
        {
            Functions.showProgressbar(activity);
            WebServices webServices = ServiceGenerator.createService(WebServices.class);
            webServices.makeLogout(WebUrl.Accept, sessionManage.getAccesstoken()).enqueue(new Callback<LogoutModel>() {
                @Override
                public void onResponse(Call<LogoutModel> call, Response<LogoutModel> response) {
                    Functions.hideProgressbar(activity);
                    if (!response.isSuccessful())
                    {
                        loginView.onError("Something went wrong from server side");
                    }

                    else if (isUnauthorized(activity, response.code())) {
                        Log.e("response", "Unauthorized"+(response.code()));
                    }

                    else {
                        parseResponse(response);
                    }
                }

                @Override
                public void onFailure(Call<LogoutModel> call, Throwable t) {
                    Functions.hideProgressbar(activity);
                    loginView.onError(getErrorMSG(t));


                }
            });


        }
        else
        {
            //    loginView.onNoInternet();
            loginView.onNoInternetLog();
        }
    }
}