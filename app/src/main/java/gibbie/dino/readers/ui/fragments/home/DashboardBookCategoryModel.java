package gibbie.dino.readers.ui.fragments.home;

public class DashboardBookCategoryModel {
    private final String bookCategory;
    private final DashboardModel.Data books;
    private final String bookCategoryIcon;

    public DashboardBookCategoryModel(String bookCategory,String bookCategoryIcon, DashboardModel.Data books) {
        this.bookCategory = bookCategory;
        this.bookCategoryIcon = bookCategoryIcon;
        this.books = books;
    }

    public String bookCategory() {
        return bookCategory;
    }
    public String bookCategoryIcon() {
        return bookCategoryIcon;
    }

    public DashboardModel.Data books() {
        return books;
    }
}