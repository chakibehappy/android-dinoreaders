package gibbie.dino.readers.ui.activities.placementtest;

import static gibbie.dino.readers.retrofitsetup.CommonResponse.isUnauthorized;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gibbie.dino.readers.R;
import gibbie.dino.readers.commonclasses.Functions;
import gibbie.dino.readers.database.SessionManager;
import gibbie.dino.readers.retrofitsetup.ServiceGenerator;
import gibbie.dino.readers.retrofitsetup.WebServices;
import gibbie.dino.readers.retrofitsetup.WebUrl;
import gibbie.dino.readers.ui.fragments.singlebook.SingleBookModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GradeBookListAdapter extends RecyclerView.Adapter<GradeBookListAdapter.DataViewHolder>{
    private Context mCtx;
    private List<GradeBookData> listBookData;
    private int readingLevel;

    public GradeBookListAdapter(Context mCtx, List<GradeBookData> listBookData, int readingLevel) {
        this.mCtx = mCtx;
        this.listBookData = listBookData;
        this.readingLevel = readingLevel;
    }

    @Override
    public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.list_view_book, parent,false);
        return new DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final GradeBookData book = listBookData.get(position);
        final String id, title;

        id = String.valueOf(book.getId());
        title = book.getTitle();

        holder.txt_title.setText(title);
        Picasso.get()
                .load(book.getImageUrl())
                .placeholder(R.drawable.book)
                .into(holder.vw_image);

        holder.vw_card.setOnClickListener(v -> openSelectedBook(mCtx, id, book.getUid(), title));

        if(position == 0){
            ViewGroup.MarginLayoutParams layoutParams =
                    (ViewGroup.MarginLayoutParams)  holder.vw_card.getLayoutParams();
            layoutParams.setMargins(24, 16, 16, 16);
            holder.vw_card.setLayoutParams(layoutParams);
        }
    }

    @Override
    public int getItemCount() {
        return listBookData.size();
    }

    class DataViewHolder extends RecyclerView.ViewHolder {

        CardView vw_card;
        ImageView vw_image, medal_image;
        TextView txt_title;

        public DataViewHolder(View itemView) {
            super(itemView);
            vw_card = itemView.findViewById(R.id.vw_card);
            vw_image = itemView.findViewById(R.id.vw_image);
            txt_title = itemView.findViewById(R.id.txt_title);
        }
    }

    private void openSelectedBook(Context context, String id, String uid, String title)
    {
        List<SingleBookModel.Data.Pages> pageSettings;
        WebServices webServices = ServiceGenerator.createService(WebServices.class);
        SessionManager sessionManager = new SessionManager(mCtx);
        webServices.BookDetail(WebUrl.Accept, WebUrl.ContentType, sessionManager.getAccesstoken(),id).enqueue(new Callback<SingleBookModel>() {
            @Override
            public void onResponse(Call<SingleBookModel> call, Response<SingleBookModel> response) {
                try {

                    if (response.isSuccessful()) {
                        try {
                            SingleBookModel.Data data = response.body().getData();
                            List<SingleBookModel.Data.Pages> pageSettings = data.getPages();

                            String url = WebUrl.AWSBOOKPATH + uid + "/book_data.json";
                            JsonReaderTask task = new JsonReaderTask(new JsonReaderTask.JsonReaderListener() {
                                @Override
                                public void onJsonRead(Book book) {
                                    List<Page> pages = book.getPages();
                                    List<Page> finalPages = new ArrayList<>();
                                    // process to add both text and audio url here
                                    for (int i = 0; i < pages.size(); i++) {

                                        pages.get(i).setStoryPage(pageSettings.get(i).getShow_pages() == 1);
                                        pages.get(i).setPlayAudio(pageSettings.get(i).getPlay_audio() == 1);

                                        if(pages.get(i).isStoryPage())
                                        {
                                            if(pages.get(i).getLines().size() <= 0)
                                                pages.get(i).setPlayAudio(false);
                                            finalPages.add(pages.get(i));
                                        }
                                    }

                                    Intent intent = new Intent(context, PlacementTestReadingActivity.class);
                                    intent.putExtra("id", id);
                                    intent.putExtra("uid", uid);
                                    intent.putExtra("readingLevel", readingLevel);
                                    intent.putExtra("title", title);
                                    intent.putExtra("pagesList", (Serializable) finalPages);
                                    context.startActivity(intent);
                                }
                            });
                            task.execute(url);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (isUnauthorized((Activity)mCtx, response.code())) {
                        Log.e("response", "Unauthorized");
                    } else if (response.code() == 404) {
                        // "Duplicate entry";
                    } else if (response.code() == 500) {
                        // "Server is busy at this time Please try again.";
                    } else {
                        // "Oops something went wrong!Please try again";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<SingleBookModel> call, Throwable t) {
            }
        });
    }

}

