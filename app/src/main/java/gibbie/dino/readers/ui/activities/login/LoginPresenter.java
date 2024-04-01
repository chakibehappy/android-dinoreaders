package gibbie.dino.readers.ui.activities.login;

public interface LoginPresenter
{
    void doLogin(String username, String password);
    void doLoginGoogle(String access_token);
    void doLoginFacebook(String access_token);
}
