package gibbie.dino.readers.ui.fragments.home;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class DashboardModel implements Serializable{
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
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("item_type")
        @Expose
        private String item_type;
        @SerializedName("title_icon")
        @Expose
        private String title_icon;

        @SerializedName("content")
        @Expose
        private ArrayList<BookModel.Data> content;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
        public String getItem_type() {
            return item_type;
        }

        public String getTitle_icon() {
            return title_icon;
        }

        public  ArrayList<BookModel.Data> getContent(){
            return this.content;
        }
    }

}
