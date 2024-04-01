package gibbie.dino.readers.ui.fragments.collection;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

import gibbie.dino.readers.ui.fragments.home.BookModel;

public class CollectionDetailModel implements Serializable{
    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("data")
    @Expose
    private CollectionDetailModel.Data data;

    public Boolean getSuccess() {
        return success;
    }

    public CollectionDetailModel.Data getData() {
        return data;
    }
    public class Data  implements Serializable {
        @SerializedName("name")
        @Expose
        private String name;
        @SerializedName("description")
        @Expose
        private String description;


        @SerializedName("books")
        @Expose
        private ArrayList<BookModel.Data> content;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String type) {
            this.description = type;
        }

        public  ArrayList<BookModel.Data> getContent(){
            return this.content;
        };

    }
}
