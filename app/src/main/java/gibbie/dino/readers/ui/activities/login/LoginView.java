package gibbie.dino.readers.ui.activities.login;

import java.util.ArrayList;

public interface LoginView {
    void onSuccess(ArrayList<LoginModel> model);

    void onError(String error);

    void onNoInternet();

}
