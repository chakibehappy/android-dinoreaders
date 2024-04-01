package gibbie.dino.readers.retrofitsetup;


public interface WebUrl {

    String Accept = "application/json";
    String ContentType = "application/json";
    String MULTIPAT_CONTENT = "multipart/form-data";
    String BASEURL_INTERNET_CHECK = "http://google.com/";

    //live
//    String BASEURL = "http://dinoreader.com:8002/";
//    String BASEAPIURL = BASEURL + "api/";
    //mine
//    String BASEURL = "http://192.168.109.197:8555/";
//    String BASEAPIURL = BASEURL + "api/";
    //emu
    //String BASEURL = "http://10.0.2.2:8555/";
    //String BASEURL = "http://54.251.190.8/";
//    String BASEURL = "https://7d55-114-122-138-88.ngrok-free.app/";
    String BASEURL = "http://dinoreaders.com/";
//    String BASEURL = "http://54.251.190.8/";

    String BASEAPIURL = BASEURL+"api/";

    String LOGINURL = BASEAPIURL + "login";
    String REGISTERURL = BASEAPIURL + "register";
    String LOGOUT = BASEAPIURL + "logout";
    String BOOKLATESTURL = BASEAPIURL + "dashboard/latestfive";
    String BOOKDETAILURL = BASEAPIURL + "book/single/";
    String LIBRARYURL= BASEAPIURL + "library";
    String FAVOURITEURL = LIBRARYURL + "/favourite";
    String SEARCHURL = BASEAPIURL + "book/search/";
    String PROFILEURL = BASEAPIURL + "profile";
    String CREATEPROFILEURL = BASEAPIURL + "profile/store";
    String PROFILEDETAILURL = BASEAPIURL + "profile/show/";
    String UPDATEPROFILEURL = BASEAPIURL + "profile/update";
    String SETPROFILEURL = BASEAPIURL + "profile/set";
    String DELETEPROFILEURL = BASEAPIURL + "profile/delete/";
    String DASHBOARDURL = BASEAPIURL + "dashboard";
    String GETPROFILEINFOURL = BASEAPIURL + "profile/show_info/";
    String SOCIALITEURL = BASEAPIURL + "socialite/mobileCallback/";
    String GOOGLEURL = SOCIALITEURL + "google";
    String FACEBOOKURL = SOCIALITEURL + "facebook";
    String COLLECTIONDASHBOARDURL = BASEAPIURL + "collections";
    String COLLECTIONDETAILURL = COLLECTIONDASHBOARDURL + "/details/";
    String COLLECTIONFAVOURITEURL = COLLECTIONDASHBOARDURL + "/favourite";
    String CLASSBASEURL = BASEAPIURL + "classes";
    String CLASSDASHBOARDURL = CLASSBASEURL + "/dashboard";
    String CLASSJOINDURL = CLASSBASEURL + "/join";
    String CLASSJOINBYCODEURL = CLASSJOINDURL + "/by-code";
    String CLASSLEAVEURL = CLASSBASEURL + "/left";
    String CLASSDETAILURL = CLASSBASEURL + "/details/";

    String OWNSTORYURL = BASEAPIURL + "own-story/all-books/";
    String UPLOADOWNSTORYURL = BASEAPIURL + "own-story/upload";

    String PROFILESETTINGURL = BASEAPIURL + "parent-setting/show/";
    String SAVESETTINGURL = BASEAPIURL + "parent-setting/edit";

    String SAVEREADINGHISTORYURL = BASEAPIURL + "profile-book-reading/store";
    String ALLBOOK = BASEAPIURL + "publish/book/all";
    String BOOKBYREADINGLEVEL = BASEAPIURL + "publish/book/allByReadingLevel/";

    String AWSPATH = "https://dinoreadersbucket.s3.ap-southeast-1.amazonaws.com/public/";
    String AWSBOOKPATH = AWSPATH + "document/";

    String SAVEPLACEMENTTESTRESULT = BASEAPIURL + "placement-test/store";
    String GETBOOKQUIZ = BASEAPIURL + "book-quiz/view-quiz/";
}