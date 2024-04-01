package gibbie.dino.readers.ui.fragments.singlebook;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.fragments.home.BookModel;

public class SingleBookModel {
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
    public class Data {
        @SerializedName("id")
        @Expose
        private String id;
        @SerializedName("uid")
        @Expose
        private String uid;
        @SerializedName("title")
        @Expose
        private String title;

        @SerializedName("description")
        @Expose
        private String description;
        @SerializedName("image_url")
        @Expose
        private String image_url;
        @SerializedName("read_url")
        @Expose
        private String read_url;
        @SerializedName("author")
        @Expose
        private  String author;
        @SerializedName("categories")
        @Expose
        private ArrayList<BookModel.Data.Categories> categories;
        @SerializedName("level")
        @Expose
        private ArrayList<BookModel.Data.Categories> level;
        @SerializedName("reading_level")
        @Expose
        private  String reading_level;
        @SerializedName("pages")
        @Expose
        private ArrayList<Pages> pages;

        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getUid() {
            return uid;
        }
        public void setUid(String Uid) {
            this.uid = uid;
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
        public String getImage_url() {
            if(image_url.contains("http"))
                return image_url;
            return WebUrl.BASEURL +image_url;
        }
        public void setImage_url(String image_url) {
            this.image_url = image_url;
        }
        public String getRead_url() { return WebUrl.BASEURL +read_url;  }
        public void setRead_url(String image_url) {
            this.read_url = read_url;
        }
        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }
        public ArrayList<BookModel.Data.Categories> getCategories() {
            return categories;
        }

        public ArrayList<BookModel.Data.Categories> getLevel() {
            if(level == null)
                level = new ArrayList<>();
            return level;
        }

        public String getReading_level() { return reading_level; }

        public void setPages(ArrayList<Pages> pages) { this.pages = pages; }
        public ArrayList<Pages> getPages() { return pages; }

        public class Categories implements Serializable {
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

        public class Pages implements Serializable {
            @SerializedName("page_number")
            @Expose
            private int page_number;
            @SerializedName("play_audio")
            @Expose
            private int play_audio;
            @SerializedName("show_pages")
            @Expose
            private int show_pages;

            public int getPage_number() {
                return page_number;
            }

            public void setPage_number(int page_number) {
                this.page_number = page_number;
            }

            public int getPlay_audio() {
                return play_audio;
            }

            public void setPlay_audio(int play_audio) {
                this.page_number = play_audio;
            }

            public int getShow_pages() {
                return show_pages;
            }

            public void setShow_pages(int show_pages) {
                this.page_number = show_pages;
            }

        }

    }
}
