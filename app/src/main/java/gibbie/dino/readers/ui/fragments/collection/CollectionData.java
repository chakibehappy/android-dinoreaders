package gibbie.dino.readers.ui.fragments.collection;

import java.io.Serializable;
import java.util.List;

public class CollectionData implements Serializable {
    public int id;
    public String name;
    public String description;
    public int visibility;
    public int status;
    public String created_at;
    public String date_of_creation;
    public int created_by;
    public String created_by_user;
    public String short_description;
    public List<Book> books;
    public List<CollectionBook> collection_books;
    public List<Object> organization; // You can replace "Object" with appropriate class if needed
    public Profile profiles;

    public class Book  implements Serializable{
        public int id;
        public String author;
        public String title;
        public String lang;
        public String description;
        public String read_url;
        public String image_url;

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getImage_url() {
            return image_url;
        }
    }

    public class CollectionBook {
        public int id;
        public int collection_id;
        public int book_id;
    }

    public class Profile {
        public int id;
        public String name;
        public String img_url;
        public Object dob; // You can replace "Object" with appropriate class if needed
        public int reading_level;
        public String grl;
        public int dra;
        public int max_created_story;
        public Object created_at; // You can replace "Object" with appropriate class if needed
        public Object updated_at; // You can replace "Object" with appropriate class if needed
        public Object deleted_at; // You can replace "Object" with appropriate class if needed
        public Object created_by; // You can replace "Object" with appropriate class if needed
        public Object updated_by; // You can replace "Object" with appropriate class if needed
        public Object deleted_by; // You can replace "Object" with appropriate class if needed
        public Pivot pivot;

        public class Pivot {
            public int collection_id;
            public int profile_id;
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Book> getBooks() {
        return books;
    }

    public String getShort_description() {
        return short_description;
    }

    public String getDescription() {
        return description;
    }
}
