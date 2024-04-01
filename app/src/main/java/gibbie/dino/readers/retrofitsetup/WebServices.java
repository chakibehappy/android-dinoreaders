package gibbie.dino.readers.retrofitsetup;

import com.google.gson.JsonElement;

import gibbie.dino.readers.ui.activities.login.LoginModel;
import gibbie.dino.readers.ui.activities.logout.LogoutModel;
import gibbie.dino.readers.ui.activities.register.RegisterModel;
import gibbie.dino.readers.ui.fragments.collection.CollectionDetailModel;
import gibbie.dino.readers.ui.fragments.home.BookModel;
import gibbie.dino.readers.ui.fragments.home.DashboardModel;
import gibbie.dino.readers.ui.fragments.profile.ProfileDetailModel;
import gibbie.dino.readers.ui.fragments.profile.ProfilesModel;
import gibbie.dino.readers.ui.fragments.singlebook.SingleBookModel;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WebServices {
    @POST(WebUrl.LOGINURL)
    Call<LoginModel> MakeLogin(
            @Header("Accept") String accept,
            @Header("Content-Type") String contenttype,
            @Query("email") String username,
            @Query("password") String password,
            @Query("device_type") String device_type
    );
    @GET(WebUrl.LOGOUT)
    Call<LogoutModel> makeLogout(
            @Header("Accept") String accept,
            @Header("Authorization") String authorization
    );
    @POST(WebUrl.REGISTERURL)
    Call<RegisterModel> Register(
            @Header("Accept") String accept,
            @Header("Content-Type") String contenttype,
            @Query("email") String email,
            @Query("password") String password,
            @Query("password_confirmation") String password_confirmation
    );

    @GET(WebUrl.BOOKLATESTURL)
    Call<BookModel> BookLatest(
            @Header("Accept") String accept,
            @Header("Content-Type") String contenttype,
            @Header("Authorization") String authorization
    );
    @GET(WebUrl.BOOKDETAILURL +"{id}")
    Call<SingleBookModel> BookDetail(
            @Header("Accept") String accept,
            @Header("Content-Type") String contenttype,
            @Header("Authorization") String authorization,
            @Path("id") String id
    );
    @GET(WebUrl.LIBRARYURL)//THIS FOR LIBRARY GRID
    Call<BookModel> LibraryGrid(
            @Header("Accept") String accept,
            @Header("Content-Type") String contenttype,
            @Header("Authorization") String authorization
    );
    @GET(WebUrl.LIBRARYURL)
    Call<DashboardModel> LibrarySlider(
            @Header("Accept") String accept,
            @Header("Content-Type") String contenttype,
            @Header("Authorization") String authorization
    );
    @POST(WebUrl.FAVOURITEURL)
    Call<JsonElement> Favourite(
            @Header("Accept") String accept,
            @Header("Content-Type") String contenttype,
            @Header("Authorization") String authorization,
            @Query("book_id") String email
    );
    @GET(WebUrl.SEARCHURL +"{query}")
    Call<BookModel> Search(
            @Header("Accept") String accept,
            @Header("Content-Type") String contenttype,
            @Header("Authorization") String authorization,
            @Path("query") String query
    );
    @GET(WebUrl.PROFILEURL)
    Call<ProfilesModel> MyProfiles(
            @Header("Accept") String accept,
            @Header("Content-Type") String contenttype,
            @Header("Authorization") String authorization
    );
    @GET(WebUrl.PROFILEDETAILURL+"{id}")
    Call<ProfileDetailModel> ProfileDetail(
            @Header("Accept") String accept,
            @Header("Content-Type") String contenttype,
            @Header("Authorization") String authorization,
            @Path("id") String query
    );
    @POST(WebUrl.SETPROFILEURL)
    Call<JsonElement> SetProfile(
            @Header("Accept") String accept,
            @Header("Content-Type") String contenttype,
            @Header("Authorization") String authorization,
            @Query("id") String profile_id
    );
    @DELETE(WebUrl.DELETEPROFILEURL+"{id}")
    Call<JsonElement> DeleteProfile(
            @Header("Accept") String accept,
            @Header("Content-Type") String contenttype,
            @Header("Authorization") String authorization,
            @Path("id") String query
    );
    @GET(WebUrl.DASHBOARDURL)
    Call<DashboardModel> Dashboard(
            @Header("Accept") String accept,
            @Header("Content-Type") String contenttype,
            @Header("Authorization") String authorization
    );
    @POST(WebUrl.GOOGLEURL)
    Call<LoginModel> MakeLoginGoogle(
            @Header("Accept") String accept,
            @Header("Content-Type") String contenttype,
            @Query("access_token") String access_token
    );
    @POST(WebUrl.FACEBOOKURL)
    Call<LoginModel> MakeLoginFacebook(
            @Header("Accept") String accept,
            @Header("Content-Type") String contenttype,
            @Query("access_token") String access_token
    );
    @POST(WebUrl.COLLECTIONDASHBOARDURL)
    Call<DashboardModel> CollectionDashboard(
            @Header("Accept") String accept,
            @Header("Content-Type") String contenttype,
            @Header("Authorization") String authorization,
            @Query("category_selection") String query
    );
    @GET(WebUrl.COLLECTIONDETAILURL+"{id}")
    Call<CollectionDetailModel> CollectionDetail(
            @Header("Accept") String accept,
            @Header("Content-Type") String contenttype,
            @Header("Authorization") String authorization,
            @Path("id") String query
    );
    @POST(WebUrl.COLLECTIONFAVOURITEURL)
    Call<JsonElement> FavouriteCollection(
            @Header("Accept") String accept,
            @Header("Content-Type") String contenttype,
            @Header("Authorization") String authorization,
            @Query("collection_id") String collection_id
    );
    @GET(WebUrl.CLASSDASHBOARDURL)
    Call<DashboardModel> ClassesDashboard(
            @Header("Accept") String accept,
            @Header("Content-Type") String contenttype,
            @Header("Authorization") String authorization
    );
    @POST(WebUrl.CLASSJOINDURL)
    Call<JsonElement> ClassesJoin(
            @Header("Accept") String accept,
            @Header("Content-Type") String contenttype,
            @Header("Authorization") String authorization,
            @Query("class_id") String class_id
    );
    @POST(WebUrl.CLASSJOINBYCODEURL)
    Call<JsonElement> ClassesJoinByCode(
            @Header("Accept") String accept,
            @Header("Content-Type") String contenttype,
            @Header("Authorization") String authorization,
            @Query("code") String code
    );
    @POST(WebUrl.CLASSLEAVEURL)
    Call<JsonElement> ClassesLeave(
            @Header("Accept") String accept,
            @Header("Content-Type") String contenttype,
            @Header("Authorization") String authorization,
            @Query("class_id") String class_id
    );
    @GET(WebUrl.CLASSDETAILURL+"{id}")
    Call<CollectionDetailModel> ClassesDetail(
            @Header("Accept") String accept,
            @Header("Content-Type") String contenttype,
            @Header("Authorization") String authorization,
            @Path("id") String query
    );
}