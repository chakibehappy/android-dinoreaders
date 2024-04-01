package gibbie.dino.readers.ui.fragments.home;

public class BookCategoryModel {
    private final String bookCategory;
    private final BookModel books;

    public BookCategoryModel(String bookCategory, BookModel books) {
        this.bookCategory = bookCategory;
        this.books = books;
    }

    public String bookCategory() {
        return bookCategory;
    }

    public BookModel books() {
        return books;
    }
}
