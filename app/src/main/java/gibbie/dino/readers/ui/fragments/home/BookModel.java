package gibbie.dino.readers.ui.fragments.home;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

import gibbie.dino.readers.retrofitsetup.WebUrl;

public class BookModel {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private ArrayList<Data> data;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public ArrayList<Data> getData() {
        return data;
    }

    public class Data  implements Serializable {
        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("title")
        @Expose
        private String title;

        @SerializedName("description")
        @Expose
        private String description;
        @SerializedName("image_url")
        @Expose
        private String image_url;
        @SerializedName("favourite")
        @Expose
        private  Boolean favourite;
        @SerializedName("author")
        @Expose
        private  String author;
        @SerializedName("read_to_me")
        @Expose
        private  Boolean read_to_me;
        @SerializedName("categories")
        @Expose
        private ArrayList<Categories> categories;
        @SerializedName("level")
        @Expose
        private ArrayList<Categories> level;
        @SerializedName("read_url")
        @Expose
        private String read_url;
        @SerializedName("is_own_story")
        @Expose
        private  Boolean is_own_story;
        @SerializedName("reading_level")
        @Expose
        private  String reading_level;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getBaseAndImage_url() {
            return WebUrl.BASEURL + image_url;
        }

        public String getImg_url_only() {
            if (image_url == null)
                return null;
            if(image_url.contains("http"))
                return image_url;
            return getBaseAndImage_url();
        }

        public void setImage_url(String image_url) {
            this.image_url = image_url;
        }

        public Boolean getFavourite() {
            return favourite;
        }

        public void setFavourite(Boolean favourite) {
            this.favourite = favourite;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }
        public Boolean getRead_to_me() {
            return read_to_me;
        }

        public void setRead_to_me(Boolean read_to_me) {
            this.read_to_me = read_to_me;
        }

        public ArrayList<Categories> getCategories() {
            return categories;
        }
        public ArrayList<Categories> getLevel() {
            if(level == null)
                level = new ArrayList<>();
            return level;
        }
        public String getRead_url() { return WebUrl.BASEURL +read_url;  }
        public void setRead_url(String image_url) {
            this.read_url = read_url;
        }

        public Boolean getIs_own_story() { return is_own_story; }

        public String getReading_level() { return reading_level; }

        public class Categories implements Serializable{
            @SerializedName("name")
            @Expose
            private String name;
            @SerializedName("color")
            @Expose
            private String color;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getColor() {
                return color;
            }

            public void setColor(String color) {
                this.color = color;
            }

        }

    }
}
