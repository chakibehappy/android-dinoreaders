package gibbie.dino.readers.ui.activities.logout;

import java.util.ArrayList;

public interface LogoutView {
    void onSuccess(ArrayList<LogoutModel> list);

    void onError(String error);

    void onNoInternet();

    void onNoInternetLog();
}
