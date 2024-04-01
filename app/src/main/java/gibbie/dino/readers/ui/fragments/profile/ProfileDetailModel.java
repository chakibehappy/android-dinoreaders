package gibbie.dino.readers.ui.fragments.profile;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import gibbie.dino.readers.retrofitsetup.WebUrl;

public class ProfileDetailModel {
    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private Data data;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Data getData() {
        return data;
    }
    public class Data implements Serializable {

        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("img_url")
        @Expose
        private String img_url;
        @SerializedName("dob")
        @Expose
        private String dob;
        @SerializedName("age")
        @Expose
        private String age;

        private int local_image;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getImg_url() {
            if(img_url.contains("http"))
                return img_url;
            else
                return WebUrl.BASEURL + img_url;
        }

        public void setImg_url(String img_url) {
            this.img_url = img_url;
        }

        public String getAge() {
            return age;
        }

        public void setAge(String age) {
            this.age = age;
        }
        public String getDob() {
            return dob;
        }

        public void setDob(String dob) {
            this.dob = dob;
        }

        public int getLocal_image() {
            return local_image;
        }

        public void setLocal_image(int local_image) {
            this.local_image = local_image;
        }
    }
}
