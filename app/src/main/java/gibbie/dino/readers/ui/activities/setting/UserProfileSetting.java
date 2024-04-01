package gibbie.dino.readers.ui.activities.setting;

import java.util.List;

public class UserProfileSetting {
    private int id;
    private int profile_id;
    private boolean auto_correct_own_story;
    private boolean book_by_reading_level;
    private boolean recommend_by_reading_level;
    private boolean set_limit_time;
    private String reading_day;
    private String calculated_by;
    private int reading_time;
    private List<Profile> profile;

    public int getId() {
        return id;
    }

    public int getProfileId() {
        return profile_id;
    }

    public boolean isAutoCorrectOwnStory() {
        return auto_correct_own_story;
    }

    public void setAutoCorrectOwnStory(boolean auto_correct_own_story) {
        this.auto_correct_own_story = auto_correct_own_story;
    }

    public boolean isBookByReadingLevel() {
        return book_by_reading_level;
    }

    public void setBookByReadingLevel(boolean book_by_reading_level) {
        this.book_by_reading_level = book_by_reading_level;
    }

    public boolean isRecommendByReadingLevel() {
        return recommend_by_reading_level;
    }

    public void setRecommendByReadingLevel(boolean recommend_by_reading_level) {
        this.recommend_by_reading_level = recommend_by_reading_level;
    }

    public boolean isSetLimitTime() {
        return set_limit_time;
    }

    public void setEnableLimitTime(boolean set_limit_time) {
        this.set_limit_time = set_limit_time;
    }

    public String getReadingDay() {
        return reading_day;
    }

    public void setReadingDay(String reading_day) {
        this.reading_day = reading_day;
    }

    public String getCalculatedBy() {
        return calculated_by;
    }

    public void setCalculatedBy(String calculated_by) {
        this.calculated_by = calculated_by;
    }

    public int getReadingTime() {
        return reading_time;
    }

    public void setReadingTime(int reading_time) {
        this.reading_time = reading_time;
    }

    public List<Profile> getProfile() {
        return profile;
    }

    public void setProfile(List<Profile> profile) {
        this.profile = profile;
    }

    public static class Profile {
        private String name;
        private String img_url;
        public String getName() {
            return name;
        }
        public String getImgUrl() {
            return img_url;
        }
    }
}
