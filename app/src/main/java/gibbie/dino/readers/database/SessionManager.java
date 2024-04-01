package gibbie.dino.readers.database;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import gibbie.dino.readers.commonclasses.Constant;
import gibbie.dino.readers.ui.activities.readingbuddy.AvatarBackgroundModel;
import gibbie.dino.readers.ui.activities.readingbuddy.AvatarFrameModel;
import gibbie.dino.readers.ui.activities.readingbuddy.ReadingBuddyActivity;
import gibbie.dino.readers.ui.activities.readingbuddy.ReadingBuddyAvatarModel;
import gibbie.dino.readers.ui.activities.setting.UserProfileSetting;

import static gibbie.dino.readers.commonclasses.Functions.stringToDate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class SessionManager {
    Context context;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private static final String PREF_NAME = "affinate";
    private static final int PRIVATE_MODE = 0;
    private static final String ISLOGGED_IN = "isloggedin";
    private static final String ACCESSTOKEN = "accesstoken";
    private static final String EXPIRES_AT = "expires_at";
    private static final String USER_INFO = "userInfo";
    private static final String IS_REMEMBER_ME = "ISREMEMBERME";
    private static final String SAVE_PASS = "savepass";
    private static final String SAVEEMAIL = "saveemail";
    private static final String JOB_TYPE = "jobType";
    private static final String USER_CONTACT_INFO = "user_contact_info";
    private static final String USER_ADDRESS = "user_Address";
    private static final String USER_COMPENSATIONS = "Compensations";
    private static final String NEXT_PAYROLL_DATE = "nextPayrollDate";
    private static final String PROFILE_PICTURE_PATH = "profile_picture_path";
    private static final String CREATE_PROFILE_PICTURE_PATH = "create_profile_picture_path";
    private static final String ACL_PERMISSION = "acl_permission";
    private static final String MENU_LIST = "menu_list";
    private static final String REFERRAL_CODE = "referralCode";
    private static final String INTERNET_CHECK = "internet_check";
    private static final String USER_ID = "user_id";
    private static final String PROFILE_ID = "profile_id";
    private static final String GOOGLE_SIGN_IN = "GOOGLE_SIGN_IN";
    private static final String FACEBOOK_SIGN_IN = "FACEBOOK_SIGN_IN";

    private static final String READING_BUDDY = "readingBuddy";
    private static final String READING_BUDDY_FRAME = "readingBuddyFrame";
    private static final String READING_BUDDY_BG = "readingBuddyBg";
    private static final String READING_BUDDY_POINT = "readingBuddyPoint";

    private static final String READING_TIME = "readingTime";
    private static final String CURRENT_READING_TIME = "currentReadingTime";
    private static final String LAST_ACTIVE_READING_TIME = "lastActiveReadingTime";
    private static final String TEMP_READING_TIME = "tempReadingTime";

    private static final String USER_PROFILE_SETTINGS = "userProfileSettings";
    private static final String SETTING_PASSWORD = "setting_password";

    private static final String HAVE_READING_LEVEL = "have_reading_level";
    private static final String READING_LEVEL = "reading_level";
    public static ArrayList<String> listOfGrade = new ArrayList<>(Arrays.asList(
            "Pre-Reading",
            "Early Emergent",
            "Emergent",
            "Early Fluent",
            "Fluent",
            "Advanced Fluent",
            "Proficient",
            "Independent"
    ));

    private static final String READING_POINTS = "readingPoints";

    public SharedPreferences getSharedPreferencesInstance() {
        return pref;
    }

    public SessionManager(Context context) {
        this.context = context;
        pref = this.context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }


    public void setAclPermission(String acl_data) {
        editor.putString(ACL_PERMISSION, acl_data);
        editor.commit();
    }

    public String getAclPermission() {
        return pref.getString(ACL_PERMISSION, "");
    }

    public void setInternetCheck(boolean status)
    {
        editor.putBoolean(INTERNET_CHECK,status);
        editor.commit();
    }

    public boolean getInternetCheck(){
        return pref.getBoolean(INTERNET_CHECK, false);

    }

    public void setFCMToken(String fcm_token_old)
    {
        editor.putString(Constant.FCM_TOKEN_OLD,fcm_token_old);
        editor.commit();
    }

    public String getFCMToken(){
        return pref.getString(Constant.FCM_TOKEN_OLD, "");
    }

// FCM_TOKEN_CURRENT

    public void setFcmTokenCurrent(String fcm_token)
    {
        editor.putString(Constant.FCM_TOKEN_CURRENT,fcm_token);
        editor.commit();
    }

    public String getFcmTokenCurrent(){
        return pref.getString(Constant.FCM_TOKEN_CURRENT, "");
    }


    public void setMenuList(String menuList) {
        editor.putString(MENU_LIST, menuList);
        editor.commit();
    }

    public String getMenuList() {
        return pref.getString(MENU_LIST, "");
    }


    public void SetLoggedin(boolean isloged) {
        editor.putBoolean(ISLOGGED_IN, isloged);
        editor.commit();
    }

    public void setEmail(String email) {
        editor.putString(SAVEEMAIL, email);
        editor.commit();
    }

    public String getEmail() {
        return pref.getString(SAVEEMAIL, "");
    }

    public String getProfilePicturePath() {
        return pref.getString(PROFILE_PICTURE_PATH, "");
    }


    public void setProfilePicturePath(String picture_path) {
        editor.putString(PROFILE_PICTURE_PATH, picture_path);
        editor.commit();
    }
    public String getCreateProfilePicturePath() {
        return pref.getString(CREATE_PROFILE_PICTURE_PATH, "");
    }

    public void setCreateFilePicturePath(String picture_path) {
        editor.putString(CREATE_PROFILE_PICTURE_PATH, picture_path);
        editor.commit();
    }
    public void setJobType(String jobtype) {
        editor.putString(JOB_TYPE, jobtype);
        editor.commit();
    }

    public String getJobType() {
        return pref.getString(JOB_TYPE, "");
    }


    public boolean IsLoggedIn() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date sessionExpiresDate = stringToDate(pref.getString(EXPIRES_AT, ""), dateFormat);
        if (System.currentTimeMillis() > sessionExpiresDate.getTime()) {
            setAccessToken("");
            setExpiresAt("");
            setUserInfo("");
            return false;
        }

        return pref.getBoolean(ISLOGGED_IN, false);

    }

    public void setPass(String pass) {
        editor.putString(SAVE_PASS, pass);
        editor.commit();
    }

    public String getPassword() {
        return pref.getString(SAVE_PASS, "");
    }

    public void setRememberme(boolean isRememberme) {
        editor.putBoolean(IS_REMEMBER_ME, isRememberme);
        editor.commit();
    }

    public boolean getIsRememberme() {
        return pref.getBoolean(IS_REMEMBER_ME, false);
    }

    public void setSettingPassword(String pass) {
        editor.putString(SETTING_PASSWORD, pass);
        editor.commit();
    }

    public String getSettingPassword() {
        return pref.getString(SETTING_PASSWORD, "123456");
    }

    public void setAccessToken(String accessToken) {
        editor.putString(ACCESSTOKEN, accessToken);
        editor.commit();
    }

    public String getAccesstoken() {
        return pref.getString(ACCESSTOKEN, "");
    }

    public void setUserInfo(String userInfo) {
        editor.putString(USER_INFO, userInfo);
        editor.commit();
    }

    public String getUserInfo() {
        return pref.getString(USER_INFO, "");
    }

    public void setExpiresAt(String expiresAt) {
        editor.putString(EXPIRES_AT, expiresAt);
        editor.commit();
    }

    public String getExpiresAt() {
        return pref.getString(EXPIRES_AT, "");
    }

    public void logOut() {

        String email= getEmail();
        String password=getPassword();
        String fcm_token=getFCMToken();
        String fcm_token_current=getFcmTokenCurrent();
        List<UserProfileSetting> profileSettings = getUserProfileSettings();
        Boolean expiredReadingTime = false;
        int currentReadingTime = getCurrentReadingTime();
        String latstTimeReading = getLastTimeReading();

        boolean remember=getIsRememberme();
        String access_token="";
        if(getInternetCheck())
        {
            access_token=getAccesstoken();
        }

        editor.clear().commit();

        setEmail(email);
        setPass(password);
        setRememberme(remember);
        setAccessToken(access_token);
        setFCMToken(fcm_token);
        setFcmTokenCurrent(fcm_token_current);
        setUserProfileSettings(profileSettings);
        setLastTimeReading(latstTimeReading);
        setCurrentReadingTime(expiredReadingTime? 0 : currentReadingTime);
    }


    public void setUserContactInfo(String userInfo) {
        editor.putString(USER_CONTACT_INFO, userInfo);
        editor.commit();
    }
    public String getUserContactInfo() {
        return pref.getString(USER_CONTACT_INFO, "");
    }


    public void setUserAddress(String userInfo) {
        editor.putString(USER_ADDRESS, userInfo);
        editor.commit();
    }

    public String getUserAddress() {
        return pref.getString(USER_ADDRESS, "");
    }

    public void setCompensations(String data) {
        editor.putString(USER_COMPENSATIONS, data);
        editor.commit();
    }
    public String getCompensations( ){
        return pref.getString(USER_COMPENSATIONS, "");

    }

    public void setPayRolldate(String data) {
        editor.putString(NEXT_PAYROLL_DATE, data);
        editor.commit();
    }
    public String getPayRolldate() {
        return pref.getString(NEXT_PAYROLL_DATE, "");

    }

    public void setReferralCode(String referralCode) {
        editor.putString(REFERRAL_CODE,referralCode).commit();

    }
    public String getReferralCode() {
       return pref.getString(REFERRAL_CODE,"");

    }

    public String getProfileId() {
        return pref.getString(PROFILE_ID, "");
    }

    public void setProfileId(String profile_id) {
        editor.putString(PROFILE_ID,profile_id).commit();
    }

    public String getUserId() {
        return pref.getString(USER_ID, "");
    }

    public void setUserId(String user_id) {
        editor.putString(USER_ID,user_id).commit();
    }

    public void setGoogleSignIn(boolean signIn) {
        editor.putBoolean(GOOGLE_SIGN_IN, signIn);
        editor.commit();
    }

    public boolean getGoogleSignIn() {
        return pref.getBoolean(GOOGLE_SIGN_IN,false);

    }
    public void setFacebookSignIn(boolean signIn) {
        editor.putBoolean(FACEBOOK_SIGN_IN, signIn);
        editor.commit();
    }

    public boolean getFacebookSignIn() {
        return pref.getBoolean(FACEBOOK_SIGN_IN,false);
    }

    public void setReadingBuddy(ReadingBuddyAvatarModel avatar) {
        Gson gson = new Gson();
        String json = gson.toJson(avatar);
        editor.putString(READING_BUDDY, json);
        editor.commit();
    }

    public ReadingBuddyAvatarModel getReadingBuddy() {
        Gson gson = new Gson();
        String json = pref.getString(READING_BUDDY, "");
        return gson.fromJson(json, ReadingBuddyAvatarModel.class);
    }

    public void setReadingBuddyFrame(AvatarFrameModel frame) {
        Gson gson = new Gson();
        String json = gson.toJson(frame);
        editor.putString(READING_BUDDY_FRAME, json);
        editor.commit();
    }

    public AvatarFrameModel getReadingBuddyFrame() {
        Gson gson = new Gson();
        String json = pref.getString(READING_BUDDY_FRAME, "");
        return gson.fromJson(json, AvatarFrameModel.class);
    }

    public void setReadingBuddyBackground(AvatarBackgroundModel bg){
        Gson gson = new Gson();
        String json = gson.toJson(bg);
        editor.putString(READING_BUDDY_BG, json);
        editor.commit();
    }

    public AvatarBackgroundModel getReadingBuddyBg() {
        Gson gson = new Gson();
        String json = pref.getString(READING_BUDDY_BG, "");
        return gson.fromJson(json, AvatarBackgroundModel.class);
    }

    public void setReadingBuddyPoint(int point) {
        editor.putInt(READING_BUDDY_POINT, point);
        editor.commit();
    }
    public int getReadingBuddyPoint() {
        return pref.getInt(READING_BUDDY_POINT,0);
    }

    public void setUserProfileSettings(List<UserProfileSetting> profileSettings) {
        Gson gson = new Gson();
        String profileSettingsJson = gson.toJson(profileSettings);
        editor.putString(USER_PROFILE_SETTINGS, profileSettingsJson);
        editor.apply();
    }

    public List<UserProfileSetting> getUserProfileSettings() {
        List<UserProfileSetting> profileSettings = new ArrayList<>();
        String profileSettingsJson = pref.getString(USER_PROFILE_SETTINGS, "");
        if (!profileSettingsJson.isEmpty()) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<UserProfileSetting>>() {}.getType();
            profileSettings = gson.fromJson(profileSettingsJson, type);
        }
        return profileSettings;
    }

    public UserProfileSetting getCurrentUserProfile(){
        List<UserProfileSetting> userProfileSettings = getUserProfileSettings();
        int profileId = Integer.valueOf(getProfileId());
        for (int i = 0; i < userProfileSettings.size(); i++) {
            if(userProfileSettings.get(i).getProfileId() == profileId)
                return userProfileSettings.get(i);
        }
        return null;
    }

    public boolean isAutoCorrectOwnStorySetting(){
        if(getCurrentUserProfile() != null)
            return getCurrentUserProfile().isAutoCorrectOwnStory();
        return true;
    }

    public boolean showBookByReadingLevelSetting(){
        if(getCurrentUserProfile() != null)
            return getCurrentUserProfile().isBookByReadingLevel();
        return false;
    }

    public boolean showRecommendByReadingLevelSetting(){
        if(getCurrentUserProfile() != null)
            return getCurrentUserProfile().isRecommendByReadingLevel();
        return false;
    }

    public boolean getActivateReadingTime(){
        if(getCurrentUserProfile() != null)
            return getCurrentUserProfile().isSetLimitTime();
        return false;
    }

    public boolean canReadToday(){
        if(getActivateReadingTime()){
            return getCurrentUserProfile().getReadingDay().contains(getCurrentDayName());
        }
        return true;
    }

    public String getCurrentDayName(){
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == Calendar.MONDAY)
            return "Monday";
        else if (dayOfWeek == Calendar.TUESDAY)
            return "Tuesday";
        else if (dayOfWeek == Calendar.WEDNESDAY)
            return "Wednesday";
        else if (dayOfWeek == Calendar.THURSDAY)
            return "Thursday";
        else if (dayOfWeek == Calendar.FRIDAY)
            return "Friday";
        else if (dayOfWeek == Calendar.SATURDAY)
            return "Saturday";

        return "Sunday";
    }

    public int getReadingTime() {
        if(getActivateReadingTime()){
            return getCurrentUserProfile().getReadingTime() * 60;
        }
        return 3600;
    }

    public boolean isCalculatedByEachLogins(){
        if(getCurrentUserProfile() != null)
            return getCurrentUserProfile().getCalculatedBy().equals("Each Login For");
        return false;
    }

    public boolean isReadingTimeExpired(){
        if(getActivateReadingTime()){
            if(getCurrentUserProfile().getCalculatedBy().equals("Each Login For"))
                return true;
            else
                return !lastTimeReadingIsToday();
        }
        return false;
    }

    public void setLastTimeReading(String systemTime){
        editor.putString(LAST_ACTIVE_READING_TIME, systemTime);
        editor.commit();
    }

    public String getLastTimeReading(){
        return pref.getString(LAST_ACTIVE_READING_TIME,null);
    }

    public Boolean lastTimeReadingIsToday(){
        String storedTime = getLastTimeReading();
        Calendar calendar = Calendar.getInstance();
        String currentTime = getDateTime(calendar);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        try {
            Date storedDate = dateFormat.parse(storedTime);
            Date currentDate = dateFormat.parse(currentTime);
            boolean isSameDay = storedDate.compareTo(currentDate) == 0;
            return isSameDay;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getDateTime(Calendar calendar) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(calendar.getTime());
    }

    public void resetCurrentReadingTime(){
        setCurrentReadingTime(0);
    }

    public void setCurrentReadingTime(int time) {
        editor.putInt(CURRENT_READING_TIME, time);
        editor.commit();
    }

    public int getCurrentReadingTime() {
        return pref.getInt(CURRENT_READING_TIME,0);
    }

    public void setTempReadingTime(int time){
        editor.putInt(TEMP_READING_TIME, time);
        editor.commit();
    }
    public int getTempReadingTime(){
        return pref.getInt(TEMP_READING_TIME,0);
    }

//    public void setCurrentRestingTime(int time) {
//        editor.putInt(CURRENT_RESTING_TIME, time);
//        editor.commit();
//    }
//    public int getCurrentRestingTime() {
//        return pref.getInt(CURRENT_RESTING_TIME,0);
//    }
//
//    public void setNextActiveTime(String systemTime) {
//        editor.putString(NEXT_ACTIVE_READING_TIME, systemTime);
//        editor.commit();
//    }
//
//    public String getNextActiveTime(){
//        return pref.getString(NEXT_ACTIVE_READING_TIME,null);
//    }


    public void setReadingLevel(String readingLevel) {
        editor.putBoolean(HAVE_READING_LEVEL, true);
        editor.putString(READING_LEVEL, readingLevel);
        editor.commit();
    }
    public boolean isHavingReadingLevel() {
        return pref.getBoolean(HAVE_READING_LEVEL, false);
    }
    public String getReadingLevel() {
        return pref.getString(READING_LEVEL, "1");
    }


    public void setReadingPoints(int point) {
        editor.putInt(READING_POINTS, point);
        editor.commit();
    }
    public int getReadingPoints() {
        return pref.getInt(READING_POINTS,0);
    }
}
