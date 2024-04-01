package gibbie.dino.readers.ui.activities.register;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RegisterModel {
    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private RegisterModel.Data data;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("errors")
    @Expose
    private RegisterModel.Errors errors;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public RegisterModel.Data getData() {
        return data;
    }

    public void setData(RegisterModel.Data data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    public RegisterModel.Errors getErrors() {
        return errors;
    }
    public void setErrors(RegisterModel.Errors errors) {
        this.errors = errors;
    }
    public class Data {

        @SerializedName("setting_password")
        @Expose
        private String settingPassword;

        @SerializedName("access_token")
        @Expose
        private String accessToken;

        @SerializedName("token_type")
        @Expose
        private String tokenType;
        @SerializedName("expires_at")
        @Expose
        private String expiresAt;
        @SerializedName("profile_image")
        @Expose
        private String profile_image;
        @SerializedName("profile_id")
        @Expose
        private String profile_id;
        @SerializedName("user_id")
        @Expose
        private String user_id;

        public String getSettingPassword() {
            return settingPassword;
        }

        public void setSettingPassword(String settingPassword) {
            this.settingPassword = settingPassword;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        public String getExpiresAt() {
            return expiresAt;
        }

        public void setExpiresAt(String expiresAt) {
            this.expiresAt = expiresAt;
        }

        public String getProfile_image() {
            return profile_image;
        }

        public void setProfile_image(String profile_image) {
            this.profile_image = profile_image;
        }

        public String getProfile_id() {
            return profile_id;
        }

        public String getUserId() {
            return user_id;
        }

        public void setUserId(String user_id) {
            this.user_id = user_id;
        }

    }
    public class Errors{
        @SerializedName("email")
        @Expose
        private String email;
        @SerializedName("password")
        @Expose
        private String password;

        public String All(){ return this.email + this.password;}
    }
}