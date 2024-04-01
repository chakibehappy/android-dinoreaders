package gibbie.dino.readers.ui.activities.register;

import java.util.ArrayList;

import gibbie.dino.readers.ui.activities.register.RegisterModel;

public interface RegisterView {
    void onSuccess(ArrayList<RegisterModel> model);

    void onError(String error);

    void onNoInternet();
}
